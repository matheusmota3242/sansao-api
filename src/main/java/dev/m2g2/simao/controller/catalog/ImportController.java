package dev.m2g2.simao.controller.catalog;

import dev.m2g2.simao.dto.catalog.ImportRequest;
import dev.m2g2.simao.dto.catalog.ImportResult;
import dev.m2g2.simao.service.catalog.ImportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final ImportService service;

    public ImportController(ImportService service) {
        this.service = service;
    }

    // Accepts a full argilalab.json export and upserts it (idempotent).
    @PostMapping
    public ImportResult importProject(@RequestBody ImportRequest request) {
        return service.importProject(request);
    }
}
