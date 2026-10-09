package com.billpayment.service;

import com.billpayment.domain.Bill;
import com.billpayment.domain.BillState;
import com.billpayment.domain.BillType;
import com.billpayment.repository.BillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BillServiceTest {

    private BillService billService;

    @BeforeEach
    void setUp() {
        billService = new BillService(
                new BillRepository());
    }

    @Test
    void shouldCreateBillSuccessfully() {
        Bill bill = billService.createBill(
                BillType.ELECTRIC,
                new BigDecimal("200000"),
                LocalDate.of(2026, 10, 25),
                "EVN HCMC");

        assertEquals(1L, bill.getId());
        assertEquals(
                BillType.ELECTRIC,
                bill.getType());
        assertEquals(
                BillState.NOT_PAID,
                bill.getState());
    }

    @Test
    void shouldFindBillById() {
        Bill created = billService.createBill(
                BillType.WATER,
                new BigDecimal("175000"),
                LocalDate.of(2026, 10, 30),
                "SAVACO HCMC");

        Bill found = billService.getBill(
                created.getId());

        assertEquals(
                created,
                found);
    }

    @Test
    void shouldThrowWhenBillDoesNotExist() {
        assertThrows(
                IllegalArgumentException.class,
                () -> billService.getBill(999));
    }

    @Test
    void shouldSearchBillByProvider() {
        billService.createBill(
                BillType.INTERNET,
                new BigDecimal("800000"),
                LocalDate.of(2026, 11, 30),
                "VNPT");

        billService.createBill(
                BillType.ELECTRIC,
                new BigDecimal("200000"),
                LocalDate.of(2026, 10, 25),
                "EVN");

        List<Bill> result = billService.searchByProvider(
                "vnpt");

        assertEquals(
                1,
                result.size());

        assertEquals(
                "VNPT",
                result.get(0).getProvider());
    }

    @Test
    void shouldReturnBillsOrderedByDueDate() {
        billService.createBill(
                BillType.INTERNET,
                new BigDecimal("800000"),
                LocalDate.of(2026, 11, 30),
                "VNPT");

        billService.createBill(
                BillType.ELECTRIC,
                new BigDecimal("200000"),
                LocalDate.of(2026, 10, 25),
                "EVN");

        List<Bill> result = billService.getUnpaidBillsByDueDate();

        assertEquals(
                BillType.ELECTRIC,
                result.get(0).getType());

        assertEquals(
                BillType.INTERNET,
                result.get(1).getType());
    }
}