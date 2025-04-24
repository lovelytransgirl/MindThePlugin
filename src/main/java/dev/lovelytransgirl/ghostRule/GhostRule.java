package dev.lovelytransgirl.ghostRule;

import dev.lovelytransgirl.ghostRule.ChatFilter.ChatFilterManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class GhostRule extends JavaPlugin {
    private ChatFilterManager chatFilter;
    public final String prefix = "<gradient:#a77df0:#602fb5><b>Mind Utilities <reset><dark_gray>» ";
    private static GhostRule instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        this.chatFilter = new ChatFilterManager(this);
        getServer().getPluginManager().registerEvents(new EventListener(this, chatFilter), this);

        getCommand("filter").setExecutor(new Commands(chatFilter));
        getCommand("filter").setTabCompleter(new Commands(chatFilter));
        getLogger().info("Starting up...");
    }
    public static GhostRule getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("BYE NERDSSS");
    }
}
