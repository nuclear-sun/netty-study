package org.sun.herostory.exception;

public class AuthFailedException extends RuntimeException {

    public AuthFailedException() {
    }

    public AuthFailedException(String msg) {
        super(msg);
    }
}
