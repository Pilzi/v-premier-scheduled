package io.github.pilzi.discord.utils;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerUtil {
    @NonNull
    private static final Logger logger = LoggerFactory.getLogger(SchedulerUtil.class);

    public static void safe(@NonNull String jobName,
                            @NonNull Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            logger.error("Scheduled job failed: job={}", jobName, e);
        }
    }
}
