package com.example.billing_service.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.example.billing_service.dto.payment.PaymentOrderResponse;
import com.example.billing_service.entity.Invoice;
import com.example.billing_service.entity.Plan;
import com.example.billing_service.entity.UserSubscription;
import com.example.billing_service.entity.enums.InvoiceStatus;
import com.example.billing_service.entity.enums.PaymentMethod;
import com.example.billing_service.entity.enums.PaymentProvider;
import com.example.billing_service.entity.enums.SubscriptionStatus;
import com.example.billing_service.exception.BadRequestExecption;
import com.example.billing_service.repository.InvoiceRepository;
import com.example.billing_service.repository.PlanRepository;
import com.example.billing_service.repository.UserSubscriptionRepository;
import com.example.billing_service.service.payments.PaymentProviderFactory;
import com.example.billing_service.service.payments.PaymentProviderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckoutService {
    private final PlanRepository planRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentProviderFactory paymentProviderFactory;
    private final UserSubscriptionRepository userSubscriptionRepository;

    public PaymentOrderResponse checkout(String userId, Long PlanId, PaymentProvider paymentProvider,
            PaymentMethod paymentMethod) {

        Plan plan = planRepository.findById(PlanId)
                .orElseThrow(() -> new BadRequestExecption("Plan not found"));

        UserSubscription subscription = userSubscriptionRepository.save(
                UserSubscription.builder()
                        .userId(userId)
                        .plan(plan)
                        .billingCycle(plan.getBillingCycle())
                        .status(SubscriptionStatus.PENDING)
                        .startDate(Instant.now())
                        .paymentProvider(paymentProvider)
                        .paymentMethod(paymentMethod)
                        .autoDebitEnabled(false)
                        .build());


        Invoice invoice = invoiceRepository.save(
                Invoice.builder()
                        .invoiceNumber("INV-" + System.currentTimeMillis())
                        .userId(userId)
                        .plan(plan)
                        .subscription(subscription)
                        .billingCycle(plan.getBillingCycle())
                        .amount(plan.getPrice())
                        .totalAmount(plan.getPrice())
                        .currency(plan.getCurrency())
                        .status(InvoiceStatus.ISSUED)
                        .issuedAt(Instant.now())
                        .dueDate(Instant.now().plusSeconds(86400))
                        .build());

        PaymentProviderService providerService = paymentProviderFactory.getProvider(paymentProvider);
        PaymentOrderResponse orderResponse =
        providerService.createOrder(invoice, paymentMethod);
        invoice.setPaymentOrderId(orderResponse.getOrderId());
invoiceRepository.save(invoice);
        return orderResponse ;

    }
}
