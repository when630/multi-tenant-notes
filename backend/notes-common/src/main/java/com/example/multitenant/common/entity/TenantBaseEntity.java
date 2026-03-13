package com.example.multitenant.common.entity;

import com.example.multitenant.common.context.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class TenantBaseEntity extends BaseEntity {
  
  @Column(name = "tenant_id", nullable = false, updatable = false)
  private Long tenantId;
  
  @PrePersist
  public void prePersistTenant() {
    if (this.tenantId == null) {
      this.tenantId = TenantContext.getTenantId();
    }
  }
}
