package com.example.billing_service.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.billing_service.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByPaymentOrderId(String paymentOrderId);
}
