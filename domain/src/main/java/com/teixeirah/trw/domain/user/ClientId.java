package com.teixeirah.trw.domain.user;

public record ClientId(String value) {
  public ClientId {
    if (value == null || value.isBlank()) throw new IllegalArgumentException("clientId");
  }
}


