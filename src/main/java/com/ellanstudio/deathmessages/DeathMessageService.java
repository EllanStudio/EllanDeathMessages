package com.ellanstudio.deathmessages;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

final class DeathMessageService {
    private final Plugin plugin;
    private final DeathSpamGuard spamGuard = new DeathSpamGuard();
    private final Map<String, List<String>> messages = new HashMap<>();
    private volatile Settings settings = Settings.disabled();
    private String prefix = "";

    DeathMessageService(Plugin plugin) {
        this.plugin = plugin;
    }

    void reload() {
        messages.clear();
        prefix = plugin.getConfig().getString("prefix", "");

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("messages");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                List<String> values = section.getStringList(key);
                if (!values.isEmpty()) {
                    messages.put(key.toLowerCase(Locale.ROOT), List.copyOf(values));
                }
            }
        }

        settings = new Settings(
                plugin.getConfig().getBoolean("enabled", true),
                plugin.getConfig().getBoolean("settings.cross-server", true),
                plugin.getConfig().getInt("settings.broadcast-range", -1),
                plugin.getConfig().getBoolean("settings.send-victim-own-message", true),
                plugin.getConfig().getBoolean("settings.send-to-console", true),
                plugin.getConfig().getBoolean("settings.fallback-to-vanilla", true),
                plugin.getConfig().getBoolean("settings.anti-spam.enabled", true),
                plugin.getConfig().getInt("settings.anti-spam.seconds", 30),
                plugin.getConfig().getInt("settings.anti-spam.count", 3),
                lowerSet(plugin.getConfig().getStringList("settings.disabled-worlds")),
                lowerSet(plugin.getConfig().getStringList("settings.muted-worlds")),
                lowerSet(plugin.getConfig().getStringList("settings.ignored-players"))
        );
        spamGuard.clear();
    }

    void handle(PlayerDeathEvent event) {
        Settings current = settings;
        if (!current.enabled()) {
            return;
        }

        DeathContext context = DeathContext.create(event);
        Player victim = context.victim();
        if (current.disabledWorlds().contains(victim.getWorld().getName().toLowerCase(Locale.ROOT))
                || current.ignoredPlayers().contains(victim.getName().toLowerCase(Locale.ROOT))) {
            event.deathMessage(null);
            return;
        }

        List<String> candidates = messages.get(context.category());
        if (candidates == null || candidates.isEmpty()) {
            candidates = messages.get("generic");
        }
        if (candidates == null || candidates.isEmpty()) {
            if (!current.fallbackToVanilla()) {
                event.deathMessage(null);
            }
            return;
        }

        String template = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        Component component = ComponentRenderer.render(prefix, template, context.placeholders());
        event.deathMessage(null);

        boolean allowed = spamGuard.allow(
                victim.getUniqueId(),
                current.antiSpamEnabled(),
                current.antiSpamSeconds(),
                current.antiSpamCount()
        );
        if (!allowed) {
            if (current.sendVictimOwnMessage()) {
                victim.sendMessage(component);
            }
            return;
        }

        pluginAsEllan().broadcastDeath(component, local -> broadcastLocal(local, context.victim().getLocation(), true));
    }

    void broadcastRemote(Component component) {
        broadcastLocal(component, null, false);
    }

    boolean crossServerEnabled() {
        return settings.crossServer();
    }

    int broadcastRange() {
        return settings.broadcastRange();
    }

    String statusLine() {
        return "enabled=" + settings.enabled()
                + ", crossServer=" + settings.crossServer()
                + ", range=" + settings.broadcastRange()
                + ", categories=" + messages.size();
    }

    private void broadcastLocal(Component component, Location source, boolean applyRange) {
        Settings current = settings;
        double rangeSquared = current.broadcastRange() < 0 ? -1 : current.broadcastRange() * (double) current.broadcastRange();
        for (Player recipient : Bukkit.getOnlinePlayers()) {
            if (current.mutedWorlds().contains(recipient.getWorld().getName().toLowerCase(Locale.ROOT))) {
                continue;
            }
            if (applyRange && rangeSquared >= 0 && source != null && !withinRange(recipient, source, rangeSquared)) {
                continue;
            }
            recipient.sendMessage(component);
        }
        if (current.sendToConsole()) {
            Bukkit.getConsoleSender().sendMessage(component);
        }
    }

    private static boolean withinRange(Player recipient, Location source, double rangeSquared) {
        World sourceWorld = source.getWorld();
        return sourceWorld != null
                && recipient.getWorld().equals(sourceWorld)
                && recipient.getLocation().distanceSquared(source) <= rangeSquared;
    }

    private EllanDeathMessagesPlugin pluginAsEllan() {
        return (EllanDeathMessagesPlugin) plugin;
    }

    private static Set<String> lowerSet(List<String> input) {
        Set<String> output = new HashSet<>();
        for (String value : input) {
            if (value != null && !value.isBlank()) {
                output.add(value.toLowerCase(Locale.ROOT));
            }
        }
        return output;
    }

    private record Settings(
            boolean enabled,
            boolean crossServer,
            int broadcastRange,
            boolean sendVictimOwnMessage,
            boolean sendToConsole,
            boolean fallbackToVanilla,
            boolean antiSpamEnabled,
            int antiSpamSeconds,
            int antiSpamCount,
            Set<String> disabledWorlds,
            Set<String> mutedWorlds,
            Set<String> ignoredPlayers
    ) {
        static Settings disabled() {
            return new Settings(false, false, -1, true, true, true, false,
                    30, 3, Set.of(), Set.of(), Set.of());
        }
    }
}
