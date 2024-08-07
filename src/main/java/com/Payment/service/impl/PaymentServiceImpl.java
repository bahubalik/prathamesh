package com.Payment.service.impl;

import com.Payment.Repository.PaymentTransactionRepository;
import com.Payment.entities.PaymentTransaction;
import com.Payment.security.PayPalApiConfig;
import com.Payment.service.PaymentService;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentTransactionRepository transactionRepository;
    private final PayPalApiConfig payPalApiConfig;

    public PaymentServiceImpl(PaymentTransactionRepository transactionRepository, PayPalApiConfig payPalApiConfig) {
        this.transactionRepository = transactionRepository;
        this.payPalApiConfig = payPalApiConfig;
    }

    @Override
    public String initiatePayment(double amount, String currency) {
        Payment payment = createPayPalPayment(amount, currency);
        return payment.getId(); // Return the payment ID
    }

    @Override
    public void processPaymentResponse(String paymentId, String payerId) {
        APIContext apiContext = new APIContext(
                payPalApiConfig.getClientId(),
                payPalApiConfig.getClientSecret(),
                payPalApiConfig.getMode()
        );

        try {
            Payment payment = Payment.get(apiContext, paymentId);

            // Create PaymentExecution object and set the payer ID
            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(payerId);

            // Execute the payment using the API context and payment execution
            Payment executedPayment = payment.execute(apiContext, paymentExecution);

            // Retrieve the payment status
            String paymentStatus = executedPayment.getState();

            // Update the payment transaction status based on the payment status
            PaymentTransaction transaction = transactionRepository.findByTransactionId(paymentId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid payment ID"));

            transaction.setStatus(paymentStatus);
            transactionRepository.save(transaction);
        } catch (PayPalRESTException e) {
            // Handle exception
            e.printStackTrace();
        }
    }

    private Payment createPayPalPayment(double amount, String currency) {
        APIContext apiContext = new APIContext(
                payPalApiConfig.getClientId(),
                payPalApiConfig.getClientSecret(),
                payPalApiConfig.getMode()
        );

        Amount paymentAmount = new Amount();
        paymentAmount.setCurrency(currency);
        paymentAmount.setTotal(String.format("%.2f", amount));

        Transaction transaction = new Transaction();
        transaction.setAmount(paymentAmount);
        transaction.setDescription("Payment description");

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        try {
            Payment createdPayment = payment.create(apiContext);
            return createdPayment;
        } catch (PayPalRESTException e) {
            // Handle exception
            e.printStackTrace();
        }

        return null;
    }
}
