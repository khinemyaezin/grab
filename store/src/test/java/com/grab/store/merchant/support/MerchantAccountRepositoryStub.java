package com.grab.store.merchant.support;

import com.grab.framework.id.Id;
import com.merchant.domain.aggregate.MerchantAccount;
import com.merchant.domain.enums.MerchantStatus;
import com.merchant.domain.enums.MerchantType;
import com.merchant.domain.repository.MerchantAccountRepository;
import com.merchant.domain.valueobject.BusinessRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MerchantAccountRepositoryStub implements MerchantAccountRepository {
    private final List<MerchantAccount> accounts = new ArrayList<>();

    @Override
    public Optional<MerchantAccount> findById(Id id) {
        return accounts.stream().filter(account -> account.getId().equals(id)).findFirst();
    }

    @Override
    public List<MerchantAccount> findByApplicantUserId(Id applicantUserId) {
        return accounts.stream().filter(account -> account.isApplicant(applicantUserId)).toList();
    }

    @Override
    public List<MerchantAccount> findByStatus(MerchantStatus status) {
        return accounts.stream().filter(account -> account.getStatus() == status).toList();
    }

    @Override
    public boolean existsOpenApplication(Id applicantUserId, MerchantType type) {
        return accounts.stream().anyMatch(account -> account.isApplicant(applicantUserId)
                && account.getType() == type
                && !account.getStatus().isTerminal());
    }

    @Override
    public boolean existsRegistration(BusinessRegistration registration, Id excludingMerchantId) {
        return accounts.stream().anyMatch(account -> !account.getId().equals(excludingMerchantId)
                && registration.equals(account.getRegistration()));
    }

    @Override
    public MerchantAccount save(MerchantAccount merchant) {
        accounts.removeIf(account -> account.getId().equals(merchant.getId()));
        accounts.add(merchant);
        return merchant;
    }
}
