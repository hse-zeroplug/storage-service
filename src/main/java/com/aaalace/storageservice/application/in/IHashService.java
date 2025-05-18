package com.aaalace.storageservice.application.in;

import org.springframework.web.multipart.MultipartFile;

public interface IHashService {
    String hash(MultipartFile file);
}
