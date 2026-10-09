package com.billpayment.cli;

import com.billpayment.domain.Bill;
import com.billpayment.domain.BillType;
import com.billpayment.domain.Payment;
import com.billpayment.service.AccountService;
import com.billpayment.service.BillService;
import com.billpayment.service.PaymentService;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CommandLineShell {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AccountService accountService;
    private final BillService billService;
    private final PaymentService paymentService;
    private final Scanner scanner;
    private final PrintStream output;

    public CommandLineShell(
            AccountService accountService,
            BillService billService,
            PaymentService paymentService,
            Scanner scanner,
            PrintStream output) {
        this.accountService = accountService;
        this.billService = billService;
        this.paymentService = paymentService;
        this.scanner = scanner;
        this.output = output;
    }

    public void start() {
        while (true) {
            if (!scanner.hasNextLine()) {
                break;
            }

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            try {
                boolean shouldExit = execute(input);

                if (shouldExit) {
                    return;
                }
            } catch (IllegalArgumentException | IllegalStateException exception) {
                output.println("Sorry! " + exception.getMessage());
            } catch (Exception exception) {
                output.println("Sorry! Invalid command.");
            }
        }
    }

    boolean execute(String input) {
        String[] tokens = input.trim().split("\\s+");

        String command = tokens[0].toUpperCase();

        return switch (command) {
            case "CASH_IN" -> {
                handleCashIn(tokens);
                yield false;
            }

            case "CREATE_BILL" -> {
                handleCreateBill(tokens);
                yield false;
            }

            case "UPDATE_BILL" -> {
                handleUpdateBill(tokens);
                yield false;
            }

            case "DELETE_BILL" -> {
                handleDeleteBill(tokens);
                yield false;
            }

            case "LIST_BILL" -> {
                handleListBill(tokens);
                yield false;
            }

            case "PAY" -> {
                handlePay(tokens);
                yield false;
            }

            case "DUE_DATE" -> {
                handleDueDate(tokens);
                yield false;
            }

            case "LIST_PAYMENT" -> {
                handleListPayment(tokens);
                yield false;
            }

            case "SEARCH_BILL_BY_PROVIDER" -> {
                handleSearchBillByProvider(tokens);
                yield false;
            }

            case "EXIT" -> {
                validateArgumentCount(tokens, 1);
                output.println("Good bye!");
                yield true;
            }

            default ->
                throw new IllegalArgumentException(
                        "Unknown command: " + tokens[0]);
        };
    }

    private void handleCashIn(String[] tokens) {
        validateArgumentCount(tokens, 2);

        BigDecimal amount = parseAmount(tokens[1]);

        BigDecimal balance = accountService.cashIn(amount);

        output.println(
                "Your available balance: "
                        + formatAmount(balance));
    }

    private void handleCreateBill(String[] tokens) {
        if (tokens.length < 5) {
            throw new IllegalArgumentException(
                    "Usage: CREATE_BILL <TYPE> <AMOUNT> <DUE_DATE> <PROVIDER>");
        }

        BillType type = parseBillType(tokens[1]);

        BigDecimal amount = parseAmount(tokens[2]);

        LocalDate dueDate = parseDate(tokens[3]);

        String provider = joinTokens(tokens, 4);

        Bill bill = billService.createBill(
                type,
                amount,
                dueDate,
                provider);

        output.println(
                "Bill has been created with id "
                        + bill.getId()
                        + ".");
    }

    private void handleUpdateBill(String[] tokens) {
        if (tokens.length < 6) {
            throw new IllegalArgumentException(
                    "Usage: UPDATE_BILL <ID> <TYPE> <AMOUNT> <DUE_DATE> <PROVIDER>");
        }

        long id = parseId(tokens[1]);

        BillType type = parseBillType(tokens[2]);

        BigDecimal amount = parseAmount(tokens[3]);

        LocalDate dueDate = parseDate(tokens[4]);

        String provider = joinTokens(tokens, 5);

        billService.updateBill(
                id,
                type,
                amount,
                dueDate,
                provider);

        output.println(
                "Bill with id "
                        + id
                        + " has been updated.");
    }

    private void handleDeleteBill(String[] tokens) {
        validateArgumentCount(tokens, 2);

        long id = parseId(tokens[1]);

        billService.deleteBill(id);

        output.println(
                "Bill with id "
                        + id
                        + " has been deleted.");
    }

    private void handleListBill(String[] tokens) {
        validateArgumentCount(tokens, 1);

        printBills(
                billService.getAllBills());
    }

    private void handlePay(String[] tokens) {
        if (tokens.length < 2) {
            throw new IllegalArgumentException(
                    "Usage: PAY <BILL_ID> [BILL_ID...]");
        }

        List<Long> billIds = new ArrayList<>();

        for (int index = 1; index < tokens.length; index++) {

            billIds.add(
                    parseId(tokens[index]));
        }

        List<Payment> payments = paymentService.payBills(billIds);

        for (Payment payment : payments) {
            output.println(
                    "Payment has been completed for Bill with id "
                            + payment.getBillId()
                            + ".");
        }

        output.println(
                "Your current balance is: "
                        + formatAmount(
                                accountService.getBalance()));
    }

    private void handleDueDate(String[] tokens) {
        validateArgumentCount(tokens, 1);

        printBills(
                billService.getUnpaidBillsByDueDate());
    }

    private void handleListPayment(String[] tokens) {
        validateArgumentCount(tokens, 1);

        List<Payment> payments = paymentService.getPaymentHistory();

        printPayments(payments);
    }

    private void handleSearchBillByProvider(
            String[] tokens) {
        if (tokens.length < 2) {
            throw new IllegalArgumentException(
                    "Usage: SEARCH_BILL_BY_PROVIDER <PROVIDER>");
        }

        String provider = joinTokens(tokens, 1);

        List<Bill> bills = billService.searchByProvider(
                provider);

        printBills(bills);
    }

    private void printBills(List<Bill> bills) {
        if (bills.isEmpty()) {
            output.println("No bills found.");
            return;
        }

        output.printf(
                "%-10s %-12s %-15s %-12s %-12s %s%n",
                "Bill No.",
                "Type",
                "Amount",
                "Due Date",
                "State",
                "Provider");

        for (Bill bill : bills) {
            output.printf(
                    "%-10d %-12s %-15s %-12s %-12s %s%n",
                    bill.getId(),
                    bill.getType(),
                    formatAmount(
                            bill.getAmount()),
                    bill.getDueDate()
                            .format(DATE_FORMATTER),
                    bill.getState(),
                    bill.getProvider());
        }
    }

    private void printPayments(
            List<Payment> payments) {
        if (payments.isEmpty()) {
            output.println(
                    "No payment history found.");
            return;
        }

        output.printf(
                "%-10s %-15s %-15s %-15s %s%n",
                "No.",
                "Amount",
                "Payment Date",
                "State",
                "Bill Id");

        for (Payment payment : payments) {
            output.printf(
                    "%-10d %-15s %-15s %-15s %d%n",
                    payment.getId(),
                    formatAmount(
                            payment.getAmount()),
                    payment.getPaymentDate()
                            .format(DATE_FORMATTER),
                    payment.getState(),
                    payment.getBillId());
        }
    }

    private BigDecimal parseAmount(
            String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid amount: " + value);
        }
    }

    private long parseId(String value) {
        try {
            long id = Long.parseLong(value);

            if (id <= 0) {
                throw new IllegalArgumentException(
                        "Id must be greater than zero");
            }

            return id;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid id: " + value);
        }
    }

    private BillType parseBillType(
            String value) {
        try {
            return BillType.valueOf(
                    value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid bill type: " + value);
        }
    }

    private LocalDate parseDate(
            String value) {
        try {
            return LocalDate.parse(
                    value,
                    DATE_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Invalid date: "
                            + value
                            + ". Expected format: dd/MM/yyyy");
        }
    }

    private void validateArgumentCount(
            String[] tokens,
            int expected) {
        if (tokens.length != expected) {
            throw new IllegalArgumentException(
                    "Invalid number of arguments");
        }
    }

    private String joinTokens(
            String[] tokens,
            int startIndex) {
        StringBuilder builder = new StringBuilder();

        for (int index = startIndex; index < tokens.length; index++) {

            if (builder.length() > 0) {
                builder.append(" ");
            }

            builder.append(
                    tokens[index]);
        }

        return builder.toString();
    }

    private String formatAmount(
            BigDecimal amount) {
        return amount.stripTrailingZeros()
                .toPlainString();
    }
}