package com.example.billing_service.service.payments;

import com.example.billing_service.entity.Invoice;
import com.example.billing_service.entity.enums.PaymentMethod;
import com.example.billing_service.dto.payment.PaymentOrderResponse;

public interface PaymentProviderService {
    
    PaymentOrderResponse createOrder(Invoice invoice, PaymentMethod method);
}
