package dev.lovelytransgirl.ghostRule;

import dev.lovelytransgirl.ghostRule.ChatFilter.ChatFilterManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Pattern;

public class EventListener implements Listener {
    private final ChatFilterManager chatFilter;
    private final JavaPlugin plugin;

    public EventListener(JavaPlugin plugin, ChatFilterManager chatFilter) {
        this.plugin = plugin;
        this.chatFilter = chatFilter;
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        final CachedMetaData metaData = GhostRule.getInstance().luckPerms.getPlayerAdapter(Player.class).getMetaData(player);

        if (player.hasPlayedBefore() != true) {
            player.sendMessage("Welcome, Looks like this is your first time playing!");
            player.sendMessage(colorize("Make sure to read the rules with &e&n/rules&r!"));
            player.sendMessage("");
        }
        if (player.hasPlayedBefore() == true) {
            player.sendMessage(colorize("Welcome back &e&n" + player.getName()));
        }
        player.sendMessage("");
        player.sendMessage(colorize("Your rank is &b&n" + metaData.getPrimaryGroup().toUpperCase()));
        player.sendMessage(colorize("You can view all the available command with &e&n/help&f!"));
        player.sendMessage("");
        player.sendMessage(colorize("If you have any question, Please either ask &aonline moderators &for ping us in the &bdiscord&f!"));
        player.sendMessage("");
        player.sendMessage(colorize("Enjoy playing! &b&n:BlobhajHeart150:"));;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(final AsyncPlayerChatEvent event) {
        final String message = event.getMessage();
        final Player player = event.getPlayer();

        // Get a LuckPerms cached metadata for the player.
        final CachedMetaData metaData = GhostRule.getInstance().luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
        if (metaData.getPrefix() == null) {
            event.setFormat(colorize(event.getPlayer().getName() + " &8» &r") + event.getMessage());
        } else {
            event.setFormat(colorize(metaData.getPrefix() + event.getPlayer().getName() + " &8» &r") + event.getMessage());
        }
    }

    private String colorize(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}