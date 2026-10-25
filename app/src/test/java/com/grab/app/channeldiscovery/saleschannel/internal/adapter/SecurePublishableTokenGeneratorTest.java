package com.grab.app.channeldiscovery.saleschannel.internal.adapter;

import com.saleschannel.application.port.outbound.PublishableTokenGeneratorPort.GeneratedToken;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class SecurePublishableTokenGeneratorTest {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("pk_[A-Za-z0-9_-]{43}");
    private final SecurePublishableTokenGenerator generator = new SecurePublishableTokenGenerator();

    @Test
    void generatesExpectedTokenMaterial() {
        GeneratedToken generated = generator.generate();

        assertThat(generated.rawToken()).matches(TOKEN_PATTERN);
        assertThat(generated.displayPrefix()).isEqualTo(generated.rawToken().substring(0, 11));
        assertThat(generated.digest()).hasSize(64).matches("[0-9a-f]{64}");
        assertThat(generator.digest(generated.rawToken())).isEqualTo(generated.digest());
    }

    @Test
    void generatesDifferentTokens() {
        assertThat(generator.generate().rawToken()).isNotEqualTo(generator.generate().rawToken());
    }
}
