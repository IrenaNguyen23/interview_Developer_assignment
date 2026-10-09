#!/usr/bin/env bash

set -e

mvn -q -DskipTests package

java -jar target/bill-payment-system-1.0-SNAPSHOT.jar