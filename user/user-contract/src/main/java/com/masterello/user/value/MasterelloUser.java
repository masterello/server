package com.masterello.user.value;

import java.util.List;
import java.util.Set;
import java.util.UUID;


public interface MasterelloUser {

    public UUID getUuid();

    public String getEmail();

    public String getPassword();

    public String getTitle();

    public String getName();

    public String getLastname();

    public Set<Role> getRoles();

    String getPhone();

    Country getCountry();

    City getCity();

    UserStatus getStatus();

    boolean isEmailVerified();

    List<Language> getLanguages();

    String getUsername();

    boolean isAccountNonLocked();

    boolean isEnabled();

    boolean isAccountNonExpired();

    boolean isCredentialsNonExpired();
}
