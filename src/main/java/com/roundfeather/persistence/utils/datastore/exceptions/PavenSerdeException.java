package com.roundfeather.persistence.utils.datastore.exceptions;

public class PavenSerdeException extends RuntimeException {
    public PavenSerdeException(String message) {
        super(message);
    }

    public PavenSerdeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PavenSerdeException(Throwable cause) {
        super(cause);
    }
}
