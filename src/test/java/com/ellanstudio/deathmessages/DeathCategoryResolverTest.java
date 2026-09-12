package com.ellanstudio.deathmessages;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeathCategoryResolverTest {
    @Test
    void resolvesPlayerWeaponAttack() {
        assertEquals("player_item",
                DeathCategoryResolver.resolve("player_attack", true, true, false, true));
    }

    @Test
    void resolvesPlayerProjectile() {
        assertEquals("player_projectile",
                DeathCategoryResolver.resolve("arrow", true, true, true, true));
    }

    @Test
    void resolvesMobWeaponAttack() {
        assertEquals("mob_item",
                DeathCategoryResolver.resolve("mob_attack", false, true, false, true));
    }

    @Test
    void resolvesEnvironmentBeforeKillerState() {
        assertEquals("explosion",
                DeathCategoryResolver.resolve("player_explosion", true, true, false, true));
        assertEquals("void",
                DeathCategoryResolver.resolve("out_of_world", false, false, false, false));
        assertEquals("sonic_boom",
                DeathCategoryResolver.resolve("sonic_boom", false, true, false, false));
    }
}
