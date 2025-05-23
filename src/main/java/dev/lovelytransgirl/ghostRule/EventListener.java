package dev.lovelytransgirl.ghostRule;

import dev.lovelytransgirl.ghostRule.ChatFilter.ChatFilterManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

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
        player.sendMessage(colorize("Enjoy playing! &b&n:BlobhajHeart150:"));
        ;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChatEvent(AsyncChatEvent event) {
        if (event.getPlayer().hasPermission(chatFilter.getBypassPermission())) {
            return;
        }
        if (chatFilter.isFiltered(event.originalMessage())) {
            var mm = MiniMessage.miniMessage();
            Component parsed = mm.deserialize(GhostRule.getInstance().prefix + "<red>This content has been permitted from sending.");
            event.getPlayer().sendMessage(parsed);
            event.setCancelled(true);
            String said = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(final AsyncPlayerChatEvent event) {
        final String message = event.getMessage();
        final Player player = event.getPlayer();
        if (chatFilter.isFilteredButOld(event.getMessage())) {
            event.setCancelled(true);
            return;
        }

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

//    @EventHandler
//    public void onBlockBreak(BlockBreakEvent event) {
//        List<String> playerList = new ArrayList<>();
//        playerList.add(event.getPlayer().getName());
//
//        CoreProtectAPI api = GhostRule.getInstance().getCoreProtect();
//        api.testAPI();
//        if (api != null) {
//            List<String[]> lookup = api.performLookup(10, playerList, null, null, null, null, 0, null);
//            if (lookup != null) {
//                for (String[] result : lookup){
//                    CoreProtectAPI.ParseResult parseResult = api.parseResult(result);
//                    event.getPlayer().sendMessage("" + parseResult.getX());
//                    event.getPlayer().sendMessage("" + parseResult.getY());
//                    event.getPlayer().sendMessage("" + parseResult.getZ());
//                    event.getPlayer().sendMessage("" + parseResult.getActionString());
//                    event.getPlayer().sendMessage("" + parseResult.getPlayer());
//                    event.getPlayer().sendMessage("" + parseResult.getTimestamp());
//                    event.getPlayer().sendMessage("" + parseResult.getType());
//                }
//            }
//        }
//    }
}