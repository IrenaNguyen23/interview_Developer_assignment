package com.billpayment.repository;

import com.billpayment.domain.Payment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PaymentRepository {

    private final Map<Long, Payment> payments = new LinkedHashMap<>();

    private long sequence = 0;

    public long nextId() {
        return ++sequence;
    }

    public void save(Payment payment) {
        payments.put(
                payment.getId(),
                payment);
    }

    public List<Payment> findAll() {
        return new ArrayList<>(
                payments.values());
    }
}