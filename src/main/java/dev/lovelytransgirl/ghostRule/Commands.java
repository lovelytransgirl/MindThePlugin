package dev.lovelytransgirl.ghostRule;

import dev.lovelytransgirl.ghostRule.ChatFilter.ChatFilterManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Commands implements CommandExecutor, TabCompleter {
    private final ChatFilterManager chatFilter;

    public Commands(ChatFilterManager chatFilter) {
        this.chatFilter = chatFilter;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("filter")) {
            if (sender.isOp()) {
                return handleFilterCMD(sender, args);
            } else {
                var mm = MiniMessage.miniMessage();
                Component parsed = mm.deserialize(GhostRule.getInstance().prefix + "<red>You are not an operator.");
                sender.sendMessage(parsed);
            }
        }
        if (command.getName().equalsIgnoreCase("hug")) {
            if (args.length == 0) {
                var mm = MiniMessage.miniMessage();
                sender.sendMessage(mm.deserialize(GhostRule.getInstance().prefix + "<yellow>You need to provide player you wanted to hug :3"));
            } else {
                var mm = MiniMessage.miniMessage();
                Player target = GhostRule.getInstance().getServer().getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(mm.deserialize(GhostRule.getInstance().prefix + "<red>Player " + args[0] + " not found or offline!"));
                    return true;
                }

                sender.sendMessage(mm.deserialize(GhostRule.getInstance().prefix + "<white>You hugged <light_purple>" + target.getName() + "!"));
                target.sendMessage(mm.deserialize(GhostRule.getInstance().prefix + "<light_purple>" + sender.getName() + " <white>gave you a hug :3"));
                return true;
            }
        }
        if (command.getName().equalsIgnoreCase("kiss")) {
            if (args.length == 0) {
                var mm = MiniMessage.miniMessage();
                sender.sendMessage(mm.deserialize(GhostRule.getInstance().prefix + "<yellow>You need to provide player you wanted to kiss :3"));
            } else {
                var mm = MiniMessage.miniMessage();
                Player target = GhostRule.getInstance().getServer().getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(mm.deserialize(GhostRule.getInstance().prefix + "<red>Player " + args[0] + " not found or offline!"));
                    return true;
                }

                sender.sendMessage(mm.deserialize(GhostRule.getInstance().prefix + "<white>You kissed <light_purple>" + target.getName() + "!"));
                target.sendMessage(mm.deserialize(GhostRule.getInstance().prefix + "<light_purple>" + sender.getName() + " <white>gave you a kiss :3"));
                return true;
            }
        }
        if (command.getName().equalsIgnoreCase("help")) {
            sender.sendMessage("Available Commands:");
            sender.sendMessage("");
            var mm = MiniMessage.miniMessage();
            sender.sendMessage(mm.deserialize("<rainbow>/help <gray>- <white>Show you this message."));
            sender.sendMessage(mm.deserialize("<rainbow>/tpa [<player>] <gray>- <white>Send teleport request to player."));
            sender.sendMessage(mm.deserialize("<rainbow>/tpaccept <gray>- <white>Accept pending teleport request."));
            sender.sendMessage(mm.deserialize("<rainbow>/tpadeny <gray>- <white>Deny pending teleport request."));
            sender.sendMessage(mm.deserialize("<rainbow>/warp [<warp>] <gray>- <white>Teleport to specific warps."));
            sender.sendMessage(mm.deserialize("<rainbow>/spawn <gray>- <white>Teleport you to spawn."));
            sender.sendMessage(mm.deserialize("<rainbow>/home <gray>- <white>Teleport to your home."));
            sender.sendMessage(mm.deserialize("<rainbow>/delhome <gray>- <white>Delete your home."));
            sender.sendMessage(mm.deserialize("<rainbow>/phome <gray>- <white>Teleport to public homes"));
            sender.sendMessage(mm.deserialize("<rainbow>/phomelist <gray>- <white>See all available public homes."));
            sender.sendMessage("");
            sender.sendMessage("You can find more command in our discord server!");
        }
        return false;
    }

    private boolean handleFilterCMD(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§6Filter Commands:");
            sender.sendMessage("§e/filter reload §7- Reload patterns from config");
            sender.sendMessage("§e/filter count §7- Show loaded pattern count");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                chatFilter.reloadPatterns();
                sender.sendMessage("§aReloaded " + chatFilter.getPatternCount() + " patterns");
                return true;

            case "count":
                sender.sendMessage("§eLoaded patterns: §b" + chatFilter.getPatternCount());
                return true;

            default:
                sender.sendMessage("§cUnknown subcommand");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("filter")) {
            if (args.length == 1) {
                return Arrays.asList("reload", "count");
            }
        }
        if (command.getName().equalsIgnoreCase("hug")) {
            return args.length == 1 ?
                    GhostRule.getInstance().getServer().getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList()) :
                    Collections.emptyList();
        }
        if (command.getName().equalsIgnoreCase("kiss")) {
            return args.length == 1 ?
                    GhostRule.getInstance().getServer().getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList()) :
                    Collections.emptyList();
        }
        // Add tab completions for other commands here
        return new ArrayList<>();
    }
}
