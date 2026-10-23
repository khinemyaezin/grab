package com.saleschannel.application.port.outbound;

public interface PublishableTokenGeneratorPort {
    GeneratedToken generate();

    String digest(String rawToken);

    record GeneratedToken(String rawToken, String digest, String displayPrefix) {
        @Override
        public String toString() {
            return "GeneratedToken[rawToken=[REDACTED], digest=[REDACTED]]";
        }
    }
}
