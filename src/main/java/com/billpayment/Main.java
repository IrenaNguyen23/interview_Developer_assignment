package com.billpayment;

import com.billpayment.cli.CommandLineShell;
import com.billpayment.domain.Account;
import com.billpayment.repository.BillRepository;
import com.billpayment.repository.PaymentRepository;
import com.billpayment.service.AccountService;
import com.billpayment.service.BillService;
import com.billpayment.service.PaymentService;

import java.time.Clock;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Account account = new Account();

        BillRepository billRepository = new BillRepository();

        PaymentRepository paymentRepository = new PaymentRepository();

        AccountService accountService = new AccountService(account);

        BillService billService = new BillService(
                billRepository);

        PaymentService paymentService = new PaymentService(
                accountService,
                billService,
                paymentRepository,
                Clock.systemDefaultZone());

        CommandLineShell shell = new CommandLineShell(
                accountService,
                billService,
                paymentService,
                new Scanner(System.in),
                System.out);

        shell.start();
    }
}