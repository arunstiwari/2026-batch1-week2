package com.fil.week2.exception;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
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
    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    private String currentTraceId() {
        Span currentSpan = tracer.currentSpan();
        return currentSpan != null ? currentSpan.context().traceId():  null;
    }



    @ExceptionHandler({CustomerNotFoundException.class,
            KycNotFoundException.class,
            AccountNotFoundException.class})
    ProblemDetail notFound(RuntimeException exception) {
        ProblemDetail problemDetail = problem(HttpStatus.NOT_FOUND, "Not found", exception.getMessage());
        return stamp(problemDetail);
    }

    private ProblemDetail stamp(ProblemDetail problemDetail) {
        problemDetail.setProperty("traceId", currentTraceId());
        return problemDetail;
    }

    @ExceptionHandler({InvalidStateTransitionException.class,
            AccountOpeningNotPermittedException.class,
            WithdrawalNotPermittedException.class})
    ProblemDetail notAllowed(RuntimeException exception) {
        return stamp(problem(HttpStatus.CONFLICT, "Not allowed in the current state", exception.getMessage()));
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
