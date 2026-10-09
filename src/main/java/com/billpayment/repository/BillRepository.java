package com.billpayment.repository;

import com.billpayment.domain.Bill;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BillRepository {

    private final Map<Long, Bill> bills = new LinkedHashMap<>();

    private long sequence = 0;

    public long nextId() {
        return ++sequence;
    }

    public void save(Bill bill) {
        bills.put(bill.getId(), bill);
    }

    public Optional<Bill> findById(long id) {
        return Optional.ofNullable(
                bills.get(id));
    }

    public List<Bill> findAll() {
        return new ArrayList<>(
                bills.values());
    }

    public void deleteById(long id) {
        bills.remove(id);
    }
}