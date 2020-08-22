package ar.com.sondeos.batch.integration.processor.gpg;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;

import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.callbacks.KeyringConfigCallbacks;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.keyrings.KeyringConfig;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.keyrings.KeyringConfigs;

import org.bouncycastle.util.io.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.BouncyGPG;
import org.springframework.stereotype.Service;

@Service
public class Decripter {

    private static Logger logger = LoggerFactory.getLogger(Decripter.class);

    static void installBCProvider() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static void decript(String pubKeyRingPath,
                               String secKeyRingPath,
                               String secKeyRingPassword,
                               String sourcePath,
                               String destPath) {


        final File pubKeyRing = new File(pubKeyRingPath);
        final File secKeyRing = new File(secKeyRingPath);
        final Path sourceFile = Paths.get(sourcePath);
        final Path destFile = Paths.get(destPath);

        try {
            installBCProvider();
            long startTime = System.currentTimeMillis();

            final int BUFFSIZE = 8 * 1024;
            logger.trace("Using a write buffer of {} bytes\n", BUFFSIZE);

            final KeyringConfig keyringConfig = KeyringConfigs.withKeyRingsFromFiles(pubKeyRing,
                    secKeyRing, KeyringConfigCallbacks.withPassword(secKeyRingPassword));

            try (
                    final InputStream cipherTextStream = Files.newInputStream(sourceFile);

                    final OutputStream fileOutput = Files.newOutputStream(destFile);
                    final BufferedOutputStream bufferedOut = new BufferedOutputStream(fileOutput, BUFFSIZE);

                    final InputStream plaintextStream = BouncyGPG
                            .decryptAndVerifyStream()
                            .withConfig(keyringConfig)
                            .andValidateSomeoneSigned()
                            .fromEncryptedInputStream(cipherTextStream)

            ) {
                Streams.pipeAll(plaintextStream, bufferedOut);
            }
            long endTime = System.currentTimeMillis();

            logger.info(String.format("DeEncryption took %.2f s", ((double) endTime - startTime) / 1000));
        } catch (Exception e) {
            logger.error("ERROR", e);
        }


    }
}
