package com.example.billing_service.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.billing_service.entity.Invoice;
import com.example.billing_service.entity.UserSubscription;
import com.example.billing_service.entity.enums.BillingCycle;
import com.example.billing_service.entity.enums.InvoiceStatus;
import com.example.billing_service.entity.enums.PaymentMethod;
import com.example.billing_service.entity.enums.PaymentProvider;
import com.example.billing_service.entity.enums.PlanType;
import com.example.billing_service.entity.enums.SubscriptionStatus;
import com.example.billing_service.repository.InvoiceRepository;
import com.example.billing_service.repository.UserSubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RazorpayWebhookService {

    private final InvoiceRepository invoiceRepository;
    private final UserSubscriptionRepository subscriptionRepository;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    public void processWebhook(String payload, String signature) {

        verifySignature(payload, signature);

        JSONObject event = new JSONObject(payload);
        String eventType = event.getString("event");

        if ("payment.captured".equals(eventType)) {
            handlePaymentCaptured(event);
        }
    }

    private void handlePaymentCaptured(JSONObject event) {

        JSONObject paymentEntity = event.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String orderId = paymentEntity.getString("order_id");
        String method = paymentEntity.getString("method");

        Invoice invoice = invoiceRepository
                .findByPaymentOrderId(orderId)
                .orElseThrow(() -> new RuntimeException(
                        "Invoice not found for order: " + orderId));

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(Instant.now());
        invoiceRepository.save(invoice);

        UserSubscription subscription = invoice.getSubscription();
        Instant now = Instant.now();

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(now);
        subscription.setPaymentProvider(PaymentProvider.RAZORPAY);
        subscription.setPaymentMethod(mapPaymentMethod(method));

        if (subscription.getPlan().getPlanType() == PlanType.SUBSCRIPTION) {

            if (subscription.getBillingCycle() == BillingCycle.MONTHLY) {
                subscription.setNextBillingDate(
                        now.plus(1, ChronoUnit.MONTHS));
                subscription.setEndDate(now.plus(12, ChronoUnit.MONTHS));
            } else if (subscription.getBillingCycle() == BillingCycle.YEARLY) {
                subscription.setNextBillingDate(
                        now.plus(1, ChronoUnit.YEARS));
                subscription.setEndDate(now.plus(0, ChronoUnit.YEARS));
            }

        } else {
            subscription.setNextBillingDate(null);
            subscription.setEndDate(
                    now.plus(30, ChronoUnit.DAYS));
        }

        subscriptionRepository.save(subscription);
    }

    private void verifySignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"));

            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = bytesToHex(hash);

            if (!expectedSignature.equals(signature)) {
                throw new RuntimeException("Invalid Razorpay webhook signature");
            }
        } catch (Exception e) {
            throw new RuntimeException("Webhook signature verification failed", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private PaymentMethod mapPaymentMethod(String razorpayMethod) {
        return switch (razorpayMethod.toLowerCase()) {
            case "card" -> PaymentMethod.CARD;
            case "upi" -> PaymentMethod.UPI;
            case "netbanking" -> PaymentMethod.NETBANKING;
            default -> throw new IllegalArgumentException(
                    "Unsupported payment method: " + razorpayMethod);
        };
    }

}
