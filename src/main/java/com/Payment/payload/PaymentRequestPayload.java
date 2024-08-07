package com.Payment.payload;

import lombok.Data;

@Data
public class PaymentRequestPayload {

    private double amount;
    private String currency;

    // Constructors, getters, and setters
}
