package dev.m2g2.simao.controller.management;

import dev.m2g2.simao.dto.OrderDTO;
import dev.m2g2.simao.dto.management.OrderMoveRequest;
import dev.m2g2.simao.dto.management.OrderRequest;
import dev.m2g2.simao.dto.management.OrderResponse;
import dev.m2g2.simao.dto.management.OrderStatusRequest;
import dev.m2g2.simao.model.PrintOrder;
import dev.m2g2.simao.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    /** A fila de impressão, na ordem em que será impressa. */
    @GetMapping
    public List<OrderResponse> queue(Authentication auth) {
        return map(service.queue(), auth);
    }

    /** Pedidos já encerrados (concluídos ou cancelados), mais recentes primeiro. */
    @GetMapping("/closed")
    public List<OrderResponse> closed(Authentication auth) {
        return map(service.closed(), auth);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id, Authentication auth) {
        return OrderResponse.from(service.get(id), canSeeCosts(auth));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@RequestBody OrderRequest request, Authentication auth) {
        return OrderResponse.from(service.create(toDto(request)), canSeeCosts(auth));
    }

    @PutMapping("/{id}")
    public OrderResponse update(@PathVariable Long id, @RequestBody OrderRequest request,
                                Authentication auth) {
        return OrderResponse.from(service.update(id, toDto(request)), canSeeCosts(auth));
    }

    /** Move o pedido na fila; os outros deslizam para manter 1..n sem buracos. */
    @PutMapping("/{id}/position")
    public OrderResponse move(@PathVariable Long id, @RequestBody OrderMoveRequest request,
                              Authentication auth) {
        return OrderResponse.from(service.move(id, request.position() == null ? 0 : request.position()),
                canSeeCosts(auth));
    }

    @PutMapping("/{id}/status")
    public OrderResponse changeStatus(@PathVariable Long id, @RequestBody OrderStatusRequest request,
                                      Authentication auth) {
        return OrderResponse.from(service.changeStatus(id, request.status()), canSeeCosts(auth));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    private List<OrderResponse> map(List<PrintOrder> orders, Authentication auth) {
        boolean withCosts = canSeeCosts(auth);
        return orders.stream().map(o -> OrderResponse.from(o, withCosts)).toList();
    }

    /**
     * Custo e lucro são do ADMIN. O OPERATOR recebe o pedido sem esses campos —
     * omitidos aqui, e não escondidos só na tela, senão bastaria abrir o
     * DevTools para lê-los.
     */
    private boolean canSeeCosts(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private OrderDTO toDto(OrderRequest request) {
        OrderDTO dto = new OrderDTO();
        dto.setDescription(request.description());
        dto.setCustomerName(request.customer());
        dto.setPrintTimeMinutes(request.printTimeMinutes());
        dto.setProductionCost(request.productionCost());
        dto.setSalePrice(request.salePrice());
        dto.setObservations(request.observations());
        return dto;
    }
}
