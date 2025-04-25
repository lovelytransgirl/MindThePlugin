package dev.lovelytransgirl.ghostRule;

import dev.lovelytransgirl.ghostRule.ChatFilter.ChatFilterManager;
import dev.lovelytransgirl.ghostRule.DiscordBot.Bot;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import net.luckperms.api.LuckPerms;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

import java.io.File;
import java.util.logging.Level;

public final class GhostRule extends JavaPlugin {
    private ChatFilterManager chatFilter;
    public final String prefix = "<gradient:#a77df0:#602fb5><b>Mind <reset><dark_gray>» ";
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
        getCommand("hug").setExecutor(new Commands(chatFilter));
        getCommand("hug").setTabCompleter(new Commands(chatFilter));
        getCommand("kiss").setExecutor(new Commands(chatFilter));
        getCommand("kiss").setTabCompleter(new Commands(chatFilter));
        getCommand("help").setExecutor(new Commands(chatFilter));
        getLogger().info("Starting up...");
        AsyncScheduler asyncScheduler = getServer().getAsyncScheduler();
        Start task = new Start(asyncScheduler);
        task.run(this);
        CoreProtectAPI api = getCoreProtect();
        if (api != null){ // Ensure we have access to the API
            api.testAPI(); // Will print out "[CoreProtect] API test successful." in the console.
        }
    }

    public static GhostRule getInstance() {
        return instance;
    }

    public CoreProtectAPI getCoreProtect() {
        Plugin plugin = getServer().getPluginManager().getPlugin("CoreProtect");
        if (plugin == null || !(plugin instanceof CoreProtect)) {
            return null;
        }
        // Check that the API is enabled
        CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
        if (CoreProtect.isEnabled() == false) {
            return null;
        }

        // Check that a compatible version of the API is loaded
        if (CoreProtect.APIVersion() < 10) {
            return null;
        }

        return CoreProtect;
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
