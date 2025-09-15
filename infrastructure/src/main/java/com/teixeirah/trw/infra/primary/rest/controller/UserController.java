package com.teixeirah.trw.infra.primary.rest.controller;

import com.teixeirah.trw.application.ports.input.RegisterUserInputPort;
import com.teixeirah.trw.infra.bootstrap.AppProperties;
import com.teixeirah.trw.infra.primary.rest.dto.Dtos;
import com.teixeirah.trw.infra.primary.rest.mapper.ApiMappers;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
class UserController {

    private final RegisterUserInputPort registerPort;
    private final AppProperties riskProps;

    @PostMapping("/users")
    public ResponseEntity<Void> register(@Valid @RequestBody Dtos.RegisterUserRequest req) {

        final var cmd = ApiMappers.toCommand(req, riskProps);
        registerPort.handle(cmd);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}


