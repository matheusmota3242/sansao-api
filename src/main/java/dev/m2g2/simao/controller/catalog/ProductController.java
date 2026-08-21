package dev.m2g2.simao.controller.catalog;

import dev.m2g2.simao.dto.catalog.ProductRequest;
import dev.m2g2.simao.dto.catalog.ProductResponse;
import dev.m2g2.simao.model.catalog.ProductStatus;
import dev.m2g2.simao.service.catalog.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String cat,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "sku") String sort) {
        ProductStatus st = (status == null || status.isBlank()) ? null : ProductStatus.from(status);
        return service.list(q, cat, st, sort);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@RequestBody ProductRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @RequestBody ProductRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/duplicate")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse duplicate(@PathVariable Long id) {
        return service.duplicate(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
