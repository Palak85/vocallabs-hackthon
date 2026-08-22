package com.raj.document_qna_assistant.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class TextExtractor {

    public record ExtractedPage(Integer pageNumber, String content) {}

    public List<ExtractedPage> extractText(byte[] bytes, String contentType, String filename) throws IOException {
        String ext = getFileExtension(filename).toLowerCase();
        
        if ("pdf".equals(ext) || (contentType != null && contentType.contains("pdf"))) {
            return extractPdf(bytes);
        } else if ("docx".equals(ext) || (contentType != null && contentType.contains("officedocument.wordprocessingml"))) {
            return extractDocx(bytes);
        } else if ("txt".equals(ext) || "md".equals(ext) || (contentType != null && (contentType.contains("text") || contentType.contains("markdown")))) {
            return extractPlainText(bytes);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + ext);
        }
    }

    private List<ExtractedPage> extractPdf(byte[] bytes) throws IOException {
        List<ExtractedPage> pages = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int pageCount = document.getNumberOfPages();
            for (int i = 1; i <= pageCount; i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String text = stripper.getText(document);
                pages.add(new ExtractedPage(i, text != null ? text.trim() : ""));
            }
        }
        return pages;
    }

    private List<ExtractedPage> extractDocx(byte[] bytes) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            String text = extractor.getText();
            List<ExtractedPage> pages = new ArrayList<>();
            pages.add(new ExtractedPage(1, text != null ? text.trim() : ""));
            return pages;
        }
    }

    private List<ExtractedPage> extractPlainText(byte[] bytes) {
        String text = new String(bytes, StandardCharsets.UTF_8);
        List<ExtractedPage> pages = new ArrayList<>();
        pages.add(new ExtractedPage(1, text.trim()));
        return pages;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
