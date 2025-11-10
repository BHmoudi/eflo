# Document Service

## Overview

The Document Service manages document storage, retrieval, and generation for the Eflo platform. It provides secure document management with MinIO S3-compatible storage, document metadata tracking, and integration with document generation services.

## Functionality

### Core Features

- **Document Upload**: Secure file upload with validation
- **Document Storage**: S3-compatible storage using MinIO
- **Document Retrieval**: Fast document download and streaming
- **Document Metadata**: Track document type, version, status, and relationships
- **Document Categories**: Organize documents by type (INVOICE, CONTRACT, IDENTITY, VEHICLE_REGISTRATION, etc.)
- **Version Control**: Track multiple versions of the same document
- **Access Control**: Role-based access to documents
- **Document Generation**: Integration with document generation service for invoices, contracts
- **Event Publishing**: Kafka events for document lifecycle
- **Thumbnail Generation**: Automatic thumbnail creation for images/PDFs (optional)

### Technical Details

- **Port**: 8083
- **Database**: PostgreSQL (port 5436)
- **Storage**: MinIO (ports 9000, 9001)
- **Technology**: Spring Boot 3.x, MinIO Java SDK, Spring Data JPA
- **API Documentation**: Swagger/OpenAPI at `http://localhost:8083/swagger-ui.html`

## Database Schema

### Main Entities

- **Document**: Core document metadata
- **DocumentVersion**: Version tracking for documents
- **DocumentCategory**: Document classification
- **DocumentAccess**: Access control records
- **DocumentRelation**: Links between documents and entities (orders, users, etc.)

### Document Types

- **INVOICE**: Customer invoices
- **CONTRACT**: Sales contracts
- **IDENTITY**: Customer ID documents
- **VEHICLE_REGISTRATION**: Vehicle registration papers
- **INSURANCE**: Insurance documents
- **FINANCE**: Finance/loan documents
- **DELIVERY_NOTE**: Delivery receipts
- **CUSTOM**: User-defined document types

### Document Status

- **DRAFT**: Document being created
- **PENDING_APPROVAL**: Awaiting approval
- **APPROVED**: Approved for use
- **ARCHIVED**: Archived document
- **DELETED**: Soft-deleted document

## API Endpoints

### Document Upload

#### Upload Document
```http
POST /api/v1/documents/upload
Authorization: Bearer {jwt-token}
Content-Type: multipart/form-data

Form Data:
- file: [binary file data]
- documentType: INVOICE
- relatedEntityType: ORDER
- relatedEntityId: 123
- description: Vehicle order invoice
```

Response:
```json
{
  "id": 456,
  "filename": "invoice_123.pdf",
  "documentType": "INVOICE",
  "fileSize": 245678,
  "mimeType": "application/pdf",
  "storageKey": "documents/2025/11/invoice_123_uuid.pdf",
  "uploadedAt": "2025-11-10T10:30:00Z",
  "uploadedBy": 789
}
```

#### Upload Multiple Documents
```http
POST /api/v1/documents/upload/batch
Content-Type: multipart/form-data

Form Data:
- files: [file1, file2, file3]
- documentType: VEHICLE_REGISTRATION
```

### Document Retrieval

#### Get Document by ID
```http
GET /api/v1/documents/{id}
```

#### Download Document
```http
GET /api/v1/documents/{id}/download
```

Returns binary file with appropriate Content-Type header.

#### Get Document Preview/Thumbnail
```http
GET /api/v1/documents/{id}/thumbnail
```

#### Get Documents by Entity
```http
GET /api/v1/documents/entity/ORDER/123
GET /api/v1/documents/entity/USER/456
```

#### Search Documents
```http
GET /api/v1/documents/search?filename=invoice&documentType=INVOICE&from=2025-01-01
```

### Document Management

#### Update Document Metadata
```http
PUT /api/v1/documents/{id}
Content-Type: application/json

{
  "description": "Updated description",
  "documentType": "CONTRACT",
  "status": "APPROVED"
}
```

#### Delete Document
```http
DELETE /api/v1/documents/{id}
```

Soft delete - marks document as DELETED but doesn't remove from storage.

#### Permanently Delete Document
```http
DELETE /api/v1/documents/{id}/permanent
```

Removes document from database and MinIO storage. Requires SUPER_ADMIN role.

### Document Versions

#### Create New Version
```http
POST /api/v1/documents/{id}/versions
Content-Type: multipart/form-data

Form Data:
- file: [new version file]
- versionNotes: Fixed typo in customer name
```

#### Get Document Versions
```http
GET /api/v1/documents/{id}/versions
```

#### Download Specific Version
```http
GET /api/v1/documents/{id}/versions/{versionNumber}/download
```

### Document Generation

#### Generate Invoice
```http
POST /api/v1/documents/generate/invoice/{orderId}
```

#### Generate Contract
```http
POST /api/v1/documents/generate/contract/{orderId}
Content-Type: application/json

{
  "templateId": "standard-contract-v2",
  "variables": {
    "customerName": "John Doe",
    "deliveryDate": "2025-12-01"
  }
}
```

#### Generate Custom Document
```http
POST /api/v1/documents/generate/custom
Content-Type: application/json

{
  "templateId": "custom-template-1",
  "outputFormat": "PDF",
  "data": {
    "title": "Custom Report",
    "content": "..."
  }
}
```

## Configuration

### Application Properties

```yaml
server:
  port: 8083

spring:
  application:
    name: document-service
  datasource:
    url: jdbc:postgresql://localhost:5436/document_db
    username: documentuser
    password: ${DOCUMENT_DB_PASSWORD}
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

minio:
  endpoint: http://localhost:9000
  access-key: ${MINIO_ROOT_USER}
  secret-key: ${MINIO_ROOT_PASSWORD}
  bucket-name: eflo-documents
  secure: false
```

### Environment Variables

- `DOCUMENT_DB_PASSWORD`: Database password
- `MINIO_ROOT_USER`: MinIO access key (default: minioadmin)
- `MINIO_ROOT_PASSWORD`: MinIO secret key (default: minioadmin)
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka broker URLs
- `KEYCLOAK_ISSUER_URI`: Keycloak issuer URI

## MinIO Storage

### Bucket Structure

```
eflo-documents/
├── documents/
│   ├── 2025/
│   │   ├── 01/
│   │   ├── 02/
│   │   └── 11/
│   │       ├── invoice_123_uuid.pdf
│   │       ├── contract_456_uuid.pdf
│   │       └── id_document_789_uuid.jpg
├── thumbnails/
│   ├── 2025/
│   │   └── 11/
│   │       └── id_document_789_uuid_thumb.jpg
└── temp/
```

### MinIO Configuration

Access MinIO console at: `http://localhost:9001`

Default credentials:
- Username: minioadmin
- Password: minioadmin

### Bucket Creation

The service automatically creates the bucket on startup if it doesn't exist:

```java
@PostConstruct
public void initBucket() {
    if (!minioClient.bucketExists(bucketName)) {
        minioClient.makeBucket(bucketName);
    }
}
```

## File Upload Validation

### Supported File Types

- **Documents**: PDF, DOC, DOCX, XLS, XLSX
- **Images**: JPG, JPEG, PNG, GIF, BMP
- **Text**: TXT, CSV

### Size Limits

- Maximum file size: 50MB (configurable)
- Maximum request size: 50MB (configurable)

### Validation Rules

```java
private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
    "application/pdf",
    "image/jpeg",
    "image/png",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
);

private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
```

## Event Publishing

### Document Events

| Event Topic | Trigger | Payload |
|-------------|---------|---------|
| `documents.uploaded` | Document uploaded | Document metadata |
| `documents.updated` | Metadata updated | Updated fields |
| `documents.deleted` | Document deleted | Document ID |
| `documents.generated` | Document generated | Generation details |

### Event Payload Example

```json
{
  "eventId": "uuid",
  "eventType": "DOCUMENT_UPLOADED",
  "timestamp": "2025-11-10T10:30:00Z",
  "documentId": 456,
  "documentType": "INVOICE",
  "relatedEntityType": "ORDER",
  "relatedEntityId": 123,
  "uploadedBy": 789
}
```

## Development

### Build

```bash
cd document-service
mvn clean package
```

### Run Tests

```bash
mvn test
mvn verify
```

### Run Locally

```bash
export DOCUMENT_DB_PASSWORD=documentpass
export MINIO_ROOT_USER=minioadmin
export MINIO_ROOT_PASSWORD=minioadmin

mvn spring-boot:run
```

### Run with Docker

```bash
docker-compose up -d document-service
```

### Database Migrations

Flyway migrations in `src/main/resources/db/migration/`:

```
V1__initial_schema.sql
V2__add_document_versions.sql
V3__add_document_relations.sql
V4__add_access_control.sql
```

## Testing

### Health Check

```bash
curl http://localhost:8083/actuator/health
```

### Upload Test Document

```bash
TOKEN="your-jwt-token"

curl -X POST http://localhost:8083/api/v1/documents/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/document.pdf" \
  -F "documentType=INVOICE" \
  -F "relatedEntityType=ORDER" \
  -F "relatedEntityId=123"
```

### Download Document

```bash
curl http://localhost:8083/api/v1/documents/456/download \
  -H "Authorization: Bearer $TOKEN" \
  --output downloaded-document.pdf
```

## Security

### Required Roles

- **Upload Document**: `SALESPERSON`, `SALES_MANAGER`, `ADMIN_LOCAL`, `SUPER_ADMIN`
- **Download Document**: Based on document access rules
- **Delete Document**: `SALES_MANAGER`, `ADMIN_LOCAL`, `SUPER_ADMIN`
- **Permanent Delete**: `SUPER_ADMIN`
- **Generate Document**: `SALESPERSON`, `SALES_MANAGER`, `ADMIN_LOCAL`, `SUPER_ADMIN`

### Access Control

Documents can have access restrictions:
- Public: Accessible by anyone with valid token
- Private: Only accessible by uploader
- Role-based: Accessible by specific roles
- User-based: Accessible by specific users

## Integration Points

### Order Service
- Stores order-related documents
- Listens to order events to generate invoices

### Workflow Service
- Integrates with n8n for document processing
- Stores workflow-related documents

### Document Generation Service
- Calls generation service for invoice/contract creation
- Stores generated documents

### n8n
- AI document analysis integration
- Automatic document classification

## Monitoring

### Metrics

Available at `/actuator/metrics`:
- `documents.uploaded.count`
- `documents.downloaded.count`
- `documents.storage.size`
- `documents.generation.count`

### Logging

```yaml
logging:
  level:
    com.eflo.document: DEBUG
    io.minio: INFO
```

## Troubleshooting

### Document upload fails
- Check file size limits
- Verify MIME type is allowed
- Ensure MinIO is running
- Check disk space

### MinIO connection errors
- Verify MinIO is running: `docker ps | grep minio`
- Check credentials in configuration
- Verify bucket exists
- Test MinIO console access

### Document not found
- Verify document ID exists in database
- Check if document is marked as DELETED
- Verify user has access permissions

## Performance Considerations

### Large File Handling
- Stream large files instead of loading into memory
- Use multipart upload for files > 5MB
- Implement resumable uploads

### Caching
- Cache document metadata
- Cache frequently accessed thumbnails
- Use CDN for public documents (production)

### Async Processing
- Async thumbnail generation
- Background document cleanup
- Async event publishing

## Additional Resources

- API Documentation: `http://localhost:8083/swagger-ui.html`
- MinIO Console: `http://localhost:9001`
- MinIO Documentation: https://min.io/docs/minio/linux/index.html
