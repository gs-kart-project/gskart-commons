package com.gskart.commons.domain;

import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Audit columns shared by the persisted entities.
 *
 * <p>There is no id here on purpose: each entity declares its own, so it keeps control of the type
 * and the generation strategy. Services that store their data somewhere other than a relational
 * database can ignore this class entirely - nothing forces its use.
 */
@Data
@NoArgsConstructor
@MappedSuperclass
public class BaseAuditEntity {
    private String createdBy;
    private OffsetDateTime createdOn;
    private String modifiedBy;
    private OffsetDateTime modifiedOn;
}
