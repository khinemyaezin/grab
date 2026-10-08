package com.merchant.adapter.persistence.entity;

import com.grab.outbox.infrastructure.security.SecurityManifestPublicationState;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "security_manifest_publication")
public class MerchantSecurityManifestPublicationEntity extends SecurityManifestPublicationState {
}
