package com.masterello.worker.dto;

import com.masterello.worker.domain.Language;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class SupportedLanguagesDTO {
    private Set<Language> languages;
}
