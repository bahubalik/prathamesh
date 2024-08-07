package com.Payment.service;

public interface PaymentService {
    String initiatePayment(double amount, String currency);
    void processPaymentResponse(String paymentId, String payerId);
}
