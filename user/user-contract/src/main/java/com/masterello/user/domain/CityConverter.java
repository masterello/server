package com.masterello.user.domain;

import com.masterello.user.value.City;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CityConverter implements AttributeConverter<City, String> {
    @Override
    public String convertToDatabaseColumn(City attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public City convertToEntityAttribute(String code) {
        return code == null ? null : City.getCity(code);
    }
}
