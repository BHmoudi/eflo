package com.eflo.document.integration;

import com.eflo.document.exception.ResourceNotFoundException;
import com.eflo.document.exception.ServiceCommunicationException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * Custom error decoder for Feign clients.
 * Converts HTTP error responses into appropriate application exceptions.
 *
 * <p>This decoder handles common HTTP error codes and maps them to domain-specific exceptions,
 * enabling better error handling and user feedback.</p>
 *
 * @author Document Service
 * @version 1.0
 */
@Slf4j
public class FeignClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    /**
     * Decodes an error response from a Feign client call.
     *
     * @param methodKey the method that was called
     * @param response the error response
     * @return an appropriate exception based on the response status
     */
    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        String serviceName = extractServiceName(methodKey);

        log.error("Feign client error: method={}, status={}, service={}, reason={}",
            methodKey, status, serviceName, response.reason());

        switch (status) {
            case NOT_FOUND:
                return new ResourceNotFoundException(
                    String.format("Resource not found in %s: %s", serviceName, response.reason())
                );

            case BAD_REQUEST:
                return new ServiceCommunicationException(
                    String.format("Bad request to %s: %s", serviceName, response.reason()),
                    status.value()
                );

            case UNAUTHORIZED:
            case FORBIDDEN:
                return new ServiceCommunicationException(
                    String.format("Authentication/Authorization failed for %s: %s", serviceName, response.reason()),
                    status.value()
                );

            case SERVICE_UNAVAILABLE:
            case GATEWAY_TIMEOUT:
                return new ServiceCommunicationException(
                    String.format("Service %s is currently unavailable: %s", serviceName, response.reason()),
                    status.value()
                );

            case INTERNAL_SERVER_ERROR:
                return new ServiceCommunicationException(
                    String.format("Internal error in %s: %s", serviceName, response.reason()),
                    status.value()
                );

            default:
                return defaultDecoder.decode(methodKey, response);
        }
    }

    /**
     * Extracts the service name from the Feign method key.
     *
     * @param methodKey the method key
     * @return the service name
     */
    private String extractServiceName(String methodKey) {
        if (methodKey == null || !methodKey.contains("#")) {
            return "unknown-service";
        }

        String className = methodKey.substring(0, methodKey.indexOf("#"));
        if (className.contains(".")) {
            className = className.substring(className.lastIndexOf(".") + 1);
        }

        return className.replace("Client", "").toLowerCase();
    }
}
