package dev.m2g2.simao.service;

import dev.m2g2.simao.dto.PurchaseDTO;
import dev.m2g2.simao.model.Purchase;
import dev.m2g2.simao.repository.PurchaseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PurchaseService {

    private final PurchaseRepository repository;

    public PurchaseService(PurchaseRepository repository) {
        this.repository = repository;
    }

    /** Supply purchases, newest first. */
    public List<Purchase> list() {
        return repository.findAllByActiveTrueOrderByCreatedAtDesc();
    }

    public Purchase get(Long id) {
        return repository.findById(id)
                .filter(Purchase::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Compra com id %d não encontrada.".formatted(id)));
    }

    @Transactional
    public Purchase create(PurchaseDTO dto) {
        Purchase purchase = new Purchase();
        apply(purchase, dto);
        LocalDateTime now = LocalDateTime.now();
        purchase.setCreatedAt(now);
        purchase.setUpdatedAt(now);
        purchase.setActive(true);
        return repository.save(purchase);
    }

    @Transactional
    public Purchase update(Long id, PurchaseDTO dto) {
        Purchase purchase = get(id);
        apply(purchase, dto);
        return repository.save(purchase);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Compra com id %d não encontrada.".formatted(id));
        repository.deleteById(id);
    }

    private void apply(Purchase purchase, PurchaseDTO dto) {
        purchase.setDescription(dto.getDescription());
        purchase.setAmount(dto.getAmount());
        purchase.setUnitPrice(dto.getUnitPrice());
        purchase.setSource(dto.getSource());
        purchase.setPurchasedAt(dto.getPurchasedAt());
        purchase.setObservations(dto.getObservations());
    }
}
