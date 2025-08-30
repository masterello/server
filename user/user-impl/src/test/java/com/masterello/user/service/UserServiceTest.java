package com.masterello.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.masterello.commons.async.MasterelloEventPublisher;
import com.masterello.commons.core.json.exception.PatchFailedException;
import com.masterello.commons.core.json.service.PatchService;
import com.masterello.user.domain.MasterelloUserEntity;
import com.masterello.user.event.UserStatusChangedEvent;
import com.masterello.user.exception.InvalidUserUpdateException;
import com.masterello.user.exception.UserAlreadyExistsException;
import com.masterello.user.exception.UserNotFoundException;
import com.masterello.user.exception.UserStatusCannotBeUpdatedException;
import com.masterello.user.repository.UserRepository;
import com.masterello.user.value.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static com.masterello.user.util.TestDataProvider.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Spy
    private PatchService patchService = new PatchService(new ObjectMapper(), null);
    @InjectMocks
    private UserService userService;
    @Mock
    private MasterelloEventPublisher publisher;


    @Test
    public void retrieveUserByUuid_userNotFound() {
        //GIVEN
        when(userRepository.findById(any()))
                .thenReturn(Optional.empty());

        //WHEN
        assertThrows(UserNotFoundException.class, () -> userService.retrieveUserByUuid(UUID.randomUUID()));

        //THEN
        verify(userRepository, times(1)).findById(any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void retrieveUserByUuid() {
        //GIVEN
        var user = buildUser();

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(user));

        //WHEN
        var retrievedUser = userService.retrieveUserByUuid(UUID.randomUUID());

        //THEN
        assertEquals(user.getCity(), retrievedUser.getCity());
        assertEquals(user.getLastname(), retrievedUser.getLastname());
        assertEquals(user.getName(), retrievedUser.getName());
        assertEquals(user.getPhone(), retrievedUser.getPhone());
        assertEquals(user.getEmail(), retrievedUser.getEmail());
        assertEquals(user.getPassword(), retrievedUser.getPassword());

        verify(userRepository, times(1)).findById(any());

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void createUser_already_created() {
        //GIVEN
        MasterelloUserEntity user = buildUser();
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        //WHEN
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(user));

        //THEN
        verify(userRepository, times(1)).existsByEmail(user.getEmail());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void createUser() {
        //GIVEN
        var user = buildUser();
        var savedUser = buildCompleteUser();
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.saveAndFlush(any())).thenReturn(savedUser);

        //WHEN
        var createdUser = userService.createUser(user);

        //THEN
        assertEquals(user.getCity(), createdUser.getCity());
        assertEquals(user.getLastname(), createdUser.getLastname());
        assertEquals(user.getName(), createdUser.getName());
        assertEquals(user.getPhone(), createdUser.getPhone());
        assertEquals(user.getEmail(), createdUser.getEmail());
        assertEquals(user.getPassword(), createdUser.getPassword());

        verify(userRepository, times(1)).existsByEmail(any());
        verify(userRepository, times(1)).saveAndFlush(any());

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void updateUser_no_user() throws IOException {
        //GIVEN
        when(userRepository.findById(any()))
                .thenReturn(Optional.empty());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree("""
                [
                    {"op":"replace","path":"/city","value":"Warsaw"}
                ]
                """);

        //WHEN
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(UUID.randomUUID(), JsonPatch.fromJson(node)));

        //THEN
        verify(userRepository, times(1)).findById(any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void updateUser() throws IOException {
        //GIVEN
        var user = buildUser();
        var savedUser = buildCompleteUser();

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(user));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode patch = mapper.readTree("""
                [
                    {"op":"replace","path":"/city","value":"HH"}
                ]
                """);
        when(userRepository.saveAndFlush(any())).thenReturn(savedUser);

        //WHEN
        userService.updateUser(UUID.randomUUID(), JsonPatch.fromJson(patch));

        //THEN
        verify(userRepository, times(1)).findById(any());
        verify(userRepository, times(1)).saveAndFlush(any());

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void updateUser_error() throws IOException {
        //GIVEN
        var user = buildUser();

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(user));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode patch = mapper.readTree("""
                [
                    {"op":"replace","path":"/aaa","value":"Warsaw"}
                ]
                """);

        //WHEN
        InvalidUserUpdateException invalidUserUpdateException = assertThrows(InvalidUserUpdateException.class, () -> userService.updateUser(UUID.randomUUID(), JsonPatch.fromJson(patch)));
        assertTrue(invalidUserUpdateException.getCause().getClass().isAssignableFrom(PatchFailedException.class));


        //THEN
        verify(userRepository, times(1)).findById(any());

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void updateUser_error_forbiddenFields() throws IOException {
        //GIVEN
        var user = buildUser();

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(user));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode patch = mapper.readTree("""
                [
                    {"op":"replace","path":"/email","value":"Email is not patchable"}
                ]
                """);

        //WHEN
        InvalidUserUpdateException invalidUserUpdateException = assertThrows(InvalidUserUpdateException.class, () -> userService.updateUser(UUID.randomUUID(), JsonPatch.fromJson(patch)));

        assertTrue(invalidUserUpdateException.getCause().getClass().isAssignableFrom(PatchFailedException.class));
        assertEquals("Fields are not supported for patching: [email]", invalidUserUpdateException.getCause().getMessage());

        //THEN
        verify(userRepository, times(1)).findById(any());

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void resetPassword_noUser() {
        //GIVEN
        when(userRepository.findById(any()))
                .thenReturn(Optional.empty());

        //WHEN
        //THEN
        assertThrows(UserNotFoundException.class, () ->
                userService.resetPassword(VERIFIED_USER, "test"));
    }

    @Test
    public void resetPassword() {
        //GIVEN
        var user = buildUser();

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.encode(any())).thenReturn("password");

        //WHEN
        userService.resetPassword(VERIFIED_USER, "password");
        //THEN
        verify(userRepository, times(1)).findById(VERIFIED_USER);
        verify(passwordEncoder, times(1)).encode("password");
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void changeStatus_shouldThrowExceptionWhenStatusIsTheSame() {
        UUID userId = UUID.randomUUID();
        UserStatus status = UserStatus.ACTIVE;
        var user = buildUser();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(UserStatusCannotBeUpdatedException.class, () -> userService.changeStatus(userId, status));

        verify(userRepository, never()).save(any(MasterelloUserEntity.class));
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void changeStatus_shouldUpdateStatusAndPublishEvent() {
        ArgumentCaptor<UserStatusChangedEvent> captor = ArgumentCaptor.forClass(UserStatusChangedEvent.class);
        doNothing().when(publisher).publishEvent(captor.capture());

        UUID userId = UUID.randomUUID();
        UserStatus newStatus = UserStatus.BANNED;
        var user = buildUser();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        var updatedUser = buildUser();
        updatedUser.setStatus(newStatus);
        when(userRepository.save(user)).thenReturn(updatedUser);

        userService.changeStatus(userId, newStatus);

        verify(userRepository).save(user);
        UserStatusChangedEvent capturedEvent = captor.getValue();
        assertEquals(updatedUser, capturedEvent.getUpdatedUser());
    }
}
