package com.masterello.user.repository;

import com.masterello.user.domain.SupportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupportRequestRepository extends JpaRepository<SupportRequest, UUID> {

    List<SupportRequest> findByProcessedFalse();
}
