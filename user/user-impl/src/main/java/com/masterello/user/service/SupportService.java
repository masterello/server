package com.masterello.user.service;

import com.masterello.monitoring.AlertLevel;
import com.masterello.monitoring.AlertMessage;
import com.masterello.monitoring.slack.service.SlackAlertSender;
import com.masterello.user.dto.SupportRequestDTO;
import com.masterello.user.exception.RequestFoundException;
import com.masterello.user.mapper.SupportRequestMapper;
import com.masterello.user.repository.SupportRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class SupportService {

    private final SupportRequestMapper supportRequestMapper;
    private final SupportRequestRepository supportRequestRepository;
    private final SlackAlertSender slackAlertSender;

    public void storeSupportRequest(SupportRequestDTO request) {
        log.info("Received new support request");

        var entity = supportRequestMapper.mapDtoToEntity(request);
        supportRequestRepository.save(entity);
        slackAlertSender.sendAlert(new AlertMessage(AlertLevel.INFO, "New support request", null));
        log.info("Successfully stored new support request");
    }

    public List<SupportRequestDTO> receiveAllSupportRequests() {
        log.info("Retrieving all support requests");
        var entities = supportRequestRepository.findAll();
        log.info("Successfully retrieved all support requests");
        return entities.stream().map(supportRequestMapper::mapEntityToDto)
                .toList();
    }

    public List<SupportRequestDTO> receiveAllUnprocessedSupportRequests() {
        log.info("Retrieving all unprocessed support requests");

        var entities = supportRequestRepository.findByProcessedFalse();
        log.info("Successfully retrieved all unprocessed support requests");
        return entities.stream().map(supportRequestMapper::mapEntityToDto)
                .toList();
    }

    public void markRequestProcessed(UUID requestUuid) {
        log.info("Marking request as processed");
        var entity = supportRequestRepository.findById(requestUuid)
                .orElseThrow(()-> new RequestFoundException("Request not found"));
        entity.setProcessed(true);
        supportRequestRepository.save(entity);
        log.info("Processed request successfully");
    }
}
