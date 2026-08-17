package dev.m2g2.simao.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStatusTest {

    @Test
    void fromInput_acceptsEnumNameIgnoringCase() {
        assertEquals(OrderStatus.RUNNING, OrderStatus.fromInput("running"));
        assertEquals(OrderStatus.COMPLETED, OrderStatus.fromInput("  COMPLETED "));
    }

    @Test
    void fromInput_acceptsPortugueseLabel() {
        assertEquals(OrderStatus.WAITING, OrderStatus.fromInput("Aguardando"));
        assertEquals(OrderStatus.CANCELLED, OrderStatus.fromInput("cancelado"));
    }

    @Test
    void fromInput_returnsNullForUnknown() {
        assertNull(OrderStatus.fromInput("pausado"));
        assertNull(OrderStatus.fromInput(""));
        assertNull(OrderStatus.fromInput(null));
    }

    @Test
    void isInQueue_onlyForWaitingAndRunning() {
        assertTrue(OrderStatus.WAITING.isInQueue());
        assertTrue(OrderStatus.RUNNING.isInQueue());
        assertFalse(OrderStatus.COMPLETED.isInQueue());
        assertFalse(OrderStatus.CANCELLED.isInQueue());
    }
}
