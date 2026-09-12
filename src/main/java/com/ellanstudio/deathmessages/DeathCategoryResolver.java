package com.ellanstudio.deathmessages;

final class DeathCategoryResolver {
    private DeathCategoryResolver() {
    }

    static String resolve(String damageType, boolean playerKiller, boolean livingKiller,
                          boolean projectile, boolean hasWeapon) {
        String direct = switch (damageType) {
            case "player_explosion", "explosion", "bad_respawn_point", "fireworks" -> "explosion";
            case "fall" -> "fall";
            case "falling_anvil" -> "anvil";
            case "falling_stalactite" -> "stalactite";
            case "stalagmite" -> "stalagmite";
            case "in_fire", "on_fire", "hot_floor" -> "fire";
            case "lava" -> "lava";
            case "drown", "dry_out" -> "drowning";
            case "freeze" -> "freeze";
            case "out_of_world" -> "void";
            case "lightning_bolt" -> "lightning";
            case "cactus" -> "cactus";
            case "sweet_berry_bush" -> "berry";
            case "campfire" -> "campfire";
            case "starve" -> "starvation";
            case "in_wall", "cramming" -> "suffocation";
            case "magic", "indirect_magic", "dragon_breath" -> "magic";
            case "wither" -> "wither";
            case "sonic_boom" -> "sonic_boom";
            default -> null;
        };
        if (direct != null) {
            return direct;
        }
        if (playerKiller) {
            if (projectile) {
                return "player_projectile";
            }
            return hasWeapon ? "player_item" : "player_general";
        }
        if (livingKiller) {
            if (projectile) {
                return "mob_projectile";
            }
            return hasWeapon ? "mob_item" : "mob_general";
        }
        if (projectile) {
            return "projectile";
        }
        return "generic";
    }
}
