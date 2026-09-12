package com.ellanstudio.deathmessages;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class DeathSpamGuard {
    private final Map<UUID, Deque<Long>> history = new HashMap<>();

    boolean allow(UUID playerId, boolean enabled, int seconds, int count) {
        if (!enabled || seconds < 0 || count <= 0) {
            return true;
        }
        long now = System.currentTimeMillis();
        long cutoff = now - seconds * 1000L;
        Deque<Long> deaths = history.computeIfAbsent(playerId, ignored -> new ArrayDeque<>());
        while (!deaths.isEmpty() && deaths.peekFirst() < cutoff) {
            deaths.removeFirst();
        }
        if (deaths.size() >= count) {
            return false;
        }
        deaths.addLast(now);
        return true;
    }

    void clear() {
        history.clear();
    }
}
