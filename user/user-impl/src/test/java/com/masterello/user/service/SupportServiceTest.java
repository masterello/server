package com.masterello.user.service;

import com.masterello.commons.monitoring.AlertSender;
import com.masterello.user.dto.SupportRequestDTO;
import com.masterello.user.exception.RequestFoundException;
import com.masterello.user.mapper.SupportRequestMapper;
import com.masterello.user.repository.SupportRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.masterello.user.util.TestDataProvider.buildSupportRequest;
import static com.masterello.user.util.TestDataProvider.buildSupportRequestDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SupportServiceTest {

    @Mock
    private SupportRequestRepository supportRequestRepository;

    @Mock
    private SupportRequestMapper supportRequestMapper;

    @Mock
    private AlertSender alertSender;

    @InjectMocks
    private SupportService supportService;

    @Test
    public void testStoreSupportRequest() {
        //GIVEN
        var requestDto = new SupportRequestDTO();
        var supportEntity = buildSupportRequest();
        when(supportRequestMapper.mapDtoToEntity(any())).thenReturn(supportEntity);

        //WHEN
        supportService.storeSupportRequest(requestDto);

        //THEN
        verify(supportRequestMapper, times(1)).mapDtoToEntity(requestDto);
        verify(supportRequestRepository, times(1)).save(supportEntity);
    }

    @Test
    public void testMarkRequestProcessedNotFound() {
        //GIVEN
        var requestUuid = UUID.randomUUID();
        when(supportRequestRepository.findById(requestUuid)).thenReturn(Optional.empty());

        //WHEN
        assertThrows(RequestFoundException.class, () -> supportService.markRequestProcessed(requestUuid));

        //THEN
        verify(supportRequestRepository, times(1)).findById(requestUuid);
        verify(supportRequestRepository, times(0)).save(any());
    }

    @Test
    public void testMarkRequestProcessed() {
        //GIVEN
        var requestUuid = UUID.randomUUID();
        var supportEntity = buildSupportRequest();
        when(supportRequestRepository.findById(requestUuid)).thenReturn(Optional.of(supportEntity));

        //WHEN
        supportService.markRequestProcessed(requestUuid);

        //THEN
        verify(supportRequestRepository, times(1)).findById(requestUuid);
        verify(supportRequestRepository, times(1)).save(supportEntity);
    }

    @Test
    public void testFindAllUnprocessedSupportRequests() {
        //GIVEN
        var supportEntity = buildSupportRequest();
        when(supportRequestRepository.findByProcessedFalse())
                .thenReturn(List.of(supportEntity, supportEntity));
        when(supportRequestMapper.mapEntityToDto(supportEntity))
                .thenReturn(buildSupportRequestDto());

        //WHEN
        var entities = supportService.receiveAllUnprocessedSupportRequests();

        //THEN
        assertEquals(2, entities.size());
        var entity = entities.get(0);
        assertEquals(entity.getTitle(), supportEntity.getTitle());
        assertEquals(entity.getMessage(), supportEntity.getMessage());
        assertEquals(entity.getPhone(), supportEntity.getPhone());
        verify(supportRequestRepository, times(1)).findByProcessedFalse();
        verify(supportRequestMapper, times(2)).mapEntityToDto(supportEntity);
    }

    @Test
    public void testFindAllUnprocessedSupportRequestNoRequests() {
        //GIVEN
        var supportEntity = buildSupportRequest();
        when(supportRequestRepository.findByProcessedFalse())
                .thenReturn(List.of());

        //WHEN
        var entities = supportService.receiveAllUnprocessedSupportRequests();

        //THEN
        assertEquals(0, entities.size());
        verify(supportRequestRepository, times(1)).findByProcessedFalse();
        verify(supportRequestMapper, times(0)).mapEntityToDto(supportEntity);
    }

    @Test
    public void testFindAllSupportRequests() {
        //GIVEN
        var supportEntity = buildSupportRequest();
        when(supportRequestRepository.findAll())
                .thenReturn(List.of(supportEntity, supportEntity));
        when(supportRequestMapper.mapEntityToDto(supportEntity))
                .thenReturn(buildSupportRequestDto());

        //WHEN
        var entities = supportService.receiveAllSupportRequests();

        //THEN
        assertEquals(2, entities.size());
        var entity = entities.get(0);
        assertEquals(entity.getTitle(), supportEntity.getTitle());
        assertEquals(entity.getMessage(), supportEntity.getMessage());
        assertEquals(entity.getPhone(), supportEntity.getPhone());
        verify(supportRequestRepository, times(1)).findAll();
        verify(supportRequestMapper, times(2)).mapEntityToDto(supportEntity);
    }

    @Test
    public void testFindAllSupportRequestNoRequests() {
        //GIVEN
        var supportEntity = buildSupportRequest();
        when(supportRequestRepository.findAll())
                .thenReturn(List.of());

        //WHEN
        var entities = supportService.receiveAllSupportRequests();

        //THEN
        assertEquals(0, entities.size());
        verify(supportRequestRepository, times(1)).findAll();
        verify(supportRequestMapper, times(0)).mapEntityToDto(supportEntity);
    }
}
