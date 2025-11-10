package com.eflo.docgen.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * PDF Rendering Service
 *
 * Handles PDF generation from HTML and DOCX templates
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfRenderingService {

    private final TemplateEngine templateEngine;
    private final PlaceholderResolverService placeholderResolver;

    /**
     * Generate PDF from HTML content
     */
    public byte[] generatePdfFromHtml(String htmlContent) throws IOException {
        log.debug("Generating PDF from HTML content (length: {})", htmlContent.length());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(outputStream);
            builder.run();

            log.info("PDF generated successfully (size: {} bytes)", outputStream.size());
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF from HTML", e);
            throw new IOException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Generate PDF from Thymeleaf template
     */
    public byte[] generatePdfFromThymeleafTemplate(String templateName, Map<String, Object> data) throws IOException {
        log.debug("Generating PDF from Thymeleaf template: {}", templateName);

        // Create Thymeleaf context
        Context context = new Context();
        context.setVariables(data);

        // Process template to HTML
        String html = templateEngine.process(templateName, context);

        // Convert HTML to PDF
        return generatePdfFromHtml(html);
    }

    /**
     * Process DOCX template with placeholders
     */
    public byte[] processDocxTemplate(byte[] templateContent, Map<String, Object> data) throws IOException {
        log.debug("Processing DOCX template with {} variables", data.size());

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(templateContent);
             XWPFDocument document = new XWPFDocument(inputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Replace placeholders in all paragraphs
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                replacePlaceholdersInParagraph(paragraph, data);
            }

            // Replace placeholders in tables
            document.getTables().forEach(table -> {
                table.getRows().forEach(row -> {
                    row.getTableCells().forEach(cell -> {
                        cell.getParagraphs().forEach(paragraph -> {
                            replacePlaceholdersInParagraph(paragraph, data);
                        });
                    });
                });
            });

            document.write(outputStream);
            log.info("DOCX processed successfully (size: {} bytes)", outputStream.size());
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Failed to process DOCX template", e);
            throw new IOException("DOCX processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Convert DOCX to PDF
     */
    public byte[] convertDocxToPdf(byte[] docxContent) throws IOException {
        // Note: This is a placeholder. Full DOCX to PDF conversion requires LibreOffice or similar
        // For production, consider using JODConverter with LibreOffice or Apache FOP
        log.warn("DOCX to PDF conversion not fully implemented. Returning DOCX as-is.");
        return docxContent;
    }

    /**
     * Replace placeholders in a paragraph
     */
    private void replacePlaceholdersInParagraph(XWPFParagraph paragraph, Map<String, Object> data) {
        String text = paragraph.getText();
        if (text == null || !text.contains("{{")) {
            return;
        }

        // Resolve all placeholders
        String resolvedText = placeholderResolver.resolvePlaceholders(text, data);

        // Clear existing runs
        while (paragraph.getRuns().size() > 0) {
            paragraph.removeRun(0);
        }

        // Add new run with resolved text
        XWPFRun run = paragraph.createRun();
        run.setText(resolvedText);
    }

    /**
     * Generate HTML content with styling
     */
    public String generateStyledHtml(String content, String title) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>%s</title>
                <style>
                    @page { size: A4; margin: 2cm; }
                    body { font-family: Arial, sans-serif; font-size: 11pt; line-height: 1.6; }
                    h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }
                    h2 { color: #34495e; margin-top: 20px; }
                    table { width: 100%%; border-collapse: collapse; margin: 15px 0; }
                    th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
                    th { background-color: #3498db; color: white; }
                    .footer { margin-top: 30px; font-size: 9pt; color: #7f8c8d; }
                </style>
            </head>
            <body>
                %s
            </body>
            </html>
            """.formatted(title, content);
    }
}
