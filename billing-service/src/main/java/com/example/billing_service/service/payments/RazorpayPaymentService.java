package com.example.billing_service.service.payments;

import com.example.billing_service.dto.payment.PaymentOrderResponse;
import com.example.billing_service.entity.Invoice;
import com.example.billing_service.entity.enums.PaymentMethod;
import com.example.billing_service.entity.enums.PaymentProvider;
import com.razorpay.*;

import lombok.RequiredArgsConstructor;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RazorpayPaymentService implements PaymentProviderService {

    private final RazorpayClient razorpayClient;

    @Override
    public PaymentOrderResponse createOrder(
            Invoice invoice,
            PaymentMethod paymentMethod
    ) {

        try {
            JSONObject options = new JSONObject();
            options.put("amount", invoice.getTotalAmount().multiply(BigDecimal.valueOf(100)));
            options.put("currency", invoice.getCurrency());
            options.put("receipt", invoice.getInvoiceNumber());

            Order order = razorpayClient.orders.create(options);

            return new PaymentOrderResponse(
                    PaymentProvider.RAZORPAY,
                    order.get("id"),
                    invoice.getTotalAmount(),
                    invoice.getCurrency()
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to create Razorpay order", e);
        }
    }
}
