package com.billpayment.service;

import com.billpayment.domain.Bill;
import com.billpayment.domain.BillState;
import com.billpayment.domain.BillType;
import com.billpayment.repository.BillRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class BillService {

    private final BillRepository billRepository;

    public BillService(
            BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public Bill createBill(
            BillType type,
            BigDecimal amount,
            LocalDate dueDate,
            String provider) {
        Bill bill = new Bill(
                billRepository.nextId(),
                type,
                amount,
                dueDate,
                provider);

        billRepository.save(bill);

        return bill;
    }

    public Bill getBill(long id) {
        return billRepository
                .findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Not found a bill with such id"));
    }

    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    public void updateBill(
            long id,
            BillType type,
            BigDecimal amount,
            LocalDate dueDate,
            String provider) {
        Bill bill = getBill(id);

        bill.update(
                type,
                amount,
                dueDate,
                provider);
    }

    public void deleteBill(long id) {
        Bill bill = getBill(id);

        if (bill.getState() == BillState.PAID) {
            throw new IllegalStateException(
                    "Paid bill cannot be deleted");
        }

        billRepository.deleteById(id);
    }

    public List<Bill> searchByProvider(
            String provider) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException(
                    "Provider is required");
        }

        return billRepository
                .findAll()
                .stream()
                .filter(
                        bill -> bill
                                .getProvider()
                                .equalsIgnoreCase(provider))
                .toList();
    }

    public List<Bill> getUnpaidBillsByDueDate() {
        return billRepository
                .findAll()
                .stream()
                .filter(
                        bill -> bill.getState() == BillState.NOT_PAID)
                .sorted(
                        Comparator.comparing(
                                Bill::getDueDate))
                .toList();
    }
}