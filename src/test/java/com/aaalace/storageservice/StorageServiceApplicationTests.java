package com.aaalace.storageservice;

import com.aaalace.storageservice.application.in.IHashService;
import com.aaalace.storageservice.application.service.FileService;
import com.aaalace.storageservice.domain.exception.BadRequestError;
import com.aaalace.storageservice.domain.exception.InternalServerError;
import com.aaalace.storageservice.domain.model.File;
import com.aaalace.storageservice.infrastructure.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class StorageServiceApplicationTests {

    @Mock
    private IHashService hashService;

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private FileService fileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fileService = new FileService(hashService, fileRepository);
        fileService.getClass().getDeclaredFields();
        try {
            java.lang.reflect.Field field = FileService.class.getDeclaredField("uploadDir");
            field.setAccessible(true);
            field.set(fileService, tempDir.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void upload_NewFile_Success() throws IOException {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        String hash = "12345";

        when(hashService.hash(multipartFile)).thenReturn(hash);
        when(fileRepository.findByHash(hash)).thenReturn(Optional.empty());
        when(fileRepository.save(any())).thenAnswer(invocation -> {
            File f = invocation.getArgument(0);
            f.setId("file-id");
            return f;
        });

        File result = fileService.upload(multipartFile);

        assertNotNull(result);
        assertEquals("test.txt", result.getName());
        assertEquals(hash, result.getHash());
        assertTrue(Files.exists(Path.of(result.getUrl())));
    }

    @Test
    void upload_ExistingFile_ReturnsExisting() {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        String hash = "12345";
        File existing = File.builder().id("exist-id").hash(hash).name("test.txt").build();

        when(hashService.hash(multipartFile)).thenReturn(hash);
        when(fileRepository.findByHash(hash)).thenReturn(Optional.of(existing));

        File result = fileService.upload(multipartFile);

        assertEquals("exist-id", result.getId());
        verify(fileRepository, never()).save(any());
    }

    @Test
    void upload_ErrorWhileSaving_ThrowsException() {
        MultipartFile multipartFile = new MockMultipartFile("file", "fail.txt", "text/plain", "data".getBytes());
        String hash = "fail-hash";

        when(hashService.hash(multipartFile)).thenReturn(hash);
        when(fileRepository.findByHash(hash)).thenReturn(Optional.empty());
        when(fileRepository.save(any())).thenAnswer(invocation -> {
            File f = invocation.getArgument(0);
            f.setId("fail-id");
            return f;
        });

        tempDir.toFile().setWritable(false);

        assertThrows(InternalServerError.class, () -> fileService.upload(multipartFile));
    }

    @Test
    void download_FileExists_ReturnsResponseEntity() throws IOException {
        String id = "file-id";
        String content = "data";
        java.nio.file.Path path = tempDir.resolve(id);
        Files.write(path, content.getBytes());

        File file = File.builder().id(id).name("myfile.txt").build();
        when(fileRepository.findById(id)).thenReturn(Optional.of(file));

        ResponseEntity<Resource> response = fileService.download(id);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("attachment; filename=\"myfile.txt\"", response.getHeaders().getFirst("Content-Disposition"));
    }

    @Test
    void download_FileNotFoundInDb_ThrowsBadRequest() {
        when(fileRepository.findById("not-exist")).thenReturn(Optional.empty());

        assertThrows(BadRequestError.class, () -> fileService.download("not-exist"));
    }

    @Test
    void download_FileNotOnDisk_ThrowsInternalServerError() {
        String id = "missing-file";
        File file = File.builder().id(id).name("nofile.txt").build();
        when(fileRepository.findById(id)).thenReturn(Optional.of(file));

        assertThrows(InternalServerError.class, () -> fileService.download(id));
    }
}
