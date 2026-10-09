package com.billpayment.service;

import com.billpayment.domain.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountServiceTest {

        private AccountService accountService;

        @BeforeEach
        void setUp() {
                accountService = new AccountService(
                                new Account());
        }

        @Test
        void shouldCashInSuccessfully() {
                BigDecimal balance = accountService.cashIn(
                                new BigDecimal("1000000"));

                assertEquals(
                                0,
                                balance.compareTo(
                                                new BigDecimal("1000000")));
        }

        @Test
        void shouldAccumulateMultipleCashInOperations() {
                accountService.cashIn(
                                new BigDecimal("1000000"));

                BigDecimal balance = accountService.cashIn(
                                new BigDecimal("500000"));

                assertEquals(
                                0,
                                balance.compareTo(
                                                new BigDecimal("1500000")));
        }

        @Test
        void shouldRejectZeroCashIn() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> accountService.cashIn(
                                                BigDecimal.ZERO));
        }

        @Test
        void shouldRejectNegativeCashIn() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> accountService.cashIn(
                                                new BigDecimal("-100")));
        }
}