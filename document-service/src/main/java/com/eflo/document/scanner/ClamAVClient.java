package com.eflo.document.scanner;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Simple ClamAV client implementation using socket communication.
 * This is a lightweight alternative to the external fi.solita.clamav library.
 */
@Slf4j
public class ClamAVClient {

    private final String host;
    private final int port;
    private final int timeout;

    public ClamAVClient(String host, int port, int timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    /**
     * Scan an input stream for viruses.
     *
     * @param inputStream The stream to scan
     * @return Scan result bytes
     * @throws IOException if communication fails
     */
    public byte[] scan(InputStream inputStream) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            socket.setSoTimeout(timeout);

            try (OutputStream os = socket.getOutputStream();
                 InputStream is = socket.getInputStream()) {

                // Send INSTREAM command
                os.write("zINSTREAM\0".getBytes(StandardCharsets.US_ASCII));
                os.flush();

                // Send file data in chunks
                byte[] buffer = new byte[2048];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    // Send chunk size (4 bytes, big endian)
                    os.write(new byte[]{
                        (byte) (bytesRead >>> 24),
                        (byte) (bytesRead >>> 16),
                        (byte) (bytesRead >>> 8),
                        (byte) bytesRead
                    });
                    // Send chunk data
                    os.write(buffer, 0, bytesRead);
                }

                // Send zero-length chunk to indicate end
                os.write(new byte[]{0, 0, 0, 0});
                os.flush();

                // Read response
                ByteArrayOutputStream response = new ByteArrayOutputStream();
                int b;
                while ((b = is.read()) != -1) {
                    response.write(b);
                }

                return response.toByteArray();
            }
        }
    }

    /**
     * Ping the ClamAV server to check if it's available.
     *
     * @return true if server responds to PING
     */
    public boolean ping() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            socket.setSoTimeout(timeout);

            try (OutputStream os = socket.getOutputStream();
                 InputStream is = socket.getInputStream()) {

                // Send PING command
                os.write("zPING\0".getBytes(StandardCharsets.US_ASCII));
                os.flush();

                // Read response (should be "PONG")
                byte[] response = new byte[4];
                int bytesRead = is.read(response);

                return bytesRead == 4 && new String(response).equals("PONG");
            }
        } catch (IOException e) {
            log.debug("ClamAV ping failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get ClamAV version.
     *
     * @return version string or null if unavailable
     */
    public String getVersion() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            socket.setSoTimeout(timeout);

            try (OutputStream os = socket.getOutputStream();
                 InputStream is = socket.getInputStream()) {

                // Send VERSION command
                os.write("zVERSION\0".getBytes(StandardCharsets.US_ASCII));
                os.flush();

                // Read response
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                return reader.readLine();
            }
        } catch (IOException e) {
            log.debug("Failed to get ClamAV version: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if scan result indicates clean file.
     *
     * @param result Scan result bytes
     * @return true if file is clean
     */
    public boolean isCleanReply(byte[] result) {
        if (result == null || result.length == 0) {
            return false;
        }
        String response = new String(result, StandardCharsets.UTF_8).trim();
        return response.contains("OK") && !response.contains("FOUND");
    }

    /**
     * Get statistics (stub for compatibility).
     *
     * @return empty stats string
     */
    public String stats() {
        return "ClamAV stats not available";
    }

    /**
     * Reload database (stub for compatibility).
     */
    public void reload() {
        log.debug("ClamAV reload requested - not implemented");
    }
}
