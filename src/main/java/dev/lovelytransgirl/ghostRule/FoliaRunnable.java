package dev.lovelytransgirl.ghostRule;

import io.papermc.paper.threadedregions.scheduler.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

/**
 * Represents an abstract runnable that can be scheduled using various Folia-specific schedulers:
 * <ul>
 *     <li>{@link AsyncScheduler}</li>
 *     <li>{@link EntityScheduler}</li>
 *     <li>{@link GlobalRegionScheduler}</li>
 *     <li>{@link RegionScheduler}</li>
 * </ul>
 * <p>
 * Depending on which scheduler you use, tasks will be executed in different contexts (asynchronously,
 * on an entity's region, on the global region, or on a specific region).
 * </p>
 */
public abstract class FoliaRunnable implements Runnable {

    private ScheduledTask task;

    // Schedulers
    private @Nullable AsyncScheduler asyncScheduler;
    private @Nullable EntityScheduler entityScheduler;
    private @Nullable GlobalRegionScheduler globalRegionScheduler;
    private @Nullable RegionScheduler regionScheduler;

    // For async scheduling
    private @Nullable TimeUnit timeUnit;

    // For entity scheduling (optional retired callback)
    private @Nullable Runnable entityRetired;

    // For region scheduling by Location or chunk coordinates
    private @Nullable Location location;
    private @Nullable World world;
    private int chunkX;
    private int chunkZ;

    /**
     * Constructs a new {@code FoliaRunnable} that will be executed asynchronously by the provided scheduler.
     *
     * @param scheduler an {@link AsyncScheduler} to schedule the task asynchronously
     * @param timeUnit  the time unit to use for delayed/periodic tasks
     */
    public FoliaRunnable(@NotNull AsyncScheduler scheduler, @Nullable TimeUnit timeUnit) {
        this.asyncScheduler = scheduler;
        this.timeUnit = timeUnit;
    }

    /**
     * Constructs a new {@code FoliaRunnable} that will be executed on an entity's region.
     * An optional {@code retired} callback can be provided if the entity is removed before execution.
     *
     * @param scheduler an {@link EntityScheduler} to schedule the task on an entity's region
     * @param retired   a callback invoked if the entity is removed before the task can run
     */
    public FoliaRunnable(@NotNull EntityScheduler scheduler, @Nullable Runnable retired) {
        this.entityScheduler = scheduler;
        this.entityRetired = retired;
    }

    /**
     * Constructs a new {@code FoliaRunnable} that will be executed on the global region.
     *
     * @param scheduler a {@link GlobalRegionScheduler} for scheduling on the global region
     */
    public FoliaRunnable(@NotNull GlobalRegionScheduler scheduler) {
        this.globalRegionScheduler = scheduler;
    }

    /**
     * Constructs a new {@code FoliaRunnable} that will be executed on the region
     * owning the given {@link Location}.
     *
     * @param scheduler a {@link RegionScheduler} for scheduling on a specific region
     * @param location  a {@link Location} to determine the owning region
     */
    public FoliaRunnable(@NotNull RegionScheduler scheduler, @Nullable Location location) {
        this.regionScheduler = scheduler;
        this.location = location;
    }

    /**
     * Constructs a new {@code FoliaRunnable} that will be executed on the region
     * owning the given chunk coordinates.
     *
     * @param scheduler a {@link RegionScheduler} for scheduling on a specific region
     * @param world     the {@link World} containing the chunk
     * @param chunkX    the chunk X coordinate
     * @param chunkZ    the chunk Z coordinate
     */
    public FoliaRunnable(@NotNull RegionScheduler scheduler, @NotNull World world, int chunkX, int chunkZ) {
        this.regionScheduler = scheduler;
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    /**
     * Checks if this task has been cancelled.
     *
     * @return {@code true} if the task has been cancelled
     * @throws IllegalStateException if the task has not been scheduled yet
     */
    public boolean isCancelled() throws IllegalStateException {
        checkScheduled();
        return task.isCancelled();
    }

    /**
     * Cancels this scheduled task. Future executions will be halted,
     * but if the task is already running, it will not stop immediately.
     *
     * @throws IllegalStateException if the task has not been scheduled yet
     */
    public void cancel() throws IllegalStateException {
        checkScheduled();
        task.cancel();
    }

    /**
     * Schedules this {@code FoliaRunnable} to run as soon as possible (the next tick in a region-based scheduler,
     * or immediately if it's an asynchronous scheduler).
     *
     * @param plugin the reference to the plugin scheduling the task
     * @return the {@link ScheduledTask} representing this scheduled runnable
     * @throws IllegalArgumentException      if the plugin is {@code null}
     * @throws IllegalStateException         if this runnable has already been scheduled
     * @throws UnsupportedOperationException if the scheduler type is unsupported
     */
    @NotNull
    public ScheduledTask run(@NotNull Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null.");
        }
        checkNotYetScheduled();

        if (this.globalRegionScheduler != null) {
            // Global region
            return setupTask(this.globalRegionScheduler.run(plugin, scheduledTask -> FoliaRunnable.this.run()));
        } else if (this.entityScheduler != null) {
            // Entity region
            return setupTask(this.entityScheduler.run(plugin, scheduledTask -> FoliaRunnable.this.run(), entityRetired));
        } else if (this.regionScheduler != null) {
            // Region by location or chunk coords
            if (this.location != null) {
                return setupTask(this.regionScheduler.run(plugin, location, scheduledTask -> FoliaRunnable.this.run()));
            } else if (this.world != null) {
                return setupTask(this.regionScheduler.run(plugin, world, chunkX, chunkZ, scheduledTask -> FoliaRunnable.this.run()));
            } else {
                throw new UnsupportedOperationException("Cannot schedule on region: no location or (world, chunkX, chunkZ) provided.");
            }
        } else if (this.asyncScheduler != null) {
            // Asynchronous
            return setupTask(this.asyncScheduler.runNow(plugin, scheduledTask -> FoliaRunnable.this.run()));
        } else {
            throw new UnsupportedOperationException("No scheduler available for this FoliaRunnable.");
        }
    }

    /**
     * Schedules this {@code FoliaRunnable} to be executed after a certain number of server ticks have passed.
     *
     * @param plugin the plugin responsible for scheduling this task
     * @param delay  the delay in server ticks before execution; values less than 1 are treated as 1
     * @return the {@link ScheduledTask} representing this scheduled runnable
     * @throws IllegalArgumentException      if the plugin is {@code null}
     * @throws IllegalStateException         if this runnable has already been scheduled
     * @throws UnsupportedOperationException if the scheduler type or context is unsupported
     */
    @NotNull
    public ScheduledTask runDelayed(@NotNull Plugin plugin, long delay) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null.");
        }
        checkNotYetScheduled();

        delay = Math.max(1, delay);

        if (this.globalRegionScheduler != null) {
            // Global region
            return setupTask(this.globalRegionScheduler.runDelayed(plugin, scheduledTask -> FoliaRunnable.this.run(), delay));
        } else if (this.entityScheduler != null) {
            // Entity region
            return setupTask(this.entityScheduler.runDelayed(plugin, scheduledTask -> FoliaRunnable.this.run(), entityRetired, delay));
        } else if (this.regionScheduler != null) {
            // Region by location or chunk coords
            if (this.location != null) {
                return setupTask(this.regionScheduler.runDelayed(plugin, location, scheduledTask -> FoliaRunnable.this.run(), delay));
            } else if (this.world != null) {
                return setupTask(this.regionScheduler.runDelayed(plugin, world, chunkX, chunkZ, scheduledTask -> FoliaRunnable.this.run(), delay));
            } else {
                throw new UnsupportedOperationException("Cannot schedule delayed on region: no location or (world, chunkX, chunkZ) provided.");
            }
        } else if (this.asyncScheduler != null && this.timeUnit != null) {
            // Asynchronous
            return setupTask(this.asyncScheduler.runDelayed(plugin, scheduledTask -> FoliaRunnable.this.run(), delay, timeUnit));
        } else {
            throw new UnsupportedOperationException("No scheduler available or time unit not set for this FoliaRunnable.");
        }
    }

    /**
     * Schedules this {@code FoliaRunnable} to run repeatedly until cancelled, starting
     * after the specified delay and repeating at the specified period.
     *
     * @param plugin the plugin responsible for scheduling this task
     * @param delay  the initial delay in ticks before first execution; values less than 1 are treated as 1
     * @param period the interval in ticks between consecutive executions; values less than 1 are treated as 1
     * @return the {@link ScheduledTask} representing this scheduled runnable
     * @throws IllegalArgumentException      if the plugin is {@code null}
     * @throws IllegalStateException         if this runnable has already been scheduled
     * @throws UnsupportedOperationException if the scheduler type or context is unsupported
     */
    @NotNull
    public ScheduledTask runAtFixedRate(@NotNull Plugin plugin, long delay, long period) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null.");
        }
        checkNotYetScheduled();

        delay = Math.max(1, delay);
        period = Math.max(1, period);

        if (this.globalRegionScheduler != null) {
            // Global region
            return setupTask(this.globalRegionScheduler.runAtFixedRate(plugin, scheduledTask -> FoliaRunnable.this.run(), delay, period));
        } else if (this.entityScheduler != null) {
            // Entity region
            return setupTask(this.entityScheduler.runAtFixedRate(plugin, scheduledTask -> FoliaRunnable.this.run(), entityRetired, delay, period));
        } else if (this.regionScheduler != null) {
            // Region by location or chunk coords
            if (this.location != null) {
                return setupTask(this.regionScheduler.runAtFixedRate(plugin, location, scheduledTask -> FoliaRunnable.this.run(), delay, period));
            } else if (this.world != null) {
                return setupTask(this.regionScheduler.runAtFixedRate(plugin, world, chunkX, chunkZ, scheduledTask -> FoliaRunnable.this.run(), delay, period));
            } else {
                throw new UnsupportedOperationException("Cannot schedule fixed rate on region: no location or (world, chunkX, chunkZ) provided.");
            }
        } else if (this.asyncScheduler != null && this.timeUnit != null) {
            // Asynchronous
            return setupTask(this.asyncScheduler.runAtFixedRate(plugin, scheduledTask -> FoliaRunnable.this.run(), delay, period, timeUnit));
        } else {
            throw new UnsupportedOperationException("No scheduler available or time unit not set for this FoliaRunnable.");
        }
    }

    /**
     * Retrieves the ID of the scheduled task once it has been scheduled.
     *
     * @return the ID of the scheduled task
     * @throws IllegalStateException if the task has not been scheduled yet
     */
    public int getTaskId() throws IllegalStateException {
        checkScheduled();
        return task.hashCode();
    }

    /**
     * Validates that this {@code FoliaRunnable} has already been scheduled,
     * throwing an exception otherwise.
     *
     * @throws IllegalStateException if no {@link ScheduledTask} is assigned yet
     */
    private void checkScheduled() {
        if (task == null) {
            throw new IllegalStateException("This FoliaRunnable has not been scheduled yet.");
        }
    }

    /**
     * Validates that this {@code FoliaRunnable} has not been scheduled yet,
     * throwing an exception otherwise.
     *
     * @throws IllegalStateException if this {@code FoliaRunnable} is already scheduled
     */
    private void checkNotYetScheduled() {
        if (task != null) {
            throw new IllegalStateException("This FoliaRunnable has already been scheduled as task ID: " + task.hashCode());
        }
    }

    /**
     * Internal helper method to store the returned {@link ScheduledTask} reference
     * in this {@code FoliaRunnable} and return it back.
     *
     * @param task the {@link ScheduledTask} returned by the scheduler
     * @return the same {@link ScheduledTask} reference
     */
    @NotNull
    private ScheduledTask setupTask(final ScheduledTask task) {
        this.task = task;
        return task;
    }
}