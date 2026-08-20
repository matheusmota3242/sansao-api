package dev.m2g2.simao.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderInputUtilTest {

    @Test
    void parsePrintTime_acceptsPlainMinutes() {
        assertEquals(90, OrderInputUtil.parsePrintTimeMinutes("90"));
        assertEquals(45, OrderInputUtil.parsePrintTimeMinutes(" 45 "));
    }

    @Test
    void parsePrintTime_acceptsHourShorthand() {
        assertEquals(270, OrderInputUtil.parsePrintTimeMinutes("4h30"));
        assertEquals(270, OrderInputUtil.parsePrintTimeMinutes("4:30"));
        assertEquals(240, OrderInputUtil.parsePrintTimeMinutes("4h"));
        assertEquals(30, OrderInputUtil.parsePrintTimeMinutes(":30"));
    }

    @Test
    void parsePrintTime_rejectsNonPositive() {
        assertThrows(IllegalArgumentException.class, () -> OrderInputUtil.parsePrintTimeMinutes("0"));
        assertThrows(IllegalArgumentException.class, () -> OrderInputUtil.parsePrintTimeMinutes("-5"));
    }

    @Test
    void parsePrintTime_rejectsGarbage() {
        assertThrows(IllegalArgumentException.class, () -> OrderInputUtil.parsePrintTimeMinutes("amanhã"));
        assertThrows(IllegalArgumentException.class, () -> OrderInputUtil.parsePrintTimeMinutes(""));
    }

    @Test
    void parsePrintTime_rejectsMinutesAboveFiftyNine() {
        assertThrows(IllegalArgumentException.class, () -> OrderInputUtil.parsePrintTimeMinutes("1h70"));
    }

    @Test
    void parsePrice_followsPurchaseRules() {
        assertEquals(0, new java.math.BigDecimal("49.90").compareTo(OrderInputUtil.parsePrice("R$ 49,90")));
        assertEquals(0, new java.math.BigDecimal("12.50").compareTo(OrderInputUtil.parsePrice("12.50")));
    }

    @Test
    void formatMinutes_rendersCompactForm() {
        assertEquals("1h30", OrderInputUtil.formatMinutes(90));
        assertEquals("45min", OrderInputUtil.formatMinutes(45));
        assertEquals("2h", OrderInputUtil.formatMinutes(120));
        assertEquals("1h05", OrderInputUtil.formatMinutes(65));
        assertEquals("—", OrderInputUtil.formatMinutes(null));
    }
}
