package com.masterello.user.domain;

import com.masterello.user.value.UserStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(UserStatus attribute) {
        return attribute.getCode();
    }

    @Override
    public UserStatus convertToEntityAttribute(Integer code) {
        return UserStatus.fromCode(code);
    }
}
