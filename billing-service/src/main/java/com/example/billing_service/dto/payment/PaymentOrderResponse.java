package com.example.billing_service.dto.payment;

import com.example.billing_service.entity.enums.PaymentProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PaymentOrderResponse {

    private PaymentProvider provider;

    private String orderId;

    
    private BigDecimal amount;

   
    private String currency;
}
