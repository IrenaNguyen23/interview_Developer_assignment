package com.billpayment.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Payment {

    private final long id;

    private final long billId;

    private final BigDecimal amount;

    private final LocalDate paymentDate;

    private PaymentState state;

    public Payment(
            long id,
            long billId,
            BigDecimal amount,
            LocalDate paymentDate,
            PaymentState state) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Payment id must be greater than zero");
        }

        if (billId <= 0) {
            throw new IllegalArgumentException(
                    "Bill id must be greater than zero");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero");
        }

        if (paymentDate == null) {
            throw new IllegalArgumentException(
                    "Payment date is required");
        }

        if (state == null) {
            throw new IllegalArgumentException(
                    "Payment state is required");
        }

        this.id = id;
        this.billId = billId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.state = state;
    }

    public void markProcessed() {
        this.state = PaymentState.PROCESSED;
    }

    public void markFailed() {
        this.state = PaymentState.FAILED;
    }

    public long getId() {
        return id;
    }

    public long getBillId() {
        return billId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public PaymentState getState() {
        return state;
    }
}