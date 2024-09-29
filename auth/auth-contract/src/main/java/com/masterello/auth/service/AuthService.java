package com.masterello.auth.service;

import com.masterello.auth.data.AuthData;

import java.util.Optional;

public interface AuthService {

    Optional<AuthData> validateToken(String token);
}
