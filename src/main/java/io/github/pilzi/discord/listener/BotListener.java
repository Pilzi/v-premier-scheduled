package io.github.pilzi.discord.listener;

import io.github.pilzi.database.workers.EventWorker;
import io.github.pilzi.database.workers.SeasonWorker;
import io.github.pilzi.discord.utils.Message.DropdownUtil;
import io.github.pilzi.service.services.ImportService;
import io.github.pilzi.service.services.PollService;
import io.github.pilzi.service.services.impl.ImportServiceImpl;
import io.github.pilzi.service.services.impl.PollServiceImpl;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BotListener extends ListenerAdapter {
    @NonNull
    public static final String CHANNEL_NAME = "test";
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
    private final PollService pollService = new PollServiceImpl(eventWorker);

    @Override
    public void onReady(@NonNull ReadyEvent event) {
        // Schedul regular event to fill the database with premier event data
        scheduler.scheduleAtFixedRate(() -> pollService.handlePollForAllGuilds(event.getJDA().getGuilds()), SCHEDULE_INITIAL_DELAY_IN_HOURS, SCHEDULE_PERIOD_IN_HOURS, TimeUnit.DAYS);
        scheduler.scheduleAtFixedRate(importService::importPremierData, SCHEDULE_INITIAL_DELAY_IN_HOURS, SCHEDULE_PERIOD_IN_HOURS, TimeUnit.DAYS);

        List<Guild> guilds = event.getJDA().getGuilds();

        for (Guild guild : guilds) {
            List<GuildChannel> channels = guild.getChannels();
            Optional<GuildChannel> guildChannelOptional = channels.stream()
                    .filter(channel -> channel.getName().equals(CHANNEL_NAME))
                    .findFirst();

            if (guildChannelOptional.isPresent()) {
                GuildChannel guildChannel = guildChannelOptional.get();

                String channelId = guildChannel.getId();
                TextChannel textChannel = guildChannel.getGuild().getTextChannelById(channelId);
                if (textChannel != null) {
                    Button submitButton = Button.success(AGENDA_SUBMIT, "submit");
                    ActionRow actionRow = ActionRow.of(submitButton);
                    List<ActionRow> actionRows = new ArrayList<>(DropdownUtil.buildAgendActionRows());
                    actionRows.add(actionRow);
                    textChannel.sendMessageComponents(actionRows)
                            .queue();
                }
            }
        }
    }

    @Override
    public void onButtonInteraction(@NonNull ButtonInteractionEvent event) {
        if (event.getComponentId().equals(AGENDA_SUBMIT)) {
            event.reply("Selection has been saved").setEphemeral(true).queue(); // send a message in the channel
        }
    }

    @Override
    public void onGenericSelectMenuInteraction(@NonNull GenericSelectMenuInteractionEvent<?, ?> event) {
        event.deferEdit().queue();
    }
}
