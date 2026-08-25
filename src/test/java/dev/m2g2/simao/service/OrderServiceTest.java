package dev.m2g2.simao.service;

import dev.m2g2.simao.dto.OrderDTO;
import dev.m2g2.simao.enums.OrderStatus;
import dev.m2g2.simao.model.Customer;
import dev.m2g2.simao.model.PrintOrder;
import dev.m2g2.simao.repository.PrintOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private PrintOrderRepository repository;
    @Mock
    private CustomerService customerService;

    private OrderService service;
    private List<PrintOrder> queue;

    @BeforeEach
    void setUp() {
        service = new OrderService(repository, customerService);
        queue = new ArrayList<>();
        lenient().when(repository.findAllByActiveTrueAndStatusInOrderByPriorityAsc(anyCollection()))
                .thenAnswer(i -> new ArrayList<>(queue.stream()
                        .filter(o -> o.getStatus().isInQueue())
                        .sorted(java.util.Comparator.comparing(
                                PrintOrder::getPriority,
                                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                        .toList()));
        lenient().when(repository.save(any(PrintOrder.class))).thenAnswer(i -> i.getArgument(0));
    }

    private PrintOrder queued(long id, int priority) {
        PrintOrder order = new PrintOrder();
        order.setId(id);
        order.setDescription("pedido " + id);
        order.setCustomer(new Customer());
        order.setStatus(OrderStatus.WAITING);
        order.setPriority(priority);
        order.setActive(true);
        queue.add(order);
        return order;
    }

    @Test
    void newOrderJoinsTheBackOfTheQueue() {
        queued(1L, 1);
        queued(2L, 2);
        when(customerService.resolveByNameOrId("Ana")).thenReturn(new Customer());

        OrderDTO dto = new OrderDTO();
        dto.setDescription("novo");
        dto.setCustomerName("Ana");
        PrintOrder created = service.create(dto);

        assertEquals(3, created.getPriority());
        assertEquals(OrderStatus.WAITING, created.getStatus());
    }

    @Test
    void movingAnOrderKeepsTheQueueContiguous() {
        PrintOrder first = queued(1L, 1);
        PrintOrder second = queued(2L, 2);
        PrintOrder third = queued(3L, 3);

        service.move(3L, 1);

        assertEquals(1, third.getPriority());
        assertEquals(2, first.getPriority());
        assertEquals(3, second.getPriority());
    }

    @Test
    void movingToAnImpossiblePositionIsRejected() {
        queued(1L, 1);
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.move(1L, 9));
        assertEquals(400, e.getStatusCode().value());
    }

    @Test
    void leavingTheQueueFreesThePositionAndRenumbersTheRest() {
        PrintOrder first = queued(1L, 1);
        PrintOrder second = queued(2L, 2);
        PrintOrder third = queued(3L, 3);
        when(repository.findById(1L)).thenReturn(Optional.of(first));

        service.changeStatus(1L, OrderStatus.COMPLETED);

        assertNull(first.getPriority(), "pedido encerrado não ocupa posição");
        assertEquals(1, second.getPriority());
        assertEquals(2, third.getPriority());
    }

    @Test
    void runningStampsTheStartAndGoingBackToWaitingClearsIt() {
        PrintOrder order = queued(1L, 1);
        when(repository.findById(1L)).thenReturn(Optional.of(order));

        service.changeStatus(1L, OrderStatus.RUNNING);
        assertEquals(OrderStatus.RUNNING, order.getStatus());
        assertNotNull(order.getStartedAt(), "RUNNING carimba o início");

        service.changeStatus(1L, OrderStatus.WAITING);
        assertNull(order.getStartedAt(), "voltar para WAITING limpa o início");
    }

    @Test
    void reopeningAClosedOrderPutsItAtTheBack() {
        PrintOrder closed = queued(1L, 1);
        closed.setStatus(OrderStatus.CANCELLED);
        closed.setPriority(null);
        queued(2L, 1);
        queued(3L, 2);
        when(repository.findById(1L)).thenReturn(Optional.of(closed));

        service.changeStatus(1L, OrderStatus.WAITING);

        assertEquals(3, closed.getPriority());
    }

    @Test
    void anUnknownCustomerIdIsAnErrorNotANewCustomer() {
        when(customerService.resolveByNameOrId("999")).thenReturn(null);
        OrderDTO dto = new OrderDTO();
        dto.setDescription("x");
        dto.setCustomerName("999");

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.create(dto));
        assertEquals(404, e.getStatusCode().value());
    }

    @Test
    void anOrderWithoutACustomerIsRejected() {
        OrderDTO dto = new OrderDTO();
        dto.setDescription("x");
        dto.setCustomerName("  ");

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.create(dto));
        assertEquals(400, e.getStatusCode().value());
    }

    @Test
    void salePriceStaysButCostAndProfitAreHiddenFromNonAdmins() {
        PrintOrder order = queued(1L, 1);
        order.setProductionCost(new BigDecimal("30.00"));
        order.setSalePrice(new BigDecimal("90.00"));

        var asAdmin = dev.m2g2.simao.dto.management.OrderResponse.from(order, true);
        assertEquals(new BigDecimal("30.00"), asAdmin.productionCost());
        assertEquals(new BigDecimal("60.00"), asAdmin.profit());

        var asOperator = dev.m2g2.simao.dto.management.OrderResponse.from(order, false);
        assertNull(asOperator.productionCost(), "custo não pode sair para o OPERATOR");
        assertNull(asOperator.profit(), "lucro não pode sair para o OPERATOR");
        assertEquals(new BigDecimal("90.00"), asOperator.salePrice(),
                "preço de venda o OPERATOR precisa ver");
    }
}
