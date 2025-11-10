package com.eflo.document.scanner;

import com.eflo.document.domain.enums.VirusScanStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

/**
 * ClamAV-based virus scanner implementation.
 * Provides virus scanning capabilities using the ClamAV antivirus engine.
 * Includes circuit breaker pattern for resilience and graceful degradation.
 *
 * <p>Configuration properties:</p>
 * <ul>
 *   <li>clamav.enabled - Enable/disable ClamAV scanning</li>
 *   <li>clamav.host - ClamAV server host</li>
 *   <li>clamav.port - ClamAV server port</li>
 *   <li>clamav.timeout - Connection timeout in milliseconds</li>
 *   <li>clamav.platform - Platform type (unix/windows)</li>
 * </ul>
 *
 * @author Eflo Document Service
 * @version 1.0
 */
@Slf4j
@Component
public class ClamAVScanner {

    private static final String CIRCUIT_BREAKER_NAME = "clamav-scanner";
    private static final String SCANNER_ENGINE = "ClamAV";

    @Value("${clamav.enabled:true}")
    private boolean enabled;

    @Value("${clamav.host:localhost}")
    private String host;

    @Value("${clamav.port:3310}")
    private int port;

    @Value("${clamav.timeout:30000}")
    private int timeout;

    @Value("${clamav.platform:unix}")
    private String platform;

    private ClamAVClient clamAVClient;

    /**
     * Initializes the ClamAV client after bean construction.
     * Creates the client connection based on configured platform and settings.
     */
    @PostConstruct
    public void init() {
        if (!enabled) {
            log.warn("ClamAV scanning is disabled. Virus scans will be skipped.");
            return;
        }

        try {
            InetSocketAddress address = new InetSocketAddress(host, port);

            if ("windows".equalsIgnoreCase(platform)) {
                clamAVClient = new ClamAVClient(host, port, timeout);
            } else {
                clamAVClient = new ClamAVClient(host, port, timeout);
            }

            log.info("ClamAV scanner initialized successfully - Host: {}, Port: {}, Timeout: {}ms",
                    host, port, timeout);

            // Test connectivity
            if (ping()) {
                log.info("ClamAV server is reachable and responding");
            } else {
                log.warn("ClamAV server ping failed. Service may be unavailable.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize ClamAV client: {}. Scanning will be skipped.", e.getMessage());
            clamAVClient = null;
        }
    }

    /**
     * Scans an input stream for viruses using ClamAV.
     * Includes circuit breaker protection to prevent cascading failures.
     *
     * @param inputStream the input stream to scan
     * @param fileName    the name of the file being scanned
     * @return ScanResult containing scan status and details
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "scanFallback")
    public ScanResult scanWithClamAV(InputStream inputStream, String fileName) {
        long startTime = System.currentTimeMillis();

        log.debug("Starting ClamAV scan for file: {}", fileName);

        if (!enabled) {
            log.debug("ClamAV is disabled, skipping scan for file: {}", fileName);
            return ScanResult.skipped(fileName, "ClamAV scanning is disabled");
        }

        if (!isClamAVAvailable()) {
            log.warn("ClamAV is not available, skipping scan for file: {}", fileName);
            return ScanResult.skipped(fileName, "ClamAV service is unavailable");
        }

        try {
            byte[] scanResult = clamAVClient.scan(inputStream);
            long duration = System.currentTimeMillis() - startTime;

            ScanResult result;

            if (clamAVClient.isCleanReply(scanResult)) {
                log.info("ClamAV scan completed - File: {} is CLEAN (duration: {}ms)", fileName, duration);
                result = ScanResult.clean(fileName);
            } else {
                String threat = new String(scanResult).trim();
                log.warn("ClamAV scan completed - File: {} is INFECTED with: {} (duration: {}ms)",
                        fileName, threat, duration);
                result = ScanResult.infected(fileName, threat);
            }

            // Add scan metadata
            result.setScanDurationMs(duration);
            result.setScannerEngine(SCANNER_ENGINE);
            result.setScannerVersion(getVersion());
            result.addMetadata("clamav_host", host);
            result.addMetadata("clamav_port", port);

            return result;

        } catch (IOException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("ClamAV scan failed for file: {} - Error: {} (duration: {}ms)",
                    fileName, e.getMessage(), duration, e);

            ScanResult result = ScanResult.failed(fileName, "IOException: " + e.getMessage());
            result.setScanDurationMs(duration);
            result.setScannerEngine(SCANNER_ENGINE);
            result.addMetadata("error", e.getClass().getName());
            result.addMetadata("error_message", e.getMessage());

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Unexpected error during ClamAV scan for file: {} - Error: {} (duration: {}ms)",
                    fileName, e.getMessage(), duration, e);

            ScanResult result = ScanResult.failed(fileName, "Unexpected error: " + e.getMessage());
            result.setScanDurationMs(duration);
            result.setScannerEngine(SCANNER_ENGINE);
            result.addMetadata("error", e.getClass().getName());
            result.addMetadata("error_message", e.getMessage());

            return result;
        }
    }

    /**
     * Fallback method for circuit breaker when ClamAV is unavailable.
     * Returns a SKIPPED scan result to allow graceful degradation.
     *
     * @param inputStream the input stream to scan
     * @param fileName    the name of the file being scanned
     * @param throwable   the exception that triggered the fallback
     * @return ScanResult with SKIPPED status
     */
    @SuppressWarnings("unused")
    private ScanResult scanFallback(InputStream inputStream, String fileName, Throwable throwable) {
        log.warn("ClamAV circuit breaker activated for file: {} - Reason: {}",
                fileName, throwable.getMessage());

        ScanResult result = ScanResult.skipped(fileName,
                "ClamAV service unavailable - Circuit breaker open");
        result.setScannerEngine(SCANNER_ENGINE);
        result.addMetadata("circuit_breaker", "open");
        result.addMetadata("fallback_reason", throwable.getMessage());

        return result;
    }

    /**
     * Checks if ClamAV service is available and responding.
     * Performs a health check by pinging the ClamAV server.
     *
     * @return true if ClamAV is available, false otherwise
     */
    public boolean isClamAVAvailable() {
        if (!enabled || clamAVClient == null) {
            return false;
        }

        try {
            return ping();
        } catch (Exception e) {
            log.debug("ClamAV availability check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Performs a ping test to check ClamAV server connectivity.
     * Sends a PING command and expects a PONG response.
     *
     * @return true if ping successful, false otherwise
     */
    public boolean ping() {
        if (!enabled || clamAVClient == null) {
            return false;
        }

        try {
            boolean pingResult = clamAVClient.ping();
            boolean success = pingResult ||
                            false;

            if (success) {
                log.debug("ClamAV ping successful - Host: {}, Port: {}", host, port);
            } else {
                log.debug("ClamAV ping failed");
            }

            return success;
        } catch (Exception e) {
            log.debug("ClamAV ping failed - Unexpected error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Gets the version of the ClamAV engine.
     * Queries the ClamAV server for version information.
     *
     * @return version string or "unknown" if unavailable
     */
    public String getVersion() {
        if (!enabled || clamAVClient == null) {
            return "unknown";
        }

        try {
            String versionInfo = clamAVClient.stats();
            String version = versionInfo.split("\n")[0].trim();
            log.debug("ClamAV version: {}", version);
            return version;
        } catch (Exception e) {
            log.debug("Failed to get ClamAV version: {}", e.getMessage());
            return "unknown";
        }
    }

    /**
     * Reloads the ClamAV virus database.
     * Triggers a database update on the ClamAV server.
     *
     * @return true if reload successful, false otherwise
     */
    public boolean reloadDatabase() {
        if (!enabled || clamAVClient == null) {
            log.warn("Cannot reload ClamAV database - ClamAV is not available");
            return false;
        }

        try {
            clamAVClient.reload();
            log.info("ClamAV database reload initiated successfully");
            return true;
        } catch (Exception e) {
            log.error("Failed to reload ClamAV database: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Gets the current ClamAV configuration details.
     *
     * @return configuration information as a string
     */
    public String getConfiguration() {
        return String.format("ClamAV Configuration - Enabled: %s, Host: %s, Port: %d, Timeout: %dms, Platform: %s",
                enabled, host, port, timeout, platform);
    }

    /**
     * Checks if ClamAV scanning is enabled.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
}
