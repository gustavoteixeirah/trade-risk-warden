package com.teixeirah.trw.application.usecases;

import com.teixeirah.trw.application.dto.RegisterUserCommand;

public interface RegisterUserUseCase {
  void handle(RegisterUserCommand cmd);
}


