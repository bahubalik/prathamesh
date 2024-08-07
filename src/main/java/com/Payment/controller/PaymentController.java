package com.Payment.controller;

import com.Payment.payload.PaymentRequestPayload;
import com.Payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> initiatePayment(@RequestBody PaymentRequestPayload payload) {
        double amount = payload.getAmount();
        String currency = payload.getCurrency();

        String paymentId = paymentService.initiatePayment(amount, currency);

        // Return the payment ID in the response body
        Map<String, String> response = new HashMap<>();
        response.put("paymentId", paymentId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete")
    public ResponseEntity<String> processPaymentResponse(@RequestParam("paymentId") String paymentId,
                                                         @RequestParam("payerId") String payerId) {
        paymentService.processPaymentResponse(paymentId, payerId);

        // Return success response
        return ResponseEntity.ok("Payment processed successfully");
    }
}
