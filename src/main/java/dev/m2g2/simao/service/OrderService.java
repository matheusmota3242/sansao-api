package dev.m2g2.simao.service;

import dev.m2g2.simao.dto.OrderDTO;
import dev.m2g2.simao.enums.ChatType;
import dev.m2g2.simao.enums.OrderStatus;
import dev.m2g2.simao.model.Customer;
import dev.m2g2.simao.model.PrintOrder;
import dev.m2g2.simao.model.chat.ChatRecord;
import dev.m2g2.simao.model.chat.order.CreateOrderInteraction;
import dev.m2g2.simao.model.chat.order.UpdateOrderInteraction;
import dev.m2g2.simao.repository.PrintOrderRepository;
import dev.m2g2.simao.util.OrderInputUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderService implements InteractionBaseService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final List<OrderStatus> QUEUE_STATUSES = List.of(OrderStatus.WAITING, OrderStatus.RUNNING);
    private static final List<OrderStatus> CLOSED_STATUSES = List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED);

    private final PrintOrderRepository repository;
    private final CustomerService customerService;
    private final ChatRecordService chatRecordService;

    public OrderService(PrintOrderRepository repository, CustomerService customerService,
                        ChatRecordService chatRecordService) {
        this.repository = repository;
        this.customerService = customerService;
        this.chatRecordService = chatRecordService;
    }

    /**
     * Persists an order coming from the chat interaction. Returns null when it
     * succeeded (the interaction's own text is the reply) or a message to show
     * instead when the customer could not be resolved — the interaction already
     * announced success before this runs, so the failure has to override it.
     */
    public String createFromChat(OrderDTO dto) {
        Customer customer = customerService.resolveByNameOrId(dto.getCustomerName());
        if (customer == null)
            return "Cliente com id %s não encontrado. O pedido *não* foi registrado."
                    .formatted(dto.getCustomerName().trim());
        create(dto, customer);
        return null;
    }

    @Transactional
    public PrintOrder create(OrderDTO dto) {
        return create(dto, customerService.resolveByNameOrId(dto.getCustomerName()));
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
        // New orders join the back of the queue; use @mord to move them up.
        order.setPriority(queue().size() + 1);
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setActive(true);
        return repository.save(order);
    }

    /**
     * Applies an update coming from the chat interaction. Same contract as
     * {@link #createFromChat(OrderDTO)}: null on success, message to show on
     * failure.
     */
    public String updateFromChat(Long id, OrderDTO dto) {
        Customer customer = null;
        if (dto.getCustomerName() != null && !dto.getCustomerName().isBlank()) {
            customer = customerService.resolveByNameOrId(dto.getCustomerName());
            if (customer == null)
                return "Cliente com id %s não encontrado. O pedido *não* foi atualizado."
                        .formatted(dto.getCustomerName().trim());
        }
        return update(id, dto, customer) == null
                ? "Pedido com id %d não encontrado.".formatted(id)
                : null;
    }

    @Transactional
    public PrintOrder update(Long id, OrderDTO dto) {
        Customer customer = dto.getCustomerName() == null || dto.getCustomerName().isBlank()
                ? null
                : customerService.resolveByNameOrId(dto.getCustomerName());
        return update(id, dto, customer);
    }

    @Transactional
    public PrintOrder update(Long id, OrderDTO dto, Customer customer) {
        PrintOrder order = repository.findById(id).orElse(null);
        if (order == null)
            return null;
        order.setDescription(dto.getDescription());
        if (customer != null)
            order.setCustomer(customer);
        order.setPrintTimeMinutes(dto.getPrintTimeMinutes());
        order.setProductionCost(dto.getProductionCost());
        order.setSalePrice(dto.getSalePrice());
        order.setObservations(dto.getObservations());
        return repository.save(order);
    }

    private List<PrintOrder> queue() {
        return repository.findAllByActiveTrueAndStatusInOrderByPriorityAsc(QUEUE_STATUSES);
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

    @Override
    public String createInteractionIf(String incomingMessage, String chatId, String participantId) {
        if (incomingMessage.equalsIgnoreCase(ChatType.CREATE_ORDER.getValue())) {
            CreateOrderInteraction interaction = new CreateOrderInteraction();
            ChatRecord record = new ChatRecord();
            record.setInteraction(interaction);
            record.setChatId(chatId);
            record.setParticipantId(participantId);
            chatRecordService.create(record);
            return interaction.processInput(incomingMessage).text();
        }
        return null;
    }

    public String updateInteractionIf(String incomingMessage, String chatId, String participantId) {
        if (!incomingMessage.toLowerCase().startsWith(ChatType.UPDATE_ORDER.getValue()))
            return null;

        String[] parts = incomingMessage.trim().split("\\s+");
        if (parts.length < 2)
            return "Uso: @uord <id>";

        Long id;
        try {
            id = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return "Id inválido. Tente novamente.";
        }

        PrintOrder order = repository.findById(id).filter(PrintOrder::isActive).orElse(null);
        if (order == null)
            return "Pedido com id %d não encontrado.".formatted(id);

        UpdateOrderInteraction interaction = new UpdateOrderInteraction();
        interaction.setTargetId(id);
        interaction.setData(toDto(order));
        ChatRecord record = new ChatRecord();
        record.setInteraction(interaction);
        record.setChatId(chatId);
        record.setParticipantId(participantId);
        chatRecordService.create(record);
        return interaction.processInput(ChatType.UPDATE_ORDER.getValue()).text();
    }

    @Override
    public String listIf(String incomingMessage) {
        if (!incomingMessage.equalsIgnoreCase(ChatType.LIST_ORDERS.getValue()))
            return null;

        List<PrintOrder> pending = queue();
        List<PrintOrder> closed = repository.findAllByActiveTrueAndStatusInOrderByIdDesc(CLOSED_STATUSES);
        if (pending.isEmpty() && closed.isEmpty())
            return "Nenhum pedido registrado!";

        StringBuilder builder = new StringBuilder("*Fila de impressão*\n\n");
        if (pending.isEmpty()) {
            builder.append("Fila vazia.\n");
        } else {
            int totalMinutes = 0;
            for (PrintOrder order : pending) {
                builder.append(describeQueued(order));
                if (order.getPrintTimeMinutes() != null)
                    totalMinutes += order.getPrintTimeMinutes();
            }
            builder.append("_Tempo total estimado na fila: %s_\n".formatted(OrderInputUtil.formatMinutes(totalMinutes)));
        }

        if (!closed.isEmpty()) {
            builder.append("\n*Encerrados*\n\n");
            for (PrintOrder order : closed)
                builder.append(describeClosed(order));
        }
        return builder.toString().trim();
    }

    private String describeQueued(PrintOrder order) {
        StringBuilder builder = new StringBuilder();
        builder.append("*%dº | #%d - %s*\n".formatted(
                order.getPriority(), order.getId(), order.getDescription()));
        builder.append("Cliente: %s | Status: %s\n".formatted(
                order.getCustomer().getName(), order.getStatus().getLabel()));
        builder.append("Tempo: %s%s\n".formatted(
                OrderInputUtil.formatMinutes(order.getPrintTimeMinutes()),
                order.getStartedAt() == null ? "" : " | Início: " + order.getStartedAt().format(FORMATTER)));
        builder.append(describeMoney(order));
        if (order.getObservations() != null && !order.getObservations().isBlank())
            builder.append("Obs: %s\n".formatted(order.getObservations()));
        builder.append("\n");
        return builder.toString();
    }

    private String describeClosed(PrintOrder order) {
        StringBuilder builder = new StringBuilder();
        builder.append("*#%d - %s* (%s)\n".formatted(
                order.getId(), order.getDescription(), order.getStatus().getLabel()));
        builder.append("Cliente: %s | Tempo: %s\n".formatted(
                order.getCustomer().getName(), OrderInputUtil.formatMinutes(order.getPrintTimeMinutes())));
        builder.append(describeMoney(order));
        builder.append("\n");
        return builder.toString();
    }

    private String describeMoney(PrintOrder order) {
        if (order.getProductionCost() == null && order.getSalePrice() == null)
            return "";
        String cost = order.getProductionCost() == null ? "—" : "R$ " + order.getProductionCost().toPlainString();
        String price = order.getSalePrice() == null ? "—" : "R$ " + order.getSalePrice().toPlainString();
        String margin = "";
        if (order.getProductionCost() != null && order.getSalePrice() != null) {
            BigDecimal profit = order.getSalePrice().subtract(order.getProductionCost());
            margin = " | Lucro: R$ %s".formatted(profit.toPlainString());
        }
        return "Custo: %s | Venda: %s%s\n".formatted(cost, price, margin);
    }

    /**
     * Moves an order to a new position in the queue, shifting the others so the
     * sequence stays contiguous. Usage: @mord &lt;id&gt; &lt;posição&gt;.
     */
    @Transactional
    public String moveIf(String incomingMessage) {
        if (!incomingMessage.toLowerCase().startsWith(ChatType.MOVE_ORDER.getValue()))
            return null;

        String[] parts = incomingMessage.trim().split("\\s+");
        if (parts.length < 3)
            return "Uso: @mord <id> <posição>";

        Long id;
        int target;
        try {
            id = Long.parseLong(parts[1]);
            target = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return "Id ou posição inválidos. Use: @mord <id> <posição>";
        }

        List<PrintOrder> pending = queue();
        PrintOrder order = pending.stream().filter(o -> o.getId().equals(id)).findFirst().orElse(null);
        if (order == null)
            return "Pedido com id %d não está na fila.".formatted(id);

        if (target < 1 || target > pending.size())
            return "Posição inválida. A fila tem %d pedido(s).".formatted(pending.size());

        pending.remove(order);
        pending.add(target - 1, order);
        renumber(pending);
        return "Pedido %d movido para a posição %d.".formatted(id, target);
    }

    /**
     * Changes the execution status. Leaving the queue frees the position and
     * renumbers the rest; RUNNING stamps started_at, and going back to WAITING
     * clears it. Usage: @sord &lt;id&gt; &lt;status&gt;.
     */
    @Transactional
    public String changeStatusIf(String incomingMessage) {
        if (!incomingMessage.toLowerCase().startsWith(ChatType.ORDER_STATUS.getValue()))
            return null;

        String[] parts = incomingMessage.trim().split("\\s+");
        if (parts.length < 3)
            return "Uso: @sord <id> <WAITING|RUNNING|COMPLETED|CANCELLED>";

        Long id;
        try {
            id = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return "Id inválido. Tente novamente.";
        }

        OrderStatus status = OrderStatus.fromInput(parts[2]);
        if (status == null)
            return "Status inválido. Use WAITING, RUNNING, COMPLETED ou CANCELLED.";

        PrintOrder order = repository.findById(id).filter(PrintOrder::isActive).orElse(null);
        if (order == null)
            return "Pedido com id %d não encontrado.".formatted(id);

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
        return "Pedido %d agora está como *%s*.".formatted(id, status.getLabel());
    }

    @Override
    @Transactional
    public String deleteIf(String incomingMessage) {
        if (!incomingMessage.toLowerCase().startsWith(ChatType.DELETE_ORDER.getValue()))
            return null;

        String[] parts = incomingMessage.trim().split("\\s+");
        if (parts.length < 2)
            return "Uso: @dord <id>";

        try {
            Long id = Long.parseLong(parts[1]);
            if (!repository.existsById(id))
                return "Pedido com id %d não encontrado.".formatted(id);
            repository.deleteById(id);
            renumber(queue());
            return "Pedido com id %d removido!".formatted(id);
        } catch (NumberFormatException e) {
            return "Id inválido. Tente novamente.";
        }
    }

    private OrderDTO toDto(PrintOrder order) {
        OrderDTO dto = new OrderDTO();
        dto.setDescription(order.getDescription());
        dto.setCustomerName(order.getCustomer() == null ? null : order.getCustomer().getName());
        dto.setPrintTimeMinutes(order.getPrintTimeMinutes());
        dto.setProductionCost(order.getProductionCost());
        dto.setSalePrice(order.getSalePrice());
        dto.setObservations(order.getObservations());
        return dto;
    }
}
