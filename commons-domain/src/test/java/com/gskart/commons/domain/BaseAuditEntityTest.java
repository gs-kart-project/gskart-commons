package com.gskart.commons.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the two things a service relies on: the audit fields are inherited, and the id stays with
 * the concrete entity so each one picks its own generation strategy.
 */
class BaseAuditEntityTest {

    @Entity
    static class SampleEntity extends BaseAuditEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
    }

    @Test
    void subclassInheritsTheAuditFields() {
        OffsetDateTime createdOn = OffsetDateTime.of(2026, 8, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        SampleEntity entity = new SampleEntity();

        entity.setCreatedBy("gautham");
        entity.setCreatedOn(createdOn);
        entity.setModifiedBy("gautham");
        entity.setModifiedOn(createdOn.plusHours(2));

        assertThat(entity.getCreatedBy()).isEqualTo("gautham");
        assertThat(entity.getCreatedOn()).isEqualTo(createdOn);
        assertThat(entity.getModifiedBy()).isEqualTo("gautham");
        assertThat(entity.getModifiedOn()).isEqualTo(createdOn.plusHours(2));
    }

    @Test
    void baseDeclaresNoIdentifierOfItsOwn() {
        assertThat(BaseAuditEntity.class.getDeclaredFields())
                .extracting("name")
                .containsExactlyInAnyOrder("createdBy", "createdOn", "modifiedBy", "modifiedOn");
    }
}
