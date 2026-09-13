package io.github.pilzi.discord.listener;

import io.github.pilzi.database.workers.*;
import io.github.pilzi.service.services.ImportService;
import io.github.pilzi.service.services.PollService;
import io.github.pilzi.service.services.impl.ImportServiceImpl;
import io.github.pilzi.service.services.impl.PollServiceImpl;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.poll.MessagePollVoteAddEvent;
import net.dv8tion.jda.api.events.message.poll.MessagePollVoteRemoveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BotListener extends ListenerAdapter {
    @NonNull
    public static final String AGENDA_SUBMIT = "agenda-submit";
    public static final int SCHEDULE_PERIOD_IN_HOURS = 1;
    public static final int SCHEDULE_INITIAL_DELAY_IN_HOURS = 0;
    @NonNull
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    @NonNull
    private final EventWorker eventWorker = new EventWorker();
    @NonNull
    private final SeasonWorker seasonWorker = new SeasonWorker();
    @NonNull
    private final ImportService importService = new ImportServiceImpl(seasonWorker, eventWorker);
    @NonNull
    private final GuildWorker guildWorker = new GuildWorker();
    @NonNull
    private final ActivePollEventReferenceWorker activePollEventReferenceWorker = new ActivePollEventReferenceWorker();
    @NonNull
    private final ActivePollVoteWorker activePollVoteWorker = new ActivePollVoteWorker();
    @NonNull
    private final PollService pollService = new PollServiceImpl(eventWorker, guildWorker, activePollEventReferenceWorker, activePollVoteWorker);

    @Override
    public void onReady(@NonNull ReadyEvent event) {
        // Schedul regular event to fill the database with premier event data
        scheduler.scheduleAtFixedRate(importService::importPremierData, SCHEDULE_INITIAL_DELAY_IN_HOURS, SCHEDULE_PERIOD_IN_HOURS, TimeUnit.DAYS);

        // Check if there is an active poll for the next event week
        scheduler.scheduleAtFixedRate(() -> pollService.handlePollForAllGuilds(event.getJDA().getGuilds()), SCHEDULE_INITIAL_DELAY_IN_HOURS, SCHEDULE_PERIOD_IN_HOURS, TimeUnit.DAYS);
    }

    @Override
    public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
        if (event.getComponentId().equals(AGENDA_SUBMIT)) {
            event.reply("Selection has been saved").setEphemeral(true).queue(); // send a message in the channel
        }
    }
    @Override
    public void onMessagePollVoteAdd(@NonNull MessagePollVoteAddEvent event) {
        pollService.addVote(event.getUserIdLong(), event.getAnswerId(), event.getMessageIdLong());
    }

    @Override
    public void onMessagePollVoteRemove(@NonNull MessagePollVoteRemoveEvent event) {
        pollService.removeVote(event.getUserIdLong(), event.getAnswerId(), event.getMessageIdLong());
    }

    @Override
    public void onGenericSelectMenuInteraction(@NonNull GenericSelectMenuInteractionEvent<?, ?> event) {
        event.deferEdit().queue();
    }
}
