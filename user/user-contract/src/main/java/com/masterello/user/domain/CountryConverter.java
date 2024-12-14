package com.masterello.user.domain;

import com.masterello.user.value.Country;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CountryConverter implements AttributeConverter<Country, String> {
    @Override
    public String convertToDatabaseColumn(Country attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public Country convertToEntityAttribute(String code) {
        return code == null ? null : Country.getCountry(code);
    }
}
