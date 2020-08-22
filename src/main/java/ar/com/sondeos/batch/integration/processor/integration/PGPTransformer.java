package ar.com.sondeos.batch.integration.processor.integration;

import ar.com.sondeos.batch.integration.processor.configurations.PGPConfigProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.Transformer;
import org.springframework.stereotype.Component;

import ar.com.sondeos.batch.integration.processor.gpg.Decripter;

import java.io.*;
import java.nio.file.*;

@Component
public class PGPTransformer {

    private static Logger logger = LoggerFactory.getLogger(PGPTransformer.class);

    @Value("${decrypted.directory}")
    private String decryptedDir;

    @Autowired
    Decripter decripter;

    @Autowired
    PGPConfigProperties pgpConfigProperties;

    @Transformer(inputChannel = "fileInputChannel", outputChannel = "fileToJobProcessor")
    public File transform(File aFile) throws IOException {

        logger.info("---> Descifrando archivo " + aFile.getName() + " y moviendo al decrypted");

        String pathOut = decryptedDir + "/" + aFile.getName();

        decripter.decript(
                pgpConfigProperties.getPublicKeyFilePath(),
                pgpConfigProperties.getSecretKeyFilePath(),
                pgpConfigProperties.getPassphrase(),
                aFile.getPath(),
                pathOut
                );

        Path fileToDeletePath = Paths.get(aFile.getPath());

        try {
            Files.delete(fileToDeletePath);
        } catch (NoSuchFileException x) {
            System.err.format("%s: no such" + " file or directory%n", fileToDeletePath);
        } catch (DirectoryNotEmptyException x) {
            System.err.format("%s not empty%n", fileToDeletePath);
        } catch (IOException x) {
            // File permission problems are caught here.
            System.err.println(x);
        }

        return new File(pathOut);
    }


}
