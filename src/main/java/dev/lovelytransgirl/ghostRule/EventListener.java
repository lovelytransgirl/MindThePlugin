package dev.lovelytransgirl.ghostRule;

import dev.lovelytransgirl.ghostRule.ChatFilter.ChatFilterManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;

public class EventListener implements Listener {
    private final ChatFilterManager chatFilter;
    private final JavaPlugin plugin;

    public EventListener(JavaPlugin plugin, ChatFilterManager chatFilter) {
        this.plugin = plugin;
        this.chatFilter = chatFilter;
    }

    @EventHandler
    public void onPlayerChatEvent(AsyncChatEvent event) {
        if (event.getPlayer().hasPermission(chatFilter.getBypassPermission())) {
            return;
        }
        if (chatFilter.isFiltered(event.originalMessage())) {
            var mm = MiniMessage.miniMessage();
            Component parsed = mm.deserialize(GhostRule.getInstance().prefix + "<red>This content has been permitted from sending.");
            event.getPlayer().sendMessage(parsed);
            event.setCancelled(true);
        }
    }
}
