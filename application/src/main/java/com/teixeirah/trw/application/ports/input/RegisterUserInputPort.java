package com.teixeirah.trw.application.ports.input;

import com.teixeirah.trw.application.dto.RegisterUserCommand;

public interface RegisterUserInputPort {
    void handle(RegisterUserCommand cmd);
}


