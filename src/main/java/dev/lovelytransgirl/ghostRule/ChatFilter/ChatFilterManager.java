package dev.lovelytransgirl.ghostRule.ChatFilter;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class ChatFilterManager {
    private final JavaPlugin plugin;
    private final List<Pattern> patterns = new ArrayList<>();
    private final PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();
    private final String bypassPermission = "ghostrule.filter.bypass";

    private File configFile;
    private FileConfiguration config;

    public ChatFilterManager(JavaPlugin plugin) {
        this.plugin = plugin;
        setupConfig();
        loadPattern();
    }

    public boolean isFiltered(Component message) {
        String plainText = serializer.serialize(message);
        return patterns.stream().anyMatch(pattern -> pattern.matcher(plainText).find());
    }

    public String getBypassPermission() {
        return bypassPermission;
    }
    public void reloadPatterns() {
        config = YamlConfiguration.loadConfiguration(configFile);
        loadPattern();
    }
    private void loadPattern() {
        patterns.clear();
        List<String> patternStrings = config.getStringList("patterns");

        for (String patternStr : patternStrings) {
            try {
                patterns.add(Pattern.compile(patternStr));
                plugin.getLogger().info("Loaded pattern: " + patternStr);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Not a valid regex pattern: " + patternStr, e);
            }
        }

        plugin.getLogger().info("Loaded " + patterns.size() + " filter patterns!!!");
    }
    private void setupConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        configFile = new File(plugin.getDataFolder(), "filter.yml");
        if (!configFile.exists()) {
            try {
                plugin.saveResource("filter.yml", false);
                plugin.getLogger().info("Loading default filter!!");
                config = YamlConfiguration.loadConfiguration(configFile);
                config.save(configFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "For whatever reason, Can't create CFC file.", e);
            }
        }
    }

    public int getPatternCount() {
        return patterns.size();
    }
}
