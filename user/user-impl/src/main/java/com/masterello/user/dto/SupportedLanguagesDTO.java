package com.masterello.user.dto;

import com.masterello.user.value.Language;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class SupportedLanguagesDTO {
    private Set<Language> languages;
}
