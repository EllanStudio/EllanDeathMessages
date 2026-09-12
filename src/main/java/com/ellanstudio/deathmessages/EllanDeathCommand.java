package com.ellanstudio.deathmessages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Map;

public final class EllanDeathCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUBCOMMANDS = List.of("reload", "status", "test");

    private final EllanDeathMessagesPlugin plugin;

    public EllanDeathCommand(EllanDeathMessagesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ellandeath.admin")) {
            sender.sendMessage(Component.text("你没有权限执行该命令。", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Component.text("/" + label + " <reload|status>", NamedTextColor.YELLOW));
            return true;
        }
        return switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadPlugin();
                sender.sendMessage(Component.text("EllanDeathMessages 配置已重新加载。", NamedTextColor.GREEN));
                yield true;
            }
            case "status" -> {
                sender.sendMessage(Component.text("EllanDeathMessages: " + plugin.service().statusLine(), NamedTextColor.GRAY));
                yield true;
            }
            case "test" -> {
                Component message = ComponentRenderer.render(
                        plugin.getConfig().getString("prefix", ""),
                        "<player> 被 <killer> 用 <item> 送走了",
                        Map.of(
                                "player", Component.text("测试玩家"),
                                "killer", Component.text("测试击杀者"),
                                "item", Component.translatable("block.kaleidoscope_tavern.sculk_special")
                        )
                );
                plugin.broadcastDeath(message, plugin.service()::broadcastRemote);
                sender.sendMessage(Component.text("已发送跨服测试死亡消息。", NamedTextColor.GREEN));
                yield true;
            }
            default -> {
                sender.sendMessage(Component.text("/" + label + " <reload|status>", NamedTextColor.YELLOW));
                yield true;
            }
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return SUBCOMMANDS.stream().filter(value -> value.startsWith(prefix)).toList();
        }
        return List.of();
    }
}
