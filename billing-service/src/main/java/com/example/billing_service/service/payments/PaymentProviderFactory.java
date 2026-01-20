package com.example.billing_service.service.payments;

import com.example.billing_service.entity.enums.PaymentProvider;
import com.example.billing_service.exception.BadRequestExecption;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentProviderFactory {

    private final RazorpayPaymentService razorpayPaymentService;
   

    public PaymentProviderService getProvider(PaymentProvider provider) {

        return switch (provider) {
            case RAZORPAY -> razorpayPaymentService;
            default -> throw new BadRequestExecption(
                    "Unsupported payment provider: " + provider
            );
        };
    }
}
