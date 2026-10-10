package com.merchant.adapter.persistence.entity;

import com.manifest.adapter.persistence.entity.RoleDeclarationPublicationState;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "role_declaration_publication")
public class MerchantRoleDeclarationPublicationEntity extends RoleDeclarationPublicationState {
    public MerchantRoleDeclarationPublicationEntity() {
    }

    public MerchantRoleDeclarationPublicationEntity(String publicationKey) {
        super(publicationKey);
    }
}
