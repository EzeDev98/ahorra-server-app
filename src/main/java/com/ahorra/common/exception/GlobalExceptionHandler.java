package com.ahorra.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Map<Class<? extends RuntimeException>, HttpStatus> SIMPLE_STATUS_MAPPINGS = Map.ofEntries(
            Map.entry(RegistrationException.class, HttpStatus.BAD_REQUEST),
            Map.entry(UserAlreadyExistException.class, HttpStatus.CONFLICT),
            Map.entry(RefreshTokenInvalidException.class, HttpStatus.UNAUTHORIZED),
            Map.entry(AuthenticationException.class, HttpStatus.UNAUTHORIZED),
            Map.entry(RequiredFieldsException.class, HttpStatus.BAD_REQUEST),
            Map.entry(MaxAttemptExceededException.class, HttpStatus.TOO_MANY_REQUESTS),
            Map.entry(UserNotFoundException.class, HttpStatus.NOT_FOUND),
            Map.entry(BadRequestException.class, HttpStatus.BAD_REQUEST)
    );

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ProblemDetail handleEmailNotVerifiedException(EmailNotVerifiedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setProperty("code", "EMAIL_NOT_VERIFIED");
        problem.setProperty("email", ex.getEmail());
        return problem;
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleMappedRuntimeException(RuntimeException ex) {
        HttpStatus status = SIMPLE_STATUS_MAPPINGS.get(ex.getClass());
        if (status == null) {
            return buildUnexpectedErrorResponse(ex);
        }
        return ProblemDetail.forStatusAndDetail(status, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Access Denied");
        return problem;
    }

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleDataAccessException(DataAccessException ex) {
        log.error("Database connectivity failure", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "The service is temporary not available. Please try again shortly");
        problem.setTitle("Service Temporarily Unavailable");
        return problem;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail handleUnreadableInput(Exception ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "The request could not be read or has an invalid value");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            return errorResponse.getBody();
        }
        return buildUnexpectedErrorResponse(ex);
    }

    private ProblemDetail buildUnexpectedErrorResponse(Exception ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("[traceId={}] handled exception", traceId, ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later");
        problem.setProperty("traceId", traceId);
        return problem;
    }
}
