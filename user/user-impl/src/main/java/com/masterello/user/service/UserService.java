package com.masterello.user.service;

import com.github.fge.jsonpatch.JsonPatch;
import com.masterello.commons.core.json.service.PatchService;
import com.masterello.user.domain.MasterelloUserEntity;
import com.masterello.user.event.UserStatusChangedEvent;
import com.masterello.user.exception.InvalidUserUpdateException;
import com.masterello.user.exception.SamePasswordException;
import com.masterello.user.exception.UserAlreadyExistsException;
import com.masterello.user.exception.UserHasRequestedRoleException;
import com.masterello.user.exception.UserNotFoundException;
import com.masterello.user.exception.UserStatusCannotBeUpdatedException;
import com.masterello.user.repository.UserRepository;
import com.masterello.user.value.MasterelloUser;
import com.masterello.user.value.Role;
import com.masterello.user.value.UserStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserService implements MasterelloUserService {

    private final UserRepository userRepository;
    private final PatchService patchService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public MasterelloUser createUser(MasterelloUserEntity user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException();
        }
        return userRepository.saveAndFlush(user);
    }

    public MasterelloUser addRole(UUID userId, Role role) {
        MasterelloUserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found by id"));

        if (user.getRoles().contains(role)) {
            throw new UserHasRequestedRoleException(role);
        }
        Set<Role> roles = new HashSet<>(user.getRoles());
        roles.add(role);
        user.setRoles(roles);
        return userRepository.save(user);
    }

    public void updatePassword(UUID userId, String oldPassword, String newPassword) {
        MasterelloUserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found by id"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is not correct");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new SamePasswordException("New password is same");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public MasterelloUser retrieveUserByUuid(UUID userUuid) {
        log.info("Retrieving user by uuid {}", userUuid);

        return userRepository.findById(userUuid)
                .orElseThrow(() -> {
                    log.warn("User with uuid {} not found in the system", userUuid);
                    return new UserNotFoundException("User not found by id");
                });
    }

    public MasterelloUser updateUser(UUID uuid, JsonPatch patch) {
        var user = userRepository.findById(uuid)
                .orElseThrow(() -> {
                    log.warn("User with uuid {} not found in the system", uuid);
                    return new UserNotFoundException("User not found by id");
                });

        MasterelloUserEntity patchedUser;
        try {
            patchedUser = patchService.applyPatch(patch, user, MasterelloUserEntity.class);
        } catch (Exception ex) {
            throw new InvalidUserUpdateException("Error when applying update to user", ex);
        }
        return userRepository.saveAndFlush(patchedUser);
    }

    public void resetPassword(UUID userUuid, String password) {
        MasterelloUserEntity user = userRepository.findById(userUuid)
                .orElseThrow(() -> new UserNotFoundException("User not found by id"));

        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    @Override
    public Optional<MasterelloUser> findById(UUID id) {
        return userRepository.findById(id)
                .map(Function.identity());
    }

    @Override
    public Optional<MasterelloUser> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(Function.identity());
    }

    @Override
    public Map<UUID, MasterelloUser> findAllByIds(Set<UUID> ids) {
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(MasterelloUserEntity::getUuid, Function.identity()));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void changeStatus(UUID userId, UserStatus status) {
        MasterelloUser masterelloUser = retrieveUserByUuid(userId);
        if (masterelloUser.getStatus() == status) {
            throw new UserStatusCannotBeUpdatedException(status);
        }
        ((MasterelloUserEntity) masterelloUser).setStatus(status);
        MasterelloUser updatedUser = userRepository.save((MasterelloUserEntity) masterelloUser);
        publisher.publishEvent(new UserStatusChangedEvent(updatedUser));
    }
}
