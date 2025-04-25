package dev.lovelytransgirl.ghostRule.DiscordBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;

public class Bot {
    private JDA jda;
    private final JavaPlugin plugin;

    public Bot(String token, JavaPlugin plugin) {
        this.plugin = plugin;

        try {
            this.jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .setActivity(Activity.watching("MindTheSMP"))
                    .setStatus(OnlineStatus.IDLE)
                    .addEventListeners(new CoreProtectLookUp())
                    .build();

            jda.updateCommands().addCommands(
                    Commands.slash("cpl", "Perform lookup task on CoreProtect using CoreProtect API.")
                            .addOption(OptionType.INTEGER, "time", "Time span to look up (eg. 10)", true)
                            .addOption(OptionType.STRING, "player", "Player you want to look up", true)
                            .addOption(OptionType.STRING, "action", "Action that player does (eg. break)"),
                    Commands.slash("testapi", "Test API between CoreProtect and JDA")
            ).queue();
            jda.awaitReady();
            plugin.getLogger().info("Discord bot connected successfully!");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize Discord bot: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
        }
    }

    public void sendMessage(String message, String channelId) {
        if (jda == null) {
            plugin.getLogger().warning("Discord bot is not initialized!");
            return;
        }

        MessageChannel channel = jda.getChannelById(MessageChannel.class, channelId);
        if (channel != null) {
            channel.sendMessage(message).queue(
                    success -> plugin.getLogger().fine("Message sent successfully"),
                    error -> plugin.getLogger().warning("Failed to send message: " + error.getMessage())
            );
        } else {
            plugin.getLogger().warning("Channel with ID " + channelId + " not found!");
        }
    }

    public void sendEmbedMessage(String title, String imageUrl, String description, String footer, String color, String author, String channelId) {
        if (jda == null) {
            plugin.getLogger().warning("Discord bot is not initialized!");
            return;
        }

        MessageChannel channel = jda.getChannelById(MessageChannel.class, channelId);
        if (channel != null) {
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle(title)
                    .setDescription(description)
                    .setFooter(footer)
                    .setColor(Color.GREEN)
                    .setAuthor(author, null, imageUrl)
                    .build();

            channel.sendMessage(MessageCreateData.fromEmbeds(embed)).queue(
                    success -> plugin.getLogger().fine("Embed sent successfully"),
                    error -> plugin.getLogger().warning("Failed to send embed: " + error.getMessage())
            );
        } else {
            plugin.getLogger().warning("Channel with ID " + channelId + " not found!");
        }
    }
}