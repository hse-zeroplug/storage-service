package com.aaalace.storageservice.infrastructure.repository;

import com.aaalace.storageservice.domain.model.File;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends MongoRepository<File, String> {
    Optional<File> findByHash(String hash);
}