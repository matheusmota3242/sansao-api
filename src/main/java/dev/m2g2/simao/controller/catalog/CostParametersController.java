package dev.m2g2.simao.controller.catalog;

import dev.m2g2.simao.dto.catalog.CostParametersDTO;
import dev.m2g2.simao.service.catalog.CostParametersService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cost-parameters")
public class CostParametersController {

    private final CostParametersService service;

    public CostParametersController(CostParametersService service) {
        this.service = service;
    }

    @GetMapping
    public CostParametersDTO get() {
        return service.get();
    }

    @PutMapping
    public CostParametersDTO update(@RequestBody CostParametersDTO dto) {
        return service.update(dto);
    }
}
