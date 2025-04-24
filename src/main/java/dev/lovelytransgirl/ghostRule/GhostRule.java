package dev.lovelytransgirl.ghostRule;

import dev.lovelytransgirl.ghostRule.ChatFilter.ChatFilterManager;
import dev.lovelytransgirl.ghostRule.DiscordBot.Bot;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import net.luckperms.api.LuckPerms;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.logging.Level;

public final class GhostRule extends JavaPlugin {
    private ChatFilterManager chatFilter;
    public final String prefix = "<gradient:#a77df0:#602fb5><b>Mind Utilities <reset><dark_gray>» ";
    public LuckPerms luckPerms;
    private static GhostRule instance;
    public Bot bot;
    private File configFile;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }

        configFile = new File(this.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                this.saveResource("config.yml", false);
                this.getLogger().info("Loading default config!!");
            } catch (Exception e) {
                this.getLogger().log(Level.SEVERE, "For whatever reason, Can't create config file.", e);
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        String token = config.getString("discord.token");
        if (token == null || token.isEmpty()) {
            getLogger().info("token not found");
            return;
        }
        this.bot = new Bot(token, this);
        this.luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        this.chatFilter = new ChatFilterManager(this);
        getServer().getPluginManager().registerEvents(new EventListener(this, chatFilter), this);

        getCommand("filter").setExecutor(new Commands(chatFilter));
        getCommand("filter").setTabCompleter(new Commands(chatFilter));
        getLogger().info("Starting up...");
        AsyncScheduler asyncScheduler = getServer().getAsyncScheduler();
        Start task = new Start(asyncScheduler);
        task.run(this);
    }
    public static GhostRule getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("BYE NERDSSS");
        bot.sendEmbedMessage("Server Stopped", null, "The server has stopped!", null, "RED", null, "1364870023104954409");
        if (bot != null) {
            bot.shutdown();
        }
    }

    public Bot getBot() {
        return bot;
    }
}
