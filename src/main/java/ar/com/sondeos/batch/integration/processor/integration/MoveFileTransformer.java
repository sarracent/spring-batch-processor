package ar.com.sondeos.batch.integration.processor.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.Transformer;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

@Component
public class MoveFileTransformer {

    private static Logger logger = LoggerFactory.getLogger(MoveFileTransformer.class);

    @Value("${decrypted.directory}")
    private String decryptedDir;

    @Value("${backup.directory}")
    private String backupDir;


    @Transformer
    public File moveFile()  throws IOException {

        File folder = new File(decryptedDir);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {

            logger.info("---> Moviendo archivo descifrado " + listOfFiles[i].getName() + " al directorio backup");

            try {
                Path source = Paths.get(listOfFiles[i].getPath());
                Path newdir = Paths.get(backupDir);
                Files.move(source, newdir.resolve(source.getFileName()),
                        StandardCopyOption.REPLACE_EXISTING);
                logger.info("---> El archivo se movió con éxito.");
            }catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return new File(backupDir);
    }

}
