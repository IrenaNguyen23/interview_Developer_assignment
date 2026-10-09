package com.billpayment.service;

import com.billpayment.domain.Account;
import com.billpayment.domain.Bill;
import com.billpayment.domain.BillState;
import com.billpayment.domain.BillType;
import com.billpayment.domain.Payment;
import com.billpayment.domain.PaymentState;
import com.billpayment.repository.BillRepository;
import com.billpayment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private AccountService accountService;
    private BillService billService;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        Account account = new Account();

        accountService = new AccountService(account);

        billService = new BillService(
                new BillRepository());

        PaymentRepository paymentRepository = new PaymentRepository();

        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-10-09T00:00:00Z"),
                ZoneOffset.UTC);

        paymentService = new PaymentService(
                accountService,
                billService,
                paymentRepository,
                fixedClock);
    }

    @Test
    void shouldPaySingleBillSuccessfully() {
        accountService.cashIn(
                new BigDecimal("1000000"));

        Bill bill = billService.createBill(
                BillType.ELECTRIC,
                new BigDecimal("200000"),
                LocalDate.of(2026, 10, 25),
                "EVN HCMC");

        List<Payment> payments = paymentService.payBills(
                List.of(bill.getId()));

        assertEquals(
                BillState.PAID,
                bill.getState());

        assertEquals(
                0,
                accountService
                        .getBalance()
                        .compareTo(
                                new BigDecimal("800000")));

        assertEquals(1, payments.size());

        Payment payment = payments.get(0);

        assertEquals(
                bill.getId(),
                payment.getBillId());

        assertEquals(
                PaymentState.PROCESSED,
                payment.getState());

        assertEquals(
                LocalDate.of(2026, 10, 9),
                payment.getPaymentDate());
    }

    @Test
    void shouldPayMultipleBillsByEarliestDueDate() {
        accountService.cashIn(
                new BigDecimal("2000000"));

        Bill internet = billService.createBill(
                BillType.INTERNET,
                new BigDecimal("800000"),
                LocalDate.of(2026, 11, 30),
                "VNPT");

        Bill water = billService.createBill(
                BillType.WATER,
                new BigDecimal("175000"),
                LocalDate.of(2026, 10, 30),
                "SAVACO HCMC");

        List<Payment> payments = paymentService.payBills(
                List.of(
                        internet.getId(),
                        water.getId()));

        assertEquals(2, payments.size());

        // WATER has earlier due date,
        // so it must be processed first.
        assertEquals(
                water.getId(),
                payments.get(0).getBillId());

        assertEquals(
                internet.getId(),
                payments.get(1).getBillId());

        assertEquals(
                BillState.PAID,
                water.getState());

        assertEquals(
                BillState.PAID,
                internet.getState());

        assertEquals(
                0,
                accountService
                        .getBalance()
                        .compareTo(
                                new BigDecimal("1025000")));
    }

    @Test
    void shouldRejectPaymentWhenBalanceIsInsufficient() {
        accountService.cashIn(
                new BigDecimal("900000"));

        Bill water = billService.createBill(
                BillType.WATER,
                new BigDecimal("175000"),
                LocalDate.of(2026, 10, 30),
                "SAVACO HCMC");

        Bill internet = billService.createBill(
                BillType.INTERNET,
                new BigDecimal("800000"),
                LocalDate.of(2026, 11, 30),
                "VNPT");

        assertThrows(
                IllegalStateException.class,
                () -> paymentService.payBills(
                        List.of(
                                water.getId(),
                                internet.getId())));

        // Payment must be atomic:
        // nothing should have changed.
        assertEquals(
                BillState.NOT_PAID,
                water.getState());

        assertEquals(
                BillState.NOT_PAID,
                internet.getState());

        assertEquals(
                0,
                accountService
                        .getBalance()
                        .compareTo(
                                new BigDecimal("900000")));

        assertTrue(
                paymentService
                        .getPaymentHistory()
                        .isEmpty());
    }

    @Test
    void shouldNotPayAnythingWhenOneBillDoesNotExist() {
        accountService.cashIn(
                new BigDecimal("1000000"));

        Bill bill = billService.createBill(
                BillType.ELECTRIC,
                new BigDecimal("200000"),
                LocalDate.of(2026, 10, 25),
                "EVN");

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.payBills(
                        List.of(
                                bill.getId(),
                                999L)));

        assertEquals(
                BillState.NOT_PAID,
                bill.getState());

        assertEquals(
                0,
                accountService
                        .getBalance()
                        .compareTo(
                                new BigDecimal("1000000")));

        assertTrue(
                paymentService
                        .getPaymentHistory()
                        .isEmpty());
    }

    @Test
    void shouldRejectAlreadyPaidBill() {
        accountService.cashIn(
                new BigDecimal("1000000"));

        Bill bill = billService.createBill(
                BillType.ELECTRIC,
                new BigDecimal("200000"),
                LocalDate.of(2026, 10, 25),
                "EVN");

        paymentService.payBills(
                List.of(bill.getId()));

        assertThrows(
                IllegalStateException.class,
                () -> paymentService.payBills(
                        List.of(bill.getId())));

        assertEquals(
                0,
                accountService
                        .getBalance()
                        .compareTo(
                                new BigDecimal("800000")));

        assertEquals(
                1,
                paymentService
                        .getPaymentHistory()
                        .size());
    }

    @Test
    void shouldRejectDuplicateBillIds() {
        accountService.cashIn(
                new BigDecimal("1000000"));

        Bill bill = billService.createBill(
                BillType.ELECTRIC,
                new BigDecimal("200000"),
                LocalDate.of(2026, 10, 25),
                "EVN");

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.payBills(
                        List.of(
                                bill.getId(),
                                bill.getId())));

        assertEquals(
                BillState.NOT_PAID,
                bill.getState());

        assertEquals(
                0,
                accountService
                        .getBalance()
                        .compareTo(
                                new BigDecimal("1000000")));
    }
}