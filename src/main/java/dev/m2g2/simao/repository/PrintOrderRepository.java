package dev.m2g2.simao.repository;

import dev.m2g2.simao.enums.OrderStatus;
import dev.m2g2.simao.model.PrintOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PrintOrderRepository extends JpaRepository<PrintOrder, Long> {

    /**
     * The queue itself: orders still waiting or running, in printing order.
     */
    List<PrintOrder> findAllByActiveTrueAndStatusInOrderByPriorityAsc(Collection<OrderStatus> statuses);

    /**
     * Orders that already left the queue, newest first. Ordered by id because
     * BaseModel maps updated_at as non-updatable, so it cannot stand in for
     * "when it finished".
     */
    List<PrintOrder> findAllByActiveTrueAndStatusInOrderByIdDesc(Collection<OrderStatus> statuses);
}
