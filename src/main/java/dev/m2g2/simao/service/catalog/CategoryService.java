package dev.m2g2.simao.service.catalog;

import dev.m2g2.simao.dto.catalog.CategoryRequest;
import dev.m2g2.simao.dto.catalog.CategoryResponse;
import dev.m2g2.simao.model.catalog.Category;
import dev.m2g2.simao.repository.CategoryRepository;
import dev.m2g2.simao.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository repository, ProductRepository productRepository) {
        this.repository = repository;
        this.productRepository = productRepository;
    }

    public List<CategoryResponse> list() {
        return repository.findAllByActiveTrueOrderByCodeAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse create(CategoryRequest request) {
        String code = normalizeCode(request.code());
        String name = requireText(request.name(), "Nome da categoria é obrigatório.");
        if (repository.existsByCode(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe uma categoria com o código %s.".formatted(code));
        }
        Category category = new Category();
        category.setCode(code);
        category.setName(name);
        LocalDateTime now = LocalDateTime.now();
        category.setCreatedAt(now);
        category.setUpdatedAt(now);
        category.setActive(true);
        return toResponse(repository.save(category));
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = repository.findById(id)
                .filter(Category::isActive)
                .orElseThrow(this::notFound);
        // Code is the SKU prefix and identity; only the name is editable here.
        category.setName(requireText(request.name(), "Nome da categoria é obrigatório."));
        category.setUpdatedAt(LocalDateTime.now());
        return toResponse(repository.save(category));
    }

    public void delete(Long id) {
        Category category = repository.findById(id)
                .filter(Category::isActive)
                .orElseThrow(this::notFound);
        if (productRepository.existsByCategoryIdAndActiveTrue(category.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A categoria possui produtos ativos e não pode ser removida.");
        }
        category.setActive(false);
        category.setUpdatedAt(LocalDateTime.now());
        repository.save(category);
    }

    private String normalizeCode(String code) {
        String c = requireText(code, "Código da categoria é obrigatório.").toUpperCase();
        if (!c.matches("^[A-Z0-9]{2,10}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Código inválido. Use 2 a 10 letras/números (ex: MOL).");
        }
        return c;
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada.");
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getCode(), c.getName());
    }
}
