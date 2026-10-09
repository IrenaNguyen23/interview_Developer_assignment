# interview_Developer_assignment

## Technology

- Java 21
- Maven
- JUnit 5 for testing

## Build

Run:

```bash
mvn clean package
```

## Test

Run all automated tests with:

```bash
mvn clean test
```

## Run

The easiest way to start the application on Linux or macOS is:

```bash
./run.sh
```

Alternatively:

```bash
mvn clean package
java -jar target/bill-payment-system-1.0-SNAPSHOT.jar
```


## Supported Commands

### Add funds

```text
CASH_IN <AMOUNT>
```

Example:

```text
CASH_IN 1000000
```

### Create a bill

```text
CREATE_BILL <TYPE> <AMOUNT> <DUE_DATE> <PROVIDER>
```

Example:

```text
CREATE_BILL ELECTRIC 200000 25/10/2026 EVN HCMC
```

Supported bill types:

```text
ELECTRIC
WATER
INTERNET
PHONE
OTHER
```

Dates use the format:

```text
dd/MM/yyyy
```

### Update a bill

```text
UPDATE_BILL <ID> <TYPE> <AMOUNT> <DUE_DATE> <PROVIDER>
```

Example:

```text
UPDATE_BILL 1 ELECTRIC 250000 30/10/2026 EVN HCMC
```

A bill that has already been paid cannot be updated.

### Delete a bill

```text
DELETE_BILL <ID>
```

Example:

```text
DELETE_BILL 1
```

A bill that has already been paid cannot be deleted in order to preserve payment-history consistency.

### List bills

```text
LIST_BILL
```

### Pay a bill

```text
PAY <BILL_ID>
```

Example:

```text
PAY 1
```

### Pay multiple bills

```text
PAY <BILL_ID> <BILL_ID> ...
```

Example:

```text
PAY 1 2 3
```

### List unpaid bills by due date

```text
DUE_DATE
```

### Search by provider

```text
SEARCH_BILL_BY_PROVIDER <PROVIDER>
```

Example:

```text
SEARCH_BILL_BY_PROVIDER VNPT
```

### List payment history

```text
LIST_PAYMENT
```

### Exit

```text
EXIT
```

## Example

```text
CASH_IN 1000000
CREATE_BILL ELECTRIC 200000 25/10/2026 EVN HCMC
CREATE_BILL WATER 175000 30/10/2026 SAVACO HCMC
CREATE_BILL INTERNET 800000 30/11/2026 VNPT

LIST_BILL

PAY 1

PAY 2 3

DUE_DATE

SEARCH_BILL_BY_PROVIDER VNPT

LIST_PAYMENT

EXIT
```