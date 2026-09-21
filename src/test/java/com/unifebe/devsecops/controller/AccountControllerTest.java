package com.unifebe.devsecops.controller;

import com.unifebe.devsecops.service.PaymentService;
import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountControllerTest {
    private final AccountController controller = new AccountController(new PaymentService());

    @Test
    void consultaParametrizadaPreservaResultadoEImpedeInjecao() throws Exception {
        // A conexao de fixture mantem o banco em memoria vivo durante a consulta.
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:test");
             var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE contas (id VARCHAR(80) PRIMARY KEY, nome VARCHAR(80))");
            statement.execute("INSERT INTO contas VALUES ('1', 'Ana'), ('2', 'Bruno')");
            assertEquals("Ana ", controller.buscarConta("1"));
            assertEquals("", controller.buscarConta("999"));
            assertEquals("", controller.buscarConta("1' OR '1'='1"));
            assertEquals("", controller.buscarConta("1'; DROP TABLE contas; --"));
            assertEquals("Bruno ", controller.buscarConta("2"));
        }
    }

    @Test
    void preservaHealthEDesconto() {
        assertEquals("OK", controller.health());
        assertEquals(180.0, controller.getDiscount(200, 10), 0.001);
    }
}
