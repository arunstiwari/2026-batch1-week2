package com.fil.week2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Without this, every domain exception left the application as a 500 and the
 * careful distinction between "not found" and "not allowed" was thrown away.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({CustomerNotFoundException.class,
            KycNotFoundException.class,
            AccountNotFoundException.class})
    ProblemDetail notFound(RuntimeException exception) {
        return problem(HttpStatus.NOT_FOUND, "Not found", exception.getMessage());
    }

    @ExceptionHandler({InvalidStateTransitionException.class,
            AccountOpeningNotPermittedException.class,
            WithdrawalNotPermittedException.class})
    ProblemDetail notAllowed(RuntimeException exception) {
        return problem(HttpStatus.CONFLICT, "Not allowed in the current state", exception.getMessage());
    }

    /** The balance is deliberately left out of the response. */
    @ExceptionHandler(InsufficientFundsException.class)
    ProblemDetail insufficientFunds(InsufficientFundsException exception) {
        return problem(HttpStatus.CONFLICT, "Insufficient funds",
                "Account " + exception.getAccountNumber() + " cannot be overdrawn");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(IllegalArgumentException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", exception.getMessage());
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        return problemDetail;
    }
}
