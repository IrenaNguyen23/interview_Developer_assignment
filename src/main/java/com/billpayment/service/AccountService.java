package com.billpayment.service;

import com.billpayment.domain.Account;

import java.math.BigDecimal;

public class AccountService {

    private final Account account;

    public AccountService(Account account) {
        this.account = account;
    }

    public BigDecimal cashIn(BigDecimal amount) {
        account.deposit(amount);

        return account.getBalance();
    }

    public BigDecimal getBalance() {
        return account.getBalance();
    }

    public boolean hasEnoughBalance(BigDecimal amount) {
        return account.hasEnoughBalance(amount);
    }

    public void withdraw(BigDecimal amount) {
        account.withdraw(amount);
    }
}