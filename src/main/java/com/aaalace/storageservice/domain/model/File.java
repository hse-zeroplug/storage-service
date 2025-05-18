package com.aaalace.storageservice.domain.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "file")
public class File {
    @Id
    private String id;

    private String name;

    private String hash;

    private String url;
}
