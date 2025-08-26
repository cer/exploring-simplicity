package com.travelbooking.testutils.kafka;

import java.util.List;
import java.util.stream.Collectors;

public class MultiAssertionError extends AssertionError {
  private final List<AssertionError> allErrors;

  public MultiAssertionError(String message, List<AssertionError> allErrors) {
    super(createMessage(message, allErrors));
    this.allErrors = allErrors;
  }

  private static String createMessage(String message, List<AssertionError> allErrors) {
    return message + "\n" + allErrors.stream()
        .map(e -> "- " + e.getMessage())
        .collect(Collectors.joining(",\n"));
  }

  public List<AssertionError> getAllErrors() {
    return allErrors;
  }
}
