package dev.m2g2.simao.service;

import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.enums.OrderStatus;
import dev.m2g2.simao.model.Customer;
import dev.m2g2.simao.repository.CustomerRepository;
import dev.m2g2.simao.repository.PrintOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository repository;
    private final PrintOrderRepository printOrderRepository;

    public CustomerService(CustomerRepository repository, PrintOrderRepository printOrderRepository) {
        this.repository = repository;
        this.printOrderRepository = printOrderRepository;
    }

    public Customer create(String name) {
        Customer customer = new Customer();
        customer.setName(name.trim());
        LocalDateTime now = LocalDateTime.now();
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);
        customer.setActive(true);
        return repository.save(customer);
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

    /**
     * Registers a customer up front. Usage: {@code @ccli <nome>}. A single field
     * does not justify an Interaction, so the name comes inline.
     */
    public String createIf(String incomingMessage) {
        if (!incomingMessage.toLowerCase().startsWith(ChatType.CREATE_CUSTOMER.getValue()))
            return null;

        String name = incomingMessage.trim().substring(ChatType.CREATE_CUSTOMER.getValue().length()).trim();
        if (name.isBlank())
            return "Uso: @ccli <nome>";

        if (repository.findFirstByNameIgnoreCaseAndActiveTrue(name).isPresent())
            return "Já existe um cliente chamado *%s*.".formatted(name);

        Customer customer = create(name);
        return "Cliente *%s* cadastrado com id %d!".formatted(customer.getName(), customer.getId());
    }

    public String listIf(String incomingMessage) {
        if (!incomingMessage.equalsIgnoreCase(ChatType.LIST_CUSTOMERS.getValue()))
            return null;

        List<Customer> customers = repository.findAllByActiveTrueOrderByNameAsc();
        if (customers.isEmpty())
            return "Nenhum cliente cadastrado!";

        StringBuilder builder = new StringBuilder("Clientes:\n\n");
        for (Customer customer : customers)
            builder.append("*%d* - %s\n".formatted(customer.getId(), customer.getName()));
        return builder.toString().trim();
    }

    /**
     * Renames a customer. Usage: {@code @ucli <id> <novo nome>}.
     */
    public String updateIf(String incomingMessage) {
        if (!incomingMessage.toLowerCase().startsWith(ChatType.UPDATE_CUSTOMER.getValue()))
            return null;

        String[] parts = incomingMessage.trim().split("\\s+", 3);
        if (parts.length < 3 || parts[2].isBlank())
            return "Uso: @ucli <id> <novo nome>";

        Long id;
        try {
            id = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return "Id inválido. Tente novamente.";
        }

        Customer customer = repository.findById(id).filter(Customer::isActive).orElse(null);
        if (customer == null)
            return "Cliente com id %d não encontrado.".formatted(id);

        String newName = parts[2].trim();
        customer.setName(newName);
        repository.save(customer);
        return "Cliente %d renomeado para *%s*.".formatted(id, newName);
    }

    /**
     * Deactivates a customer. Deliberately a soft delete: print_order carries a
     * foreign key to customer, so removing the row would either fail or orphan
     * the order history.
     */
    public String deleteIf(String incomingMessage) {
        if (!incomingMessage.toLowerCase().startsWith(ChatType.DELETE_CUSTOMER.getValue()))
            return null;

        String[] parts = incomingMessage.trim().split("\\s+");
        if (parts.length < 2)
            return "Uso: @dcli <id>";

        Long id;
        try {
            id = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return "Id inválido. Tente novamente.";
        }

        Customer customer = repository.findById(id).filter(Customer::isActive).orElse(null);
        if (customer == null)
            return "Cliente com id %d não encontrado.".formatted(id);

        long queued = printOrderRepository
                .findAllByActiveTrueAndStatusInOrderByPriorityAsc(List.of(OrderStatus.WAITING, OrderStatus.RUNNING))
                .stream()
                .filter(order -> order.getCustomer() != null && id.equals(order.getCustomer().getId()))
                .count();
        if (queued > 0)
            return "Cliente *%s* tem %d pedido(s) na fila. Encerre ou remova antes."
                    .formatted(customer.getName(), queued);

        customer.setActive(false);
        repository.save(customer);
        return "Cliente *%s* removido!".formatted(customer.getName());
    }
}
