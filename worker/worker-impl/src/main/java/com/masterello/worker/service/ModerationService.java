package com.masterello.worker.service;

import com.masterello.worker.dto.ModerationResult;

public interface ModerationService {
    ModerationResult moderateUserInput(String content);
}

