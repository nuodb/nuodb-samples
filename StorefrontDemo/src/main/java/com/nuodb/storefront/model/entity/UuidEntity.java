package com.nuodb.storefront.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class UuidEntity implements IEntity {
    @Id
    @Column(length = 36)
    private String uuid = UUID.randomUUID().toString();

    public UuidEntity() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
