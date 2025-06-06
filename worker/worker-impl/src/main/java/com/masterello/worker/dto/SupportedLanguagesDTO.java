package com.masterello.worker.dto;

import com.masterello.worker.domain.Language;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SupportedLanguagesDTO {
    private List<Language> languages;
}
