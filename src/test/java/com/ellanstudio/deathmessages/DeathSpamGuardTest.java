package com.ellanstudio.deathmessages;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeathSpamGuardTest {
    @Test
    void suppressesMessagesAfterConfiguredLimit() {
        DeathSpamGuard guard = new DeathSpamGuard();
        UUID playerId = UUID.randomUUID();

        assertTrue(guard.allow(playerId, true, 30, 3));
        assertTrue(guard.allow(playerId, true, 30, 3));
        assertTrue(guard.allow(playerId, true, 30, 3));
        assertFalse(guard.allow(playerId, true, 30, 3));
    }

    @Test
    void canDisableSpamProtection() {
        DeathSpamGuard guard = new DeathSpamGuard();
        UUID playerId = UUID.randomUUID();

        for (int index = 0; index < 10; index++) {
            assertTrue(guard.allow(playerId, false, 30, 1));
        }
    }
}
