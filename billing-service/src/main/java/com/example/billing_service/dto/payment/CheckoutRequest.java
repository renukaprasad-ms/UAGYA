package com.example.billing_service.dto.payment;

import com.example.billing_service.entity.enums.PaymentMethod;
import com.example.billing_service.entity.enums.PaymentProvider;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {

    private String userId;
    private Long planId;

    private PaymentProvider paymentProvider;
    private PaymentMethod paymentMethod;
}
