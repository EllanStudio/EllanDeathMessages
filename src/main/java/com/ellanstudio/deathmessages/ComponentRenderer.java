package com.ellanstudio.deathmessages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Map;

final class ComponentRenderer {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private ComponentRenderer() {
    }

    static Component render(String prefix, String template, Map<String, Component> placeholders) {
        TagResolver.Builder resolver = TagResolver.builder();
        placeholders.forEach((key, component) -> {
            if (component != null) {
                resolver.resolver(Placeholder.component(key, component));
            }
        });
        return MINI_MESSAGE.deserialize(
                (prefix == null ? "" : prefix) + (template == null ? "" : template),
                resolver.build()
        );
    }
}
