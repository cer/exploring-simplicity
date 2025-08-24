#!/bin/bash

set -e

echo "Running pre-commit checks..."

cd travel-booking-pojo

echo "Running tests..."
./gradlew test

echo "Running integration tests..."
./gradlew integrationTest

echo "Pre-commit checks passed!"