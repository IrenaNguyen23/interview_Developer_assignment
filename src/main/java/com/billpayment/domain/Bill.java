package com.billpayment.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Bill {

    private final long id;

    private BillType type;

    private BigDecimal amount;

    private LocalDate dueDate;

    private BillState state;

    private String provider;

    public Bill(
            long id,
            BillType type,
            BigDecimal amount,
            LocalDate dueDate,
            String provider) {
        validateId(id);
        validateType(type);
        validateAmount(amount);
        validateDueDate(dueDate);
        validateProvider(provider);

        this.id = id;
        this.type = type;
        this.amount = amount;
        this.dueDate = dueDate;
        this.provider = provider;
        this.state = BillState.NOT_PAID;
    }

    public void update(
            BillType type,
            BigDecimal amount,
            LocalDate dueDate,
            String provider) {
        if (state == BillState.PAID) {
            throw new IllegalStateException(
                    "Paid bill cannot be updated");
        }

        validateType(type);
        validateAmount(amount);
        validateDueDate(dueDate);
        validateProvider(provider);

        this.type = type;
        this.amount = amount;
        this.dueDate = dueDate;
        this.provider = provider;
    }

    public void markPaid() {
        if (state == BillState.PAID) {
            throw new IllegalStateException(
                    "Bill has already been paid");
        }

        state = BillState.PAID;
    }

    private void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Bill id must be greater than zero");
        }
    }

    private void validateType(BillType type) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "Bill type is required");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Bill amount must be greater than zero");
        }
    }

    private void validateDueDate(LocalDate dueDate) {
        if (dueDate == null) {
            throw new IllegalArgumentException(
                    "Due date is required");
        }
    }

    private void validateProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException(
                    "Provider is required");
        }
    }

    public long getId() {
        return id;
    }

    public BillType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public BillState getState() {
        return state;
    }

    public String getProvider() {
        return provider;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Bill bill)) {
            return false;
        }

        return id == bill.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}