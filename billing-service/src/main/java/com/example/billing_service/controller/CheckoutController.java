package com.example.billing_service.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.billing_service.dto.payment.CheckoutRequest;
import com.example.billing_service.dto.payment.PaymentOrderResponse;
import com.example.billing_service.service.CheckoutService;
import com.example.billing_service.utils.Response;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/checkout")
@RequiredArgsConstructor
public class CheckoutController {
    private final CheckoutService checkoutService;
    private final Response responseUtil;

    @PostMapping
    public ResponseEntity<Map<String,Object>> checkout(@RequestBody CheckoutRequest request) {
        PaymentOrderResponse response = checkoutService.checkout(request.getUserId(), request.getPlanId(), request.getPaymentProvider(), request.getPaymentMethod());

        return ResponseEntity.status(200).body(
            responseUtil.response(true, 200, response, "Checkout initiated", null)
        );
    }
}
