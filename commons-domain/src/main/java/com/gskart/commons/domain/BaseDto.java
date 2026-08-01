package com.gskart.commons.domain;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * The fields most response DTOs carry: the identifier plus the audit trail.
 *
 * <p>Unlike {@link BaseAuditEntity} this one keeps the id, because a DTO is a wire shape rather than
 * a mapped table and every service exposing a numeric id repeats the same four audit fields
 * alongside it. A service whose identifiers are not numeric simply doesn't extend this.
 */
@Data
public class BaseDto {
    private Long id;
    private String createdBy;
    private OffsetDateTime createdOn;
    private String modifiedBy;
    private OffsetDateTime modifiedOn;
}
