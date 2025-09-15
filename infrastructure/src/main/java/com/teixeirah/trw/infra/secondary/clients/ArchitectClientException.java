package com.teixeirah.trw.infra.secondary.clients;

public class ArchitectClientException extends RuntimeException {
    private final int status;

    public ArchitectClientException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}


