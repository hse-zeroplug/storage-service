package com.aaalace.storageservice.application.service;

import com.aaalace.storageservice.application.in.IFileService;
import com.aaalace.storageservice.application.in.IHashService;
import com.aaalace.storageservice.domain.exception.BadRequestError;
import com.aaalace.storageservice.domain.exception.InternalServerError;
import com.aaalace.storageservice.domain.model.File;
import com.aaalace.storageservice.infrastructure.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService implements IFileService {

    @Value("${internal.dir}")
    private String uploadDir;

    private final IHashService hashService;
    private final FileRepository fileRepository;

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> download(String id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("File entity not found: id={}", id);
                    return new BadRequestError("File entity not found");
                });

        Path path = Path.of(uploadDir, id);
        if (!Files.exists(path)) {
            log.error("File not found in storage: id={}", id);
            throw new InternalServerError("File not found in storage");
        }

        try {
            Resource resource = new UrlResource(path.toUri());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(Files.size(path))
                    .body(resource);
        } catch (IOException e) {
            log.error("Error sending file", e);
            throw new InternalServerError("Error sending file");
        }
    }

    @Transactional
    public File upload(MultipartFile file) {
        String hash = hashService.hash(file);

        File exists = fileRepository.findByHash(hash).orElse(null);
        if (exists != null) {
            log.info("File with same contents already exists, id={}", exists.getId());
            return exists;
        }

        File newFile = fileRepository.save(File.builder()
                .name(file.getOriginalFilename())
                .hash(hash)
                .build());
        log.info("Saved new file entity, id={}", newFile.getId());

        String url = save(newFile.getId(), file);

        newFile.setUrl(url);
        newFile = fileRepository.save(newFile);

        return newFile;
    }

    private String save(String id, MultipartFile file) {
        try {
            Path uploadPath = Path.of(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path targetPath = uploadPath.resolve(id);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return targetPath.toString();
        } catch (IOException e) {
            log.error("Error while saving file", e);
            throw new InternalServerError("Error while saving file");
        }
    }
}
