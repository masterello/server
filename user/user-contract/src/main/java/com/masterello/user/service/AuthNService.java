package com.masterello.user.service;

import com.masterello.user.value.MasterelloUser;


public interface AuthNService {

    MasterelloUser googleSignup(String email);

    boolean checkPassword(String rawPassword, String encodedPassword);
}
