package org.example.polify.attempt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "polify.attempts")
public class AttemptProperties {
    private long ttlSeconds;

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }
}

