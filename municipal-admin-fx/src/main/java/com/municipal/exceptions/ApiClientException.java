package com.municipal.exceptions;

/**
 * Represents an error returned by the backend API when calling it from the
 * JavaFX client.
 */
public class ApiClientException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public ApiClientException(int statusCode, String responseBody) {
        super("API call failed with status " + statusCode);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public ApiClientException(int statusCode, String responseBody, Throwable cause) {
        super("API call failed with status " + statusCode, cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
