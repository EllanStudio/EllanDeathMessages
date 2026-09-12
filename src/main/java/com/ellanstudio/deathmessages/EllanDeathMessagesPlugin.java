package com.ellanstudio.deathmessages;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.function.Consumer;

public final class EllanDeathMessagesPlugin extends JavaPlugin {
    private DeathMessageService service;
    private RedisComponentBus redis;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        service = new DeathMessageService(this);
        service.reload();

        if (getConfig().getBoolean("redis.enabled", true)) {
            redis = new RedisComponentBus(
                    this,
                    getConfig().getString("redis.host", "127.0.0.1"),
                    getConfig().getInt("redis.port", 6379),
                    getConfig().getString("redis.password", ""),
                    getConfig().getInt("redis.database", 0),
                    getConfig().getString("redis.channel", "ellan:death-messages"),
                    this::handleRemoteMessage
            );
            redis.start();
        }

        Bukkit.getPluginManager().registerEvents(new DeathMessageListener(service), this);
        EllanDeathCommand command = new EllanDeathCommand(this);
        if (getCommand("ellandeath") != null) {
            getCommand("ellandeath").setExecutor(command);
            getCommand("ellandeath").setTabCompleter(command);
        }

        warnIfCmiCustomMessagesEnabled();
        getLogger().info("EllanDeathMessages enabled.");
    }

    @Override
    public void onDisable() {
        if (redis != null) {
            redis.close();
        }
    }

    public void reloadPlugin() {
        reloadConfig();
        service.reload();
        if (redis != null) {
            redis.close();
            redis = null;
        }
        if (getConfig().getBoolean("redis.enabled", true)) {
            redis = new RedisComponentBus(
                    this,
                    getConfig().getString("redis.host", "127.0.0.1"),
                    getConfig().getInt("redis.port", 6379),
                    getConfig().getString("redis.password", ""),
                    getConfig().getInt("redis.database", 0),
                    getConfig().getString("redis.channel", "ellan:death-messages"),
                    this::handleRemoteMessage
            );
            redis.start();
        }
        warnIfCmiCustomMessagesEnabled();
    }

    public void broadcastDeath(Component component, Consumer<Component> localDelivery) {
        localDelivery.accept(component);
        if (service.crossServerEnabled() && service.broadcastRange() < 0 && redis != null) {
            redis.publishAsync(component);
        }
    }

    public void broadcastRemote(Component component) {
        service.broadcastRemote(component);
    }

    public DeathMessageService service() {
        return service;
    }

    private void handleRemoteMessage(RedisComponentBus.Envelope envelope) {
        if (redis != null && redis.isLocalSource(envelope.source())) {
            return;
        }
        Component component = envelope.component();
        if (component == null) {
            return;
        }
        Bukkit.getScheduler().runTask(this, () -> broadcastRemote(component));
    }

    private void warnIfCmiCustomMessagesEnabled() {
        File file = new File(getDataFolder().getParentFile(), "CMI/Settings/DeathMessages.yml");
        if (!file.isFile()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.getBoolean("EnableCustom", false)) {
            getLogger().warning("CMI custom death messages are still enabled. Set EnableCustom: false to avoid duplicate broadcasts.");
        }
    }
}
