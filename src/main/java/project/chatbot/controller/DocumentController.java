package project.chatbot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import project.chatbot.service.RagService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    private final RagService ragService;

    public DocumentController(RagService ragService) {
        this.ragService = ragService;
    }

@PostMapping("/upload")
public ResponseEntity<String> upload(@RequestParam("files") List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
        return ResponseEntity.badRequest().body("Please select at least one PDF file to upload.");
    }

    List<String> success = new ArrayList<>();
    List<String> failed = new ArrayList<>();

    for (MultipartFile file : files) {
        if (file.isEmpty()) {
            failed.add(file.getOriginalFilename() + " (empty)");
            continue;
        }
        try {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            ragService.ingest(resource);
            success.add(file.getOriginalFilename());
        } catch (IOException e) {
            log.error("Failed to process file {}: {}", file.getOriginalFilename(), e.getMessage());
            failed.add(file.getOriginalFilename());
        }
    }

    StringBuilder result = new StringBuilder();
    if (!success.isEmpty()) {
        result.append("✓ Uploaded: ").append(String.join(", ", success));
    }
    if (!failed.isEmpty()) {
        if (!result.isEmpty()) result.append("\n");
        result.append("✗ Failed: ").append(String.join(", ", failed));
    }

    return success.isEmpty()
        ? ResponseEntity.internalServerError().body(result.toString())
        : ResponseEntity.ok(result.toString());
}
}