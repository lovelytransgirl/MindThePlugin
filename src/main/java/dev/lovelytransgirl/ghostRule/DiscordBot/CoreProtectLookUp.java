package dev.lovelytransgirl.ghostRule.DiscordBot;

import dev.lovelytransgirl.ghostRule.GhostRule;
import net.coreprotect.CoreProtectAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CoreProtectLookUp extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        GhostRule.getInstance().getLogger().info("Discord Command Recieved, event name is " + event.getName());
        if (event.getName().equals("testapi")) {
            CoreProtectAPI api = GhostRule.getInstance().getCoreProtect();
            if (api != null) {
                event.reply("[CoreProtect - JDA] API Tested successfully!\n" + api).queue(); // reply immediately
            } else {
                event.reply("Uh oh, CoreProtect ``api`` is null").queue(); // reply immediately
            }
        }

        if (event.getName().equals("cpl")) {
            CoreProtectAPI api = GhostRule.getInstance().getCoreProtect();
            if (api != null) {
                event.deferReply().queue();
                List<String> playerList = new ArrayList<>();
                playerList.add(event.getOption("player").getAsString());

                event.getOption("action").getAsString();
                List<Integer> action = new ArrayList<>();
                if (event.getOption("action").getAsString() == "Break") {
                    action.add(0);
                }
                if (event.getOption("action").getAsString() == "Place") {
                    action.add(1);
                }
                if (event.getOption("action").getAsString() == "Interact") {
                    action.add(2);
                }
                List<String[]> lookup = api.performLookup(10, playerList, null, null, null, action, 0, null);
                if (lookup != null) {
                    for (String[] result : lookup){
                        CoreProtectAPI.ParseResult parseResult = api.parseResult(result);
                        event.getHook().sendMessage("Here are the result for " + event.getOption("player").getAsString()).queue();
                        event.getHook().sendMessage(parseResult.getPlayer()).queue();;
                        event.getHook().sendMessage(parseResult.getActionString()).queue();;
                        event.getHook().sendMessage("" + parseResult.getX()).queue();;
                        event.getHook().sendMessage("" + parseResult.getY()).queue();;
                        event.getHook().sendMessage("" + parseResult.getZ()).queue();;
                        event.getHook().sendMessage("" + parseResult.getTimestamp()).queue();
                        List<String> embedLines = new ArrayList<>();

                        String time = formatTime(parseResult.getTimestamp());
                        String player = parseResult.getPlayer();
                        int x = parseResult.getX();
                        int y = parseResult.getY();
                        int z = parseResult.getZ();
                        String block = parseResult.getBlockData().getMaterial().toString();
                        String line = String.format("%-10s %-12s %-15s %-22s %s",
                                time,
                                player,
                                block,
                                "X: " + x + " Y: " + y + " Z: " + z,
                                event.getOption("action").getAsString()
                        );
                        embedLines.add(line);

                        StringBuilder description = new StringBuilder("```\n");
                        description.append("---------------------------------------------------------------------\n");
                        description.append("Time       Player       Block           Coords               Action\n");
                        description.append("---------------------------------------------------------------------\n");

                        for (String l : embedLines) {
                            description.append(l).append("\n");
                        }
                        description.append("```");

                        MessageEmbed embed = new EmbedBuilder()
                                .setDescription(description.toString())
                                .setFooter("Results for " + event.getOption("player").getAsString())
                                .setColor(Color.GREEN)
                                .setAuthor("CoreProtect Lookup", null, "https://cdn.discordapp.com/icons/1343118094427750483/c634ae010f60ab1972256a050c75f365.webp?size=512")
                                .build();

                        event.reply(MessageCreateData.fromEmbeds(embed)).queue(
                                success -> GhostRule.getInstance().getLogger().fine("CoreProtect Lookup Send success yessirrr"),
                                error -> GhostRule.getInstance().getLogger().warning("Failed to send CPL: " + error.getMessage())
                        );

                    }
                } else {
                    event.reply("It's null").queue();
                }
                // event.reply(event.getOption("time").getAsInt() + event.getOption("player").getAsString() + event.getOption("action").getAsString()).queue();
            } else {
                event.reply("Unable to lookup! API is null").queue();
            }
        }
    }

    private String formatTime(long timestamp) {
        java.time.Instant instant = java.time.Instant.ofEpochSecond(timestamp);
        java.time.LocalTime localTime = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()).toLocalTime();
        return localTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
    }
}