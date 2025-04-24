package dev.lovelytransgirl.ghostRule;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;

public class Start extends FoliaRunnable {
    public Start(AsyncScheduler scheduler) {
        super(scheduler, null);
    }
    @Override
    public void run() {
        GhostRule.getInstance().bot.sendEmbedMessage("Server Started", null, "The server has started!", null, "GREEN", null, "1364870023104954409");
    }
}
