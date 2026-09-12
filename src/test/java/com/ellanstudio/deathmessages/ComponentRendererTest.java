package com.ellanstudio.deathmessages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentRendererTest {
    @Test
    void preservesTranslatableCraftEngineItemNames() {
        Component item = Component.translatable("block.kaleidoscope_tavern.sculk_special");
        Component rendered = ComponentRenderer.render(
                "",
                "<player> 被 <killer> 用 <item> 送走了",
                Map.of(
                        "player", Component.text("ItzHuaJi"),
                        "killer", Component.text("Estwind"),
                        "item", item
                )
        );

        assertTrue(containsTranslation(rendered, "block.kaleidoscope_tavern.sculk_special"));
    }

    private static boolean containsTranslation(Component component, String key) {
        if (component instanceof TranslatableComponent translatable && key.equals(translatable.key())) {
            return true;
        }
        for (Component child : component.children()) {
            if (containsTranslation(child, key)) {
                return true;
            }
        }
        return false;
    }
}
