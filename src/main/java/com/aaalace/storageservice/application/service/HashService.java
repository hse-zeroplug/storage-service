package com.aaalace.storageservice.application.service;

import com.aaalace.storageservice.application.in.IHashService;
import com.aaalace.storageservice.domain.exception.InternalServerError;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

@Slf4j
@Component
public class HashService implements IHashService {

    @Override
    public String hash(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            log.error("Error while hashing", e);
            throw new InternalServerError("Error while hashing");
        }
    }
}
