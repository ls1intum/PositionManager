package de.tum.cit.aet.core.exceptions;

public class AccessDeniedException extends org.springframework.security.access.AccessDeniedException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
