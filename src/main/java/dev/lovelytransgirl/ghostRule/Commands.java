package dev.lovelytransgirl.ghostRule;

import dev.lovelytransgirl.ghostRule.ChatFilter.ChatFilterManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        // Add tab completions for other commands here
        return new ArrayList<>();
    }
}
