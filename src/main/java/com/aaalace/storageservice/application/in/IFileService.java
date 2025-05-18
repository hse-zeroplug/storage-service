package com.aaalace.storageservice.application.in;

import com.aaalace.storageservice.domain.model.File;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
    ResponseEntity<Resource> download(String id);
    File upload(MultipartFile file);
}
