package dev.m2g2.simao.controller.management;

import dev.m2g2.simao.dto.PurchaseDTO;
import dev.m2g2.simao.dto.management.PurchaseRequest;
import dev.m2g2.simao.dto.management.PurchaseResponse;
import dev.m2g2.simao.service.PurchaseService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Compras de insumo são gasto: é informação de custo, então ficam só com o
 * ADMIN, na mesma linha de /api/cost-parameters.
 */
@RestController
@RequestMapping("/api/purchases")
@PreAuthorize("hasRole('ADMIN')")
public class PurchaseController {

    private final PurchaseService service;

    public PurchaseController(PurchaseService service) {
        this.service = service;
    }

    @GetMapping
    public List<PurchaseResponse> list() {
        return service.list().stream().map(PurchaseResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PurchaseResponse get(@PathVariable Long id) {
        return PurchaseResponse.from(service.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse create(@RequestBody PurchaseRequest request) {
        return PurchaseResponse.from(service.create(toDto(request)));
    }

    @PutMapping("/{id}")
    public PurchaseResponse update(@PathVariable Long id, @RequestBody PurchaseRequest request) {
        return PurchaseResponse.from(service.update(id, toDto(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    private PurchaseDTO toDto(PurchaseRequest request) {
        PurchaseDTO dto = new PurchaseDTO();
        dto.setDescription(request.description());
        dto.setAmount(request.amount());
        dto.setUnitPrice(request.unitPrice());
        dto.setSource(request.source());
        dto.setPurchasedAt(request.purchasedAt());
        dto.setObservations(request.observations());
        return dto;
    }
}
