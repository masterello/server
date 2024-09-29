package com.masterello.user.controller;

import com.masterello.user.dto.SupportedLanguagesDTO;
import com.masterello.user.value.Language;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LanguagesController {

    @RequestMapping(value = "/user/supported-languages", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public SupportedLanguagesDTO getSupportedLanguages() {
        return SupportedLanguagesDTO.builder()
                .languages(Set.of(Language.values()))
                .build();
    }

}
