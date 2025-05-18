package com.aaalace.storageservice.presentation.controller;

import com.aaalace.storageservice.application.in.IFileService;
import com.aaalace.storageservice.domain.model.File;
import com.aaalace.storageservice.domain.generic.GenericJsonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/file")
@RequiredArgsConstructor
public class FileController {

    private final IFileService fileService;

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> getFile(@PathVariable String id) {
        return fileService.download(id);
    }

    @PostMapping("/upload")
    public GenericJsonResponse<File> uploadFile(@RequestParam MultipartFile file) {
        File createdFile = fileService.upload(file);
        return GenericJsonResponse.success(createdFile);
    }
}
