package com.billpayment.service;

import com.billpayment.domain.Bill;
import com.billpayment.domain.BillState;
import com.billpayment.domain.Payment;
import com.billpayment.domain.PaymentState;
import com.billpayment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PaymentService {

    private final AccountService accountService;
    private final BillService billService;
    private final PaymentRepository paymentRepository;
    private final Clock clock;

    public PaymentService(
            AccountService accountService,
            BillService billService,
            PaymentRepository paymentRepository,
            Clock clock) {
        this.accountService = accountService;
        this.billService = billService;
        this.paymentRepository = paymentRepository;
        this.clock = clock;
    }

    public List<Payment> payBills(List<Long> billIds) {
        validateBillIds(billIds);

        List<Bill> bills = getBills(billIds);

        validateBillsArePayable(bills);

        bills.sort(
                Comparator.comparing(Bill::getDueDate)
                        .thenComparing(Bill::getId));

        BigDecimal totalAmount = calculateTotalAmount(bills);

        if (!accountService.hasEnoughBalance(totalAmount)) {
            throw new IllegalStateException(
                    "Not enough fund to proceed with payment");
        }

        // Only mutate state after every validation succeeds.
        accountService.withdraw(totalAmount);

        LocalDate paymentDate = LocalDate.now(clock);

        List<Payment> payments = new ArrayList<>();

        for (Bill bill : bills) {
            bill.markPaid();

            Payment payment = new Payment(
                    paymentRepository.nextId(),
                    bill.getId(),
                    bill.getAmount(),
                    paymentDate,
                    PaymentState.PROCESSED);

            paymentRepository.save(payment);
            payments.add(payment);
        }

        return payments;
    }

    public List<Payment> getPaymentHistory() {
        return paymentRepository.findAll();
    }

    private void validateBillIds(List<Long> billIds) {
        if (billIds == null || billIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one bill id is required");
        }

        Set<Long> uniqueIds = new HashSet<>();

        for (Long billId : billIds) {
            if (billId == null || billId <= 0) {
                throw new IllegalArgumentException(
                        "Bill id must be greater than zero");
            }

            if (!uniqueIds.add(billId)) {
                throw new IllegalArgumentException(
                        "Duplicate bill id: " + billId);
            }
        }
    }

    private List<Bill> getBills(List<Long> billIds) {
        List<Bill> bills = new ArrayList<>();

        for (Long billId : billIds) {
            bills.add(
                    billService.getBill(billId));
        }

        return bills;
    }

    private void validateBillsArePayable(List<Bill> bills) {
        for (Bill bill : bills) {
            if (bill.getState() == BillState.PAID) {
                throw new IllegalStateException(
                        "Bill with id "
                                + bill.getId()
                                + " has already been paid");
            }
        }
    }

    private BigDecimal calculateTotalAmount(List<Bill> bills) {
        return bills.stream()
                .map(Bill::getAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);
    }
}