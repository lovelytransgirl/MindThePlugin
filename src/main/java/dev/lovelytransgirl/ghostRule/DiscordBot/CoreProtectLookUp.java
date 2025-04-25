package dev.lovelytransgirl.ghostRule.DiscordBot;

import dev.lovelytransgirl.ghostRule.GhostRule;
import net.coreprotect.CoreProtectAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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
            if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                event.deferReply().queue();

                new Thread(() -> {
                    try {
                        CoreProtectAPI api = GhostRule.getInstance().getCoreProtect();
                        if (api == null) {
                            event.getHook().sendMessage("Unable to lookup! API is null").queue();
                            return;
                        }

                        List<String> playerList = List.of(event.getOption("player").getAsString());

                        List<Integer> action = new ArrayList<>();
                        String actionStr = event.getOption("action").getAsString();
                        if (actionStr.equals("Break")) {
                            action.add(0);
                        } else if (actionStr.equals("Place")) {
                            action.add(1);
                        } else if (actionStr.equals("Interact")) {
                            action.add(2);
                        }

                        List<String[]> lookup = api.performLookup(10, playerList, null, null, null, action, 0, null);

                        if (lookup == null || lookup.isEmpty()) {
                            event.getHook().sendMessage("No results found for " + event.getOption("player").getAsString()).queue();
                            return;
                        }

                        StringBuilder description = new StringBuilder("```\n");
                        description.append("----------------------------------------\n");
                        description.append("Time   Player   Block    Coords   Action\n");
                        description.append("----------------------------------------\n");

                        for (String[] result : lookup) {
                            CoreProtectAPI.ParseResult parseResult = api.parseResult(result);
                            String time = formatTime(parseResult.getTimestamp());
                            String player = parseResult.getPlayer();
                            int x = parseResult.getX();
                            int y = parseResult.getY();
                            int z = parseResult.getZ();
                            String block = parseResult.getBlockData().getMaterial().toString();

                            description.append(String.format("%-3s %-3s %-3s %-3s %s\n",
                                    time,
                                    player,
                                    block,
                                    "X:" + x + " Y:" + y + " Z:" + z,
                                    actionStr
                            ));
                        }
                        description.append("```");

                        MessageEmbed embed = new EmbedBuilder()
                                .setDescription(description.toString())
                                .setFooter("Results for " + event.getOption("player").getAsString())
                                .setColor(Color.GREEN)
                                .setAuthor("CoreProtect Lookup", null, "https://cdn.discordapp.com/icons/1343118094427750483/c634ae010f60ab1972256a050c75f365.webp?size=512")
                                .build();

                        // Use complete() instead of queue() to ensure thread safety
                        event.getHook().sendMessageEmbeds(embed).complete();
                    } catch (Exception e) {
                        event.getHook().sendMessage("An error occurred during lookup: " + e.getMessage()).queue();
                        GhostRule.getInstance().getLogger().severe("Error in CoreProtect lookup: " + e.getMessage());
                    }
                }).start();
            } else {
                event.reply(":x: You do not have administrator permission.");
            }
        }
    }

    private String formatTime(long timestamp) {
        java.time.Instant instant = java.time.Instant.ofEpochSecond(timestamp);
        java.time.LocalTime localTime = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()).toLocalTime();
        return localTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
    }
}