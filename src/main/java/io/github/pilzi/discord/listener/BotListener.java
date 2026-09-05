package io.github.pilzi.discord.listener;

import io.github.pilzi.discord.utils.Message.DropdownUtil;
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

public class BotListener extends ListenerAdapter {
    public static final String CHANNEL_NAME = "test";
    public static final String AGENDA_SUBMIT = "agenda-submit";

    @Override
    public void onReady(@NonNull ReadyEvent event) {
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
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals(AGENDA_SUBMIT)) {
            event.reply("Selection has been saved").setEphemeral(true).queue(); // send a message in the channel
        }
    }

    @Override
    public void onGenericSelectMenuInteraction(@NonNull GenericSelectMenuInteractionEvent<?, ?> event) {
        event.deferEdit().queue();
    }
}
