package com.ellanstudio.deathmessages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

record DeathContext(
        Player victim,
        Entity killer,
        LivingEntity livingKiller,
        Entity direct,
        ItemStack weapon,
        String damageType,
        String category,
        Component playerComponent,
        Component killerComponent,
        Component mobComponent,
        Component itemComponent,
        Component directComponent,
        Component causeComponent
) {
    private static final TextColor PLAYER_COLOR = TextColor.fromHexString("#8FD3C7");
    private static final TextColor KILLER_COLOR = TextColor.fromHexString("#E49B9B");
    private static final TextColor ITEM_COLOR = TextColor.fromHexString("#D8C08A");

    static DeathContext create(PlayerDeathEvent event) {
        DamageSource source = event.getDamageSource();
        Entity causing = source.getCausingEntity();
        Entity direct = source.getDirectEntity();
        Entity killer = resolveKiller(causing, direct);
        LivingEntity livingKiller = killer instanceof LivingEntity living ? living : null;
        ItemStack weapon = resolveWeapon(direct, livingKiller);
        boolean projectile = direct instanceof Projectile && direct != killer;
        boolean hasWeapon = weapon.getType() != Material.AIR;
        String damageType = source.getDamageType().getKey().getKey();
        String category = DeathCategoryResolver.resolve(
                damageType,
                killer instanceof Player,
                livingKiller != null,
                projectile,
                hasWeapon
        );

        return new DeathContext(
                event.getEntity(),
                killer,
                livingKiller,
                direct,
                weapon,
                damageType,
                category,
                styled(event.getEntity().displayName(), PLAYER_COLOR),
                styled(display(killer, "未知力量"), KILLER_COLOR),
                styled(display(livingKiller, "未知生物"), KILLER_COLOR),
                styled(weapon.isEmpty() ? null : weapon.effectiveName(), ITEM_COLOR),
                styled(display(direct, "未知伤害"), ITEM_COLOR),
                Component.translatable(source.getDamageType().getTranslationKey())
        );
    }

    Map<String, Component> placeholders() {
        Map<String, Component> placeholders = new LinkedHashMap<>();
        placeholders.put("player", playerComponent);
        placeholders.put("killer", killerComponent);
        placeholders.put("mob", mobComponent);
        placeholders.put("item", itemComponent);
        placeholders.put("direct", directComponent);
        placeholders.put("world", Component.text(victim.getWorld().getName()));
        placeholders.put("cause", causeComponent);
        return placeholders;
    }

    private static Entity resolveKiller(Entity causing, Entity direct) {
        Entity killer = causing;
        if (killer == null && direct instanceof LivingEntity) {
            killer = direct;
        }
        if (killer instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
            killer = shooter;
        }
        return killer;
    }

    private static ItemStack resolveWeapon(Entity direct, LivingEntity killer) {
        if (direct instanceof Trident trident) {
            return trident.getItem();
        }
        if (killer == null || killer.getEquipment() == null) {
            return new ItemStack(Material.AIR);
        }
        ItemStack mainHand = killer.getEquipment().getItemInMainHand();
        if (!mainHand.isEmpty()) {
            return mainHand;
        }
        return killer.getEquipment().getItemInOffHand();
    }

    private static Component display(Entity entity, String fallback) {
        if (entity == null) {
            return Component.text(fallback);
        }
        if (entity instanceof Player player) {
            return player.displayName();
        }
        if (entity instanceof Nameable nameable && nameable.customName() != null) {
            return nameable.customName();
        }
        return Component.translatable(entity.getType().translationKey());
    }

    private static Component styled(Component component, TextColor color) {
        if (component == null) {
            return Component.text("").color(color);
        }
        return component.applyFallbackStyle(Style.style(color));
    }
}
