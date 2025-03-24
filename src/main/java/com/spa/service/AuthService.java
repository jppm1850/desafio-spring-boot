package com.spa.service;

import com.spa.model.dto.LoginRequest;
import com.spa.model.dto.LoginResponse;
import reactor.core.publisher.Mono;

public interface AuthService {

    public Mono<LoginResponse> login(LoginRequest request);
}
