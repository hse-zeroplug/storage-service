# Storage Service

## Overview
The Storage Service stores and manages files. It saves information about files in MongoDB.

## Launch
Launch MongoDB server on 27072 port:
```bash
docker compose up
```
Launch app on 8091 port:
```bash
./gradlew bootRun
```
## Tests
All application-layer logic is covered by tests: [StorageServiceApplicationTests.java](src/test/java/com/aaalace/storageservice/StorageServiceApplicationTests.java)

## Data Model

| Field | Type   | Description                   |
|-------|--------|-------------------------------|
| id    | String | Unique ID for the file         |
| name  | String | Original name of the file      |
| hash  | String | Hash of the file content       |
| url   | String | Location or URL of the file    |

## Services

### FileService
- Upload files
- Get files by ID or other details

### HashService
- Create hash from file content
- Check file integrity and find duplicates

## Summary
This service helps to upload and get files. It saves file info in MongoDB and checks files using hashes to avoid duplicates and errors.