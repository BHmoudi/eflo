# Config Server

## Overview

Config Server is the centralized configuration management service for the Eflo microservices platform. It provides externalized configuration for all services across different environments using Spring Cloud Config.

## Functionality

### Core Features

- **Centralized Configuration**: Single source of truth for all service configurations
- **Environment-Specific Configs**: Support for dev, staging, production profiles
- **Native Profile Support**: Stores configurations in local file system or classpath
- **Dynamic Refresh**: Services can refresh configuration without restart (with Spring Cloud Bus)
- **Version Control Ready**: Can integrate with Git repositories for configuration versioning
- **Encryption/Decryption**: Support for encrypted property values

### Technical Details

- **Port**: 8888
- **Technology**: Spring Cloud Config Server
- **Profile**: Native (file-based configuration)
- **Configuration Location**: `src/main/resources/config/` or external directory

## Configuration

### Application Properties

```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/config
  profiles:
    active: native
```

### Supported Profiles

- **native**: Use local file system for configuration storage (default)
- **git**: Use Git repository for configuration storage
- **vault**: Use HashiCorp Vault for configuration storage

## API Endpoints

### Configuration Retrieval

- **GET** `/{application}/{profile}` - Get configuration for specific service and profile
  - Example: `/order-service/docker` - Returns order-service configuration for docker profile

- **GET** `/{application}-{profile}.yml` - Get configuration in YAML format
  - Example: `/order-service-docker.yml`

- **GET** `/{application}-{profile}.properties` - Get configuration in properties format

- **GET** `/{label}/{application}-{profile}.yml` - Get configuration from specific Git branch/label

### Health Check

- **GET** `/actuator/health` - Service health status

## Configuration Structure

```
config-server/
└── src/main/resources/config/
    ├── application.yml              # Common config for all services
    ├── order-service.yml           # Order service specific config
    ├── order-service-docker.yml    # Order service Docker profile
    ├── workflow-service.yml
    ├── commission-service.yml
    ├── document-service.yml
    ├── user-service.yml
    ├── internal-api-gateway.yml
    └── external-api-gateway.yml
```

## Client Configuration

Services connect to Config Server using:

```yaml
spring:
  application:
    name: order-service
  config:
    import: optional:configserver:http://localhost:8888
  cloud:
    config:
      uri: http://config-server:8888
      fail-fast: true
      retry:
        max-attempts: 6
        initial-interval: 1000
```

## Development

### Build

```bash
cd config-server
mvn clean package
```

### Run Locally

```bash
mvn spring-boot:run
```

### Run with Docker

```bash
docker-compose up -d config-server
```

## Testing Configuration

### Retrieve Order Service Config

```bash
curl http://localhost:8888/order-service/default
```

### Retrieve Docker Profile Config

```bash
curl http://localhost:8888/order-service/docker
```

### Test YAML Format

```bash
curl http://localhost:8888/order-service-docker.yml
```

## Common Configuration Properties

The following properties are typically managed through Config Server:

- **Database Connections**: JDBC URLs, credentials
- **Kafka Broker URLs**: Kafka bootstrap servers
- **Eureka Server URLs**: Service discovery endpoints
- **Keycloak Settings**: OAuth2 issuer URIs, client credentials
- **Feature Flags**: Enable/disable features per environment
- **Logging Levels**: Adjust log verbosity per service
- **Timeouts and Retry Policies**: Circuit breaker configurations

## Refresh Configuration at Runtime

### Manual Refresh (requires Spring Cloud Actuator)

```bash
# Enable refresh endpoint in client service
management:
  endpoints:
    web:
      exposure:
        include: refresh

# Trigger refresh
curl -X POST http://localhost:8089/actuator/refresh
```

### Automatic Refresh (requires Spring Cloud Bus + RabbitMQ/Kafka)

```bash
# Refresh all services
curl -X POST http://localhost:8888/actuator/bus-refresh
```

## Security Considerations

### Encryption

Config Server supports encryption of sensitive properties:

```yaml
# Encrypted value (requires encryption key)
spring:
  datasource:
    password: '{cipher}AQA3eHf8...'
```

### Setup Encryption Key

```yaml
# application.yml
encrypt:
  key: your-encryption-key
```

### Encrypt a Value

```bash
curl http://localhost:8888/encrypt -d "mysecret"
```

### Decrypt a Value

```bash
curl http://localhost:8888/decrypt -d "{cipher}AQA3eHf8..."
```

## Troubleshooting

### Service cannot connect to Config Server
- Ensure Config Server is running: `docker ps | grep config-server`
- Check network connectivity: `curl http://localhost:8888/actuator/health`
- Verify service configuration has correct Config Server URI
- Check Config Server logs: `docker logs config-server`

### Configuration not found
- Verify configuration file exists in `src/main/resources/config/`
- Check file naming: `{application-name}.yml` or `{application-name}-{profile}.yml`
- Ensure `spring.application.name` in client matches configuration file name

### Changes not reflecting
- Configuration is loaded at service startup by default
- Use `/actuator/refresh` endpoint for runtime refresh
- Or restart the service: `docker-compose restart order-service`

## Migration to Git-Based Configuration

To use Git repository instead of native file system:

```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-org/config-repo
          search-paths: '{application}'
          default-label: main
  profiles:
    active: git
```

## Dependencies

- Requires Eureka Server for service registration
- Should start after Eureka but before business services

## Additional Resources

- [Spring Cloud Config Documentation](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/)
- [Externalized Configuration Guide](https://spring.io/guides/gs/centralized-configuration/)
