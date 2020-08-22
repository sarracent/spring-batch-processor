package ar.com.sondeos.batch.integration.processor.integration;

import ar.com.sondeos.batch.integration.processor.FileMessageToJobRequest;
import com.jcraft.jsch.ChannelSftp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.integration.launch.JobLaunchingMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.*;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageChannel;

import java.io.File;

@Configuration
@IntegrationComponentScan
@EnableIntegration
public class IntegrationConfig {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationConfig.class);

    @Autowired
    private JobRegistry jobRegistry;

    @Bean
    public JobRegistryBeanPostProcessor jobRegistryInitializer() {
        JobRegistryBeanPostProcessor initializer = new JobRegistryBeanPostProcessor();
        initializer.setJobRegistry(jobRegistry);
        return initializer;
    }

    @Autowired
    Job importAgencyContactJob;

    @Autowired
    PGPTransformer pgpTransformer;

    @Autowired
    MoveFileTransformer moveFileTransformer;

    // Properties of Remote Host

    @Value("${sftp.host.ip}")
    private String sftpHostIp;

    @Value("${sftp.host.port}")
    private int sftphostPort;

    @Value("${sftp.host.user}")
    private String sftpHostUser;

    // Further Addition on private key and private key paraphrase in case needed

    /*
     * @Value("${sftp.privateKey:#{null}}") private Resource sftpPrivateKey;
     *
     * @Value("${sftp.privateKeyPassphrase:}") private String
     * sftpPrivateKeyPassphrase;
     */

    @Value("${sftp.host.password}")
    private String sftpHostPassword;

    @Value("${sftp.host.remote.directory.download}")
    private String sftpRemoteDirectoryDownloadHost;

    // @Value("${sftp.local.directory.download:${java.io.tmpdir}/localDownload}")

    // Local Directory for Download
    @Value("${sftp.local.directory.download}")
    private String sftpLocalDirectoryDownload;

    // @Value("${sftp.remote.directory.download.filter:*.*}")
    @Value("${sftp.host.remote.directory.download.filter}")
    private String sftpRemoteDirectoryDownloadFilter;

    // Creating session for Source SFTP server Folder
    @Bean
    public SessionFactory<ChannelSftp.LsEntry> sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        /*
         * factory.setHost("192.168.56.1"); factory.setPort(22);
         * factory.setUser("tester"); factory.setPassword("password");
         * factory.setAllowUnknownKeys(true);
         */

        factory.setHost(sftpHostIp);
        factory.setPort(sftphostPort);
        factory.setUser(sftpHostUser);
        factory.setPassword(sftpHostPassword);
        factory.setAllowUnknownKeys(true);
        return new CachingSessionFactory<ChannelSftp.LsEntry>(factory);
    }

    /*
     * The SftpInboundFileSynchronizer uses the session factory that we defined above.
     * Here we set information about the remote directory to fetch files from.
     * We could also set filters here to control which files get downloaded
     */
    @Bean
    public SftpInboundFileSynchronizer sftpInboundFileSynchronizer() {
        logger.info("---> Sincronizando");
        SftpInboundFileSynchronizer fileSynchronizer = new SftpInboundFileSynchronizer(sftpSessionFactory());
        fileSynchronizer.setDeleteRemoteFiles(true);
        fileSynchronizer.setRemoteDirectory(sftpRemoteDirectoryDownloadHost);
        fileSynchronizer.setFilter(new SftpSimplePatternFileListFilter(sftpRemoteDirectoryDownloadFilter));
        return fileSynchronizer;
    }


    @Bean
    public MessageChannel fileInputChannel() {
        return new DirectChannel();
    }

    /*
     * The Message source bean uses the @InboundChannelAdapter annotation.
     * This message source connects the synchronizer we defined above to a message queue (sftpChannel).
     * The adapter will take files from the sftp server and place them in the message queue as messages
     */
    @Bean
    @InboundChannelAdapter(channel = "fileInputChannel", poller = @Poller(fixedDelay = "5000"))
    public MessageSource<File> sftpReadingMessageSource() {
        logger.info("---> Entrando al sftpReadingMessageSource (InboundChannelAdapter)");
        SftpInboundFileSynchronizingMessageSource source = new SftpInboundFileSynchronizingMessageSource(sftpInboundFileSynchronizer());
        //source.setLocalDirectory(new File("sftp-inbound"));
        source.setLocalDirectory(new File(sftpLocalDirectoryDownload));
        source.setAutoCreateLocalDirectory(true);
        source.setLocalFilter(new AcceptOnceFileListFilter<File>());
        
        return source;
    }

    /*
     * Spring Batch Integration Configuration
     */

    @Bean
    @Transformer(inputChannel = "fileToJobProcessor", outputChannel = "jobChannel")
    public FileMessageToJobRequest fileMessageToJobRequest() {
        logger.info("---> Entrando al fileMessageToJobRequest");
        FileMessageToJobRequest transformFileToRequest = new FileMessageToJobRequest();
        transformFileToRequest.setFileParameterName("input.file.name");
        transformFileToRequest.setJob(importAgencyContactJob);
        return transformFileToRequest;
    }

    @Bean
    @ServiceActivator(inputChannel = "jobChannel", outputChannel = "nullChannel")
    protected JobLaunchingMessageHandler launcher(JobLauncher jobLauncher) {
        logger.info("---> Entrando al launcher del job");
        return new JobLaunchingMessageHandler(jobLauncher);
    }

    @Bean
    public IntegrationFlow integrationFlow(final JobLaunchingMessageHandler launcher) throws Exception {
        return IntegrationFlows.from(sftpReadingMessageSource(), c -> c.poller(Pollers.fixedRate(1000).maxMessagesPerPoll(1)))
                .transform(pgpTransformer)
                .transform(fileMessageToJobRequest())
                .handle(launcher)
                .transform(moveFileTransformer)
                .channel("nullChannel")
                .get();
    }
}
