package com.billpayment.cli;

import com.billpayment.domain.Account;
import com.billpayment.repository.BillRepository;
import com.billpayment.repository.PaymentRepository;
import com.billpayment.service.AccountService;
import com.billpayment.service.BillService;
import com.billpayment.service.PaymentService;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandLineShellTest {

    private ByteArrayOutputStream outputStream;

    private CommandLineShell createShell(
            String input
    ) {
        AccountService accountService =
                new AccountService(
                        new Account()
                );

        BillService billService =
                new BillService(
                        new BillRepository()
                );

        PaymentService paymentService =
                new PaymentService(
                        accountService,
                        billService,
                        new PaymentRepository(),
                        Clock.fixed(
                                Instant.parse(
                                        "2026-10-09T00:00:00Z"
                                ),
                                ZoneOffset.UTC
                        )
                );

        outputStream =
                new ByteArrayOutputStream();

        return new CommandLineShell(
                accountService,
                billService,
                paymentService,
                new Scanner(input),
                new PrintStream(outputStream)
        );
    }

    @Test
    void shouldCashInThroughCommandLine() {

        CommandLineShell shell =
                createShell(
                        """
                        CASH_IN 1000000
                        EXIT
                        """
                );

        shell.start();

        String output =
                outputStream.toString();

        assertTrue(
                output.contains(
                        "Your available balance: 1000000"
                )
        );

        assertTrue(
                output.contains(
                        "Good bye!"
                )
        );
    }

    @Test
    void shouldCreateAndListBill() {

        CommandLineShell shell =
                createShell(
                        """
                        CREATE_BILL ELECTRIC 200000 25/10/2026 EVN HCMC
                        LIST_BILL
                        EXIT
                        """
                );

        shell.start();

        String output =
                outputStream.toString();

        assertTrue(
                output.contains(
                        "Bill has been created with id 1"
                )
        );

        assertTrue(
                output.contains("ELECTRIC")
        );

        assertTrue(
                output.contains("200000")
        );

        assertTrue(
                output.contains("EVN HCMC")
        );
    }

    @Test
    void shouldPayBillThroughCommandLine() {

        CommandLineShell shell =
                createShell(
                        """
                        CASH_IN 1000000
                        CREATE_BILL ELECTRIC 200000 25/10/2026 EVN HCMC
                        PAY 1
                        EXIT
                        """
                );

        shell.start();

        String output =
                outputStream.toString();

        assertTrue(
                output.contains(
                        "Payment has been completed for Bill with id 1."
                )
        );

        assertTrue(
                output.contains(
                        "Your current balance is: 800000"
                )
        );
    }

    @Test
    void shouldDisplayErrorForUnknownBill() {

        CommandLineShell shell =
                createShell(
                        """
                        CASH_IN 1000000
                        PAY 999
                        EXIT
                        """
                );

        shell.start();

        String output =
                outputStream.toString();

        assertTrue(
                output.contains(
                        "Sorry! Not found a bill with such id"
                )
        );
    }
}