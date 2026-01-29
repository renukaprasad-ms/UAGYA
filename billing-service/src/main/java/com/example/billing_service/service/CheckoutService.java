package com.example.billing_service.service;

import java.time.Instant;
import java.util.List;

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

        public PaymentOrderResponse checkout(
                        String userId,
                        Long planId,
                        PaymentProvider paymentProvider,
                        PaymentMethod paymentMethod) {

                Plan plan = planRepository.findById(planId)
                                .orElseThrow(() -> new BadRequestExecption("Plan not found"));

                // 🔍 Find active or pending subscription
                UserSubscription subscription = userSubscriptionRepository
                                .findByUserIdAndStatusIn(
                                                userId,
                                                List.of(
                                                                SubscriptionStatus.ACTIVE,
                                                                SubscriptionStatus.PENDING))
                                .orElseGet(() -> {
                                        return UserSubscription.builder()
                                                        .userId(userId)
                                                        .status(SubscriptionStatus.PENDING)
                                                        .startDate(Instant.now())
                                                        .autoDebitEnabled(false)
                                                        .build();
                                });

                // 🔁 PLAN CHANGE OR FIRST ASSIGNMENT
                subscription.setPlan(plan);
                subscription.setBillingCycle(plan.getBillingCycle());
                subscription.setPaymentProvider(paymentProvider);
                subscription.setPaymentMethod(paymentMethod);
                subscription.setUpdatedAt(Instant.now());

                subscription = userSubscriptionRepository.save(subscription);

                // 🧾 ALWAYS CREATE A NEW INVOICE
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

                PaymentOrderResponse orderResponse = providerService.createOrder(invoice, paymentMethod);

                invoice.setPaymentOrderId(orderResponse.getOrderId());
                invoiceRepository.save(invoice);

                return orderResponse;
        }

}
