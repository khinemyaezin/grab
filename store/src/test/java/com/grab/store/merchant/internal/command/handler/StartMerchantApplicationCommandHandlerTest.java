package com.grab.store.merchant.internal.command.handler;

import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.merchant.application.model.write.StartMerchantApplicationCommand;
import com.merchant.application.service.StartMerchantApplicationService;
import com.grab.store.merchant.support.MerchantAccountRepositoryStub;
import com.merchant.domain.enums.MerchantStatus;
import com.merchant.domain.enums.MerchantType;
import com.merchant.domain.service.MerchantRegistrationPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StartMerchantApplicationServiceTest {
    @Test
    void execute_withAuthenticatedApplicant_shouldCreateAndSaveDraft() {
        CommonId applicantId = new CommonId("applicant-1");
        CommonId merchantId = new CommonId("merchant-1");
        MerchantAccountRepositoryStub merchants = new MerchantAccountRepositoryStub();
        IdGenerator ids = new IdGenerator() {
            public CommonId generateId() { return merchantId; }
            public CommonId convertIdFrom(String id) { return new CommonId(id); }
        };
        StartMerchantApplicationService service = new StartMerchantApplicationService(
                merchants, new MerchantRegistrationPolicy(merchants), ids);
        StartMerchantApplicationCommand command = new StartMerchantApplicationCommand(
                applicantId, MerchantType.FIRST_PARTY_RETAILER, "Acme Store");

        var result = service.execute(command);

        assertThat(result.merchantId()).isEqualTo("merchant-1");
        assertThat(result.status()).isEqualTo(MerchantStatus.DRAFT.name());
        assertThat(merchants.findById(merchantId)).isPresent();
    }
}
