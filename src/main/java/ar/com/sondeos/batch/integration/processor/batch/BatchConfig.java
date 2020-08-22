package ar.com.sondeos.batch.integration.processor.batch;

import ar.com.sondeos.batch.integration.processor.domain.AgencyContact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private static final Logger logger = LoggerFactory.getLogger(BatchConfig.class);

    public static final String STEP_NAME = "processingStep";
    public static final String JOB_NAME = "processingJob";

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;
    
    @Autowired
    ProcessorAgencyContact processorAgencyContact;

    @Autowired
    WriterAgencyContact writerAgencyContact;

    @Autowired
    JobCompletionNotificationListener jobCompletionNotificationListener;


    @Bean
    public Step processingStepBean(ItemReader<AgencyContact> reader) {
        logger.debug("---> Configuring Step: " + STEP_NAME);
        return stepBuilderFactory.get(STEP_NAME)
                .<AgencyContact, AgencyContact>chunk(1)
                .reader(reader)
                .processor(processorAgencyContact)
                .writer(writerAgencyContact)
                .build();
    }

    @Bean
    public Job processingJobBean(Step processingStep) {
        logger.debug("---> Configuring Job: " + JOB_NAME);
        return jobBuilderFactory.get(JOB_NAME)
                .listener(jobCompletionNotificationListener)
                .flow(processingStep)
                .end()
                .build();
    }
}
