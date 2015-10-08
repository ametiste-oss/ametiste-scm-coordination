package org.ametiste.scm.coordinator.accessor;

/**
 * {@code FetchSubscribersException} signals about fetch operation failure.
 * This exception covers all errors that can happen during SubscribersFetcher operations.
 */
public class FetchSubscribersException extends RuntimeException {

    public FetchSubscribersException(String message) {
        super(message);
    }

    public FetchSubscribersException(String message, Throwable cause) {
        super(message, cause);
    }
}
