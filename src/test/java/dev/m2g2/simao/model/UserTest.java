package dev.m2g2.simao.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.m2g2.simao.enums.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void passwordNeverReachesJson() throws Exception {
        User user = new User();
        user.setName("Matheus");
        user.setEmail("matheus@argilalab.com.br");
        user.setPassword("$2a$10$hashsecretohashsecretohash");
        user.setRole(Role.ADMIN);

        String json = mapper.writeValueAsString(user);

        assertFalse(json.contains("password"), "o campo password não pode ser serializado");
        assertFalse(json.contains("hashsecreto"), "o hash da senha não pode vazar no JSON");
        assertTrue(json.contains("matheus@argilalab.com.br"), "o resto do usuário continua serializando");
    }

    @Test
    void newUserStartsWithTheLeastPrivilege() {
        assertEquals(Role.OPERATOR, new User().getRole(),
                "usuário sem papel explícito deve cair em OPERATOR, nunca em ADMIN");
        assertFalse(Role.OPERATOR.canSeeCosts());
        assertTrue(Role.ADMIN.canSeeCosts());
    }
}
