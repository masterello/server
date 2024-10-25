package com.masterello.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "support", schema = "public")
public class SupportRequest implements Serializable {
    @Id
    @Column(name = "uuid")
    @GeneratedValue
    private UUID uuid;

    @Column(name = "title")
    private String title;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "message")
    private String message;

    @Column(name = "processed")
    private Boolean processed;

    @Column(name = "creation_date")
    @CreationTimestamp
    private OffsetDateTime creationDate;
}
