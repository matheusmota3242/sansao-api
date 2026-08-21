package dev.m2g2.simao.controller.catalog;

import dev.m2g2.simao.dto.catalog.StoreConfigDTO;
import dev.m2g2.simao.service.catalog.StoreConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store")
public class StoreConfigController {

    private final StoreConfigService service;

    public StoreConfigController(StoreConfigService service) {
        this.service = service;
    }

    @GetMapping
    public StoreConfigDTO get() {
        return service.get();
    }

    @PutMapping
    public StoreConfigDTO update(@RequestBody StoreConfigDTO dto) {
        return service.update(dto);
    }
}
