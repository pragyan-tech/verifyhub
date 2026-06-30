package com.pragyan.verifyhub.document;

import com.pragyan.verifyhub.exception.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
@Slf4j
public class FileHasher {

    private static final String ALGORITHM = "SHA-256";
    private static final int BUFFER_SIZE = 8192;

    public String hash(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return hash(in);
        } catch (IOException e) {
            log.error("Failed to read file for hashing", e);
            throw new InvalidFileException("Could not read file content for hashing");
        }
    }


    public String hash(InputStream input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            try (DigestInputStream digestStream = new DigestInputStream(input, digest)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                while (digestStream.read(buffer) != -1) {

                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available in this JVM", e);
        } catch (IOException e) {
            log.error("IO error during hashing", e);
            throw new InvalidFileException("Could not read stream during hashing");
        }
    }
}