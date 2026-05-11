package com.example.libraryapi.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

/**
 * Global exception handler for the LibraryAPI application. This class handles various exceptions
 * thrown by the application and returns appropriate HTTP responses with detailed error information.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles AuthorNotFoundException and returns a 404 Not Found response with error details.
     *
     * @param ex The AuthorNotFoundException that was thrown
     * @param request The HttpServletRequest that resulted in the exception
     * @return A ProblemDetail object containing error details to be returned in the response body
     */
    @ExceptionHandler(AuthorNotFoundException.class)
    public ProblemDetail handleAuthorNotFoundException(
            AuthorNotFoundException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Handles BookNotFoundException and returns a 404 Not Found response with error details.
     *
     * @param ex The BookNotFoundException that was thrown
     * @param request The HttpServletRequest that resulted in the exception
     * @return A ProblemDetail object containing error details to be returned in the response body
     */
    @ExceptionHandler(BookNotFoundException.class)
    public ProblemDetail handleBookNotFoundException(
            BookNotFoundException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Handles LoanNotFoundException and returns a 404 Not Found response with error details.
     *
     * @param ex The LoanNotFoundException that was thrown
     * @param request The HttpServletRequest that resulted in the exception
     * @return A ProblemDetail object containing error details to be returned in the response body
     */
    @ExceptionHandler(LoanNotFoundException.class)
    public ProblemDetail handleLoanNotFoundException(
            LoanNotFoundException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Handles NoResourceFoundException and returns a 404 Not Found response with error details.
     *
     * @param ex The NoResourceFoundException that was thrown
     * @param request The HttpServletRequest that resulted in the exception
     * @return A ProblemDetail object containing error details to be returned in the response body
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail noResourceFoundException(
            NoResourceFoundException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.NOT_FOUND, "Resource not found", request);
    }

    /**
     * Handles BookNotAvailableException and returns a 409 Conflict response with error details.
     * This exception is typically thrown when a client attempts to create a loan for a book that is
     * not currently available (e.g., because it is already loaned out).
     *
     * @param ex The BookNotAvailableException that was thrown
     * @param request The HttpServletRequest that resulted in the exception
     * @return A ProblemDetail object containing error details to be returned in the response body
     */
    @ExceptionHandler(BookNotAvailableException.class)
    public ProblemDetail handleBookNotAvailableException(
            BookNotAvailableException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Handles MethodArgumentNotValidException and returns a 400 Bad Request response with error
     * details.
     *
     * @param ex The MethodArgumentNotValidException that was thrown
     * @param request The HttpServletRequest that resulted in the exception
     * @return A ProblemDetail object containing error details to be returned in the response body
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        var message = String.join("\n", errors);
        return buildProblem(
                HttpStatus.BAD_REQUEST,
                message,
                request);
    }

    /**
     * Handles HttpMessageNotReadableException and returns a 400 Bad Request response with error
     * details. This exception is typically thrown when the request body cannot be parsed (e.g., due
     * to malformed JSON).
     *
     * @param ex The HttpMessageNotReadableException that was thrown
     * @param request The HttpServletRequest that resulted in the exception
     * @return A ProblemDetail object containing error details to be returned in the response body
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMismatchedInputException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "Can't parse request body. "
                        + "Please ensure the JSON structure is correct \n"
                        + "and all required fields are provided.",
                request);
    }

    /**
     * Handles MethodArgumentTypeMismatchException and returns a 400 Bad Request response with error
     * details. This exception is typically thrown when a request parameter cannot be converted to
     * the expected type (e.g., when a non-numeric value is provided for a numeric parameter).
     *
     * @param ex The MethodArgumentTypeMismatchException that was thrown
     * @param request The HttpServletRequest that resulted in the exception
     * @return A ProblemDetail object containing error details to be returned in the response body
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'.",
                request);
    }

    /**
     * Handles HttpMediaTypeNotSupportedException and returns a 415 Unsupported Media Type response
     * with error details. This exception is typically thrown when a client sends a request with an
     * unsupported Content-Type header (e.g., when the client sends XML instead of JSON).
     *
     * @param ex The HttpMediaTypeNotSupportedException that was thrown
     * @param request The HttpServletRequest that resulted in the exception
     * @return A ProblemDetail object containing error details to be returned in the response body
     */
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleInvalidMediaType(Exception ex, HttpServletRequest request) {
        return buildProblem(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Unsupported media type. Please ensure your Content-Type header is set correctly.",
                request);
    }

    /**
     * Handles AccessDeniedException and returns a 403 Forbidden response with error details.
     * This exception is thrown when an authenticated user attempts an operation they are not
     * authorized to perform (e.g., accessing another user's loan).
     *
     * @param ex The AccessDeniedException that was thrown
     * @param request The HttpServletRequest that resulted in the exception
     * @return A ProblemDetail object containing error details to be returned in the response body
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex,
            HttpServletRequest request) {
        return buildProblem(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    /**
     * Handles any unexpected exceptions that are not explicitly handled by other methods and
     * returns a 500 Internal Server Error response with error details. This serves as a catch-all
     * handler for any exceptions that may occur in the application.
     *
     * <p>Error details are logged for debugging purposes, but the response to the client contains a
     * generic error message to avoid exposing sensitive information about the server or application
     * internals.
     *
     * @param ex The exception that was thrown
     * @param request The HttpServletRequest that resulted in the exception
     * @return A ProblemDetail object containing error details to be returned in the response body
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnknownException(Exception ex, HttpServletRequest request) {
        LOG.error("Unexpected error: ", ex);
        return buildProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.",
                request);
    }

    private ProblemDetail buildProblem(
            HttpStatus status, String message, HttpServletRequest request) {
        var problem = ProblemDetail.forStatus(status);
        problem.setProperty("timestamp", LocalDateTime.now().toString());
        problem.setProperty("error", status.getReasonPhrase());
        problem.setProperty("message", message);
        problem.setProperty("path", request.getRequestURI());
        return problem;
    }
}
