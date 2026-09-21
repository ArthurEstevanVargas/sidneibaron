package com.unifebe.devsecops.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentServiceTest {

    private final PaymentService paymentService = new PaymentService();

    @Test
    void preservaLimitesDoDesconto() {
        assertEquals(200.0, paymentService.applyDiscount(200, 0), 0.001);
        assertEquals(0.0, paymentService.applyDiscount(200, 100), 0.001);
        assertEquals(0.0, paymentService.applyDiscount(0, 10), 0.001);
        assertEquals(17.991, paymentService.applyDiscount(19.99, 10), 0.001);
    }

    @Test
    void deveAplicarDezPorCentoDeDesconto() {
        double resultado = paymentService.applyDiscount(200.0, 10);
        // Esperado: 200 - 10% = 180.0
        assertEquals(180.0, resultado, 0.001);
    }
}
