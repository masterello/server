package com.masterello.user.service;

import com.masterello.user.value.MasterelloUser;


public interface AuthNService {

    MasterelloUser googleSignup(String email, String name, String lastName);
}
