package dev.m2g2.simao.service;

import dev.m2g2.simao.enums.OrderStatus;
import dev.m2g2.simao.model.Customer;
import dev.m2g2.simao.repository.CustomerRepository;
import dev.m2g2.simao.repository.PrintOrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerService {

    private static final List<OrderStatus> QUEUE_STATUSES = List.of(OrderStatus.WAITING, OrderStatus.RUNNING);

    private final CustomerRepository repository;
    private final PrintOrderRepository printOrderRepository;

    public CustomerService(CustomerRepository repository, PrintOrderRepository printOrderRepository) {
        this.repository = repository;
        this.printOrderRepository = printOrderRepository;
    }

    public List<Customer> list() {
        return repository.findAllByActiveTrueOrderByNameAsc();
    }

    public Customer get(Long id) {
        return repository.findById(id)
                .filter(Customer::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cliente com id %d não encontrado.".formatted(id)));
    }

    /** Registers a customer, rejecting a name that is already taken. */
    @Transactional
    public Customer register(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o nome do cliente.");
        if (repository.findFirstByNameIgnoreCaseAndActiveTrue(trimmed).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe um cliente chamado %s.".formatted(trimmed));
        return create(trimmed);
    }

    @Transactional
    public Customer create(String name) {
        Customer customer = new Customer();
        customer.setName(name.trim());
        LocalDateTime now = LocalDateTime.now();
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);
        customer.setActive(true);
        return repository.save(customer);
    }

    @Transactional
    public Customer update(Long id, String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o nome do cliente.");
        Customer customer = get(id);
        customer.setName(trimmed);
        return repository.save(customer);
    }

    /**
     * Deactivates a customer. Deliberately a soft delete: print_order carries a
     * foreign key to customer, so removing the row would either fail or orphan
     * the order history. Refuses while the customer still has orders in the
     * queue, so nothing in progress loses its owner.
     */
    @Transactional
    public void delete(Long id) {
        Customer customer = get(id);
        long queued = printOrderRepository
                .findAllByActiveTrueAndStatusInOrderByPriorityAsc(QUEUE_STATUSES)
                .stream()
                .filter(order -> order.getCustomer() != null && id.equals(order.getCustomer().getId()))
                .count();
        if (queued > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cliente %s tem %d pedido(s) na fila. Encerre ou remova antes."
                            .formatted(customer.getName(), queued));
        customer.setActive(false);
        repository.save(customer);
    }

    /**
     * Resolves the customer used when registering an order, from either an id or
     * a name:
     *
     * <ul>
     *   <li>All-digit input is read as an id and looked up directly. Returns null
     *       when no active customer has it — an id the user made up must fail
     *       loudly instead of silently becoming a customer named "12".</li>
     *   <li>Anything else is matched against active customers ignoring case and
     *       surrounding blanks, and a new customer is created when nothing
     *       matches. That is what lets order intake ask for the customer in a
     *       single question instead of requiring registration first.</li>
     * </ul>
     *
     * <p>Name matching is exact apart from case: "joão silva" finds "João Silva",
     * but "Joao" without the accent does not, and creates a second customer.
     * The trade-off of the id shortcut is that a customer whose name is only
     * digits can no longer be created by typing it.
     */
    public Customer resolveByNameOrId(String input) {
        String trimmed = input.trim();
        if (trimmed.matches("\\d+")) {
            try {
                return repository.findById(Long.parseLong(trimmed))
                        .filter(Customer::isActive)
                        .orElse(null);
            } catch (NumberFormatException e) {
                // Digits beyond long range: fall through and treat as a name.
                return resolveByName(trimmed);
            }
        }
        return resolveByName(trimmed);
    }

    private Customer resolveByName(String name) {
        String trimmed = name.trim();
        return repository.findFirstByNameIgnoreCaseAndActiveTrue(trimmed)
                .orElseGet(() -> create(trimmed));
    }
}
