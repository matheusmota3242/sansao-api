package dev.m2g2.simao.service;

import dev.m2g2.simao.dto.OrderDTO;
import dev.m2g2.simao.enums.OrderStatus;
import dev.m2g2.simao.model.Customer;
import dev.m2g2.simao.model.PrintOrder;
import dev.m2g2.simao.repository.PrintOrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private static final List<OrderStatus> QUEUE_STATUSES = List.of(OrderStatus.WAITING, OrderStatus.RUNNING);
    private static final List<OrderStatus> CLOSED_STATUSES = List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED);

    private final PrintOrderRepository repository;
    private final CustomerService customerService;

    public OrderService(PrintOrderRepository repository, CustomerService customerService) {
        this.repository = repository;
        this.customerService = customerService;
    }

    /** Orders still waiting or running, in printing order. */
    public List<PrintOrder> queue() {
        return repository.findAllByActiveTrueAndStatusInOrderByPriorityAsc(QUEUE_STATUSES);
    }

    /** Orders that already left the queue, newest first. */
    public List<PrintOrder> closed() {
        return repository.findAllByActiveTrueAndStatusInOrderByIdDesc(CLOSED_STATUSES);
    }

    public PrintOrder get(Long id) {
        return repository.findById(id)
                .filter(PrintOrder::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pedido com id %d não encontrado.".formatted(id)));
    }

    @Transactional
    public PrintOrder create(OrderDTO dto) {
        return create(dto, resolveCustomer(dto.getCustomerName(), true));
    }

    @Transactional
    public PrintOrder create(OrderDTO dto, Customer customer) {
        PrintOrder order = new PrintOrder();
        order.setDescription(dto.getDescription());
        order.setCustomer(customer);
        order.setPrintTimeMinutes(dto.getPrintTimeMinutes());
        order.setProductionCost(dto.getProductionCost());
        order.setSalePrice(dto.getSalePrice());
        order.setObservations(dto.getObservations());
        order.setStatus(OrderStatus.WAITING);
        // New orders join the back of the queue; use move() to pull them up.
        order.setPriority(queue().size() + 1);
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setActive(true);
        return repository.save(order);
    }

    @Transactional
    public PrintOrder update(Long id, OrderDTO dto) {
        PrintOrder order = get(id);
        Customer customer = resolveCustomer(dto.getCustomerName(), false);
        order.setDescription(dto.getDescription());
        if (customer != null)
            order.setCustomer(customer);
        order.setPrintTimeMinutes(dto.getPrintTimeMinutes());
        order.setProductionCost(dto.getProductionCost());
        order.setSalePrice(dto.getSalePrice());
        order.setObservations(dto.getObservations());
        return repository.save(order);
    }

    /**
     * Resolves the order's customer. An id that matches nothing is an error
     * rather than a new customer named after the digits — see
     * {@link CustomerService#resolveByNameOrId(String)}.
     */
    private Customer resolveCustomer(String nameOrId, boolean required) {
        if (nameOrId == null || nameOrId.isBlank()) {
            if (required)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o cliente do pedido.");
            return null;
        }
        Customer customer = customerService.resolveByNameOrId(nameOrId);
        if (customer == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Cliente com id %s não encontrado.".formatted(nameOrId.trim()));
        return customer;
    }

    /**
     * Moves an order to a new position in the queue, shifting the others so the
     * sequence stays contiguous.
     */
    @Transactional
    public PrintOrder move(Long id, int position) {
        List<PrintOrder> pending = queue();
        PrintOrder order = pending.stream().filter(o -> o.getId().equals(id)).findFirst().orElse(null);
        if (order == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Pedido com id %d não está na fila.".formatted(id));
        if (position < 1 || position > pending.size())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Posição inválida. A fila tem %d pedido(s).".formatted(pending.size()));

        pending.remove(order);
        pending.add(position - 1, order);
        renumber(pending);
        return order;
    }

    /**
     * Changes the execution status. Leaving the queue frees the position and
     * renumbers the rest; RUNNING stamps started_at, and going back to WAITING
     * clears it.
     */
    @Transactional
    public PrintOrder changeStatus(Long id, OrderStatus status) {
        if (status == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Status inválido. Use WAITING, RUNNING, COMPLETED ou CANCELLED.");

        PrintOrder order = get(id);
        boolean wasInQueue = order.getStatus().isInQueue();
        order.setStatus(status);

        if (status == OrderStatus.RUNNING && order.getStartedAt() == null)
            order.setStartedAt(LocalDateTime.now());
        if (status == OrderStatus.WAITING)
            order.setStartedAt(null);

        if (!status.isInQueue()) {
            order.setPriority(null);
            repository.save(order);
            renumber(queue());
        } else if (!wasInQueue) {
            // Coming back into the queue: rejoin at the back.
            order.setPriority(queue().size() + 1);
            repository.save(order);
        } else {
            repository.save(order);
        }
        return order;
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Pedido com id %d não encontrado.".formatted(id));
        repository.deleteById(id);
        renumber(queue());
    }

    /**
     * Rewrites positions so the queue stays 1..n with no gaps, which is what the
     * sequential-position model promises.
     */
    private void renumber(List<PrintOrder> ordered) {
        for (int i = 0; i < ordered.size(); i++) {
            PrintOrder order = ordered.get(i);
            int position = i + 1;
            if (!Integer.valueOf(position).equals(order.getPriority())) {
                order.setPriority(position);
                repository.save(order);
            }
        }
    }
}
