package io.github.pilzi.database.workers;

import io.github.pilzi.database.domain.GuildEntity;
import net.dv8tion.jda.api.entities.Guild;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jspecify.annotations.NonNull;

public class GuildWorker {

    @NonNull
    public GuildEntity getOrCreate(@NonNull Session session,
                                   @NonNull Guild guild) {
        Query<GuildEntity> query = session.createQuery("SELECT guild FROM GuildEntity guild WHERE guild.discordId = :guildId", GuildEntity.class)
                .setParameter("guildId", guild.getId());


        GuildEntity guildEntity = query.getSingleResultOrNull();

        if (guildEntity == null) {
            GuildEntity newGuildEntity = new GuildEntity(guild.getId(),
                    guild.getName());
            save(session, newGuildEntity);
            return newGuildEntity;
        }

        return guildEntity;
    }

    public void save(@NonNull Session session,
                     @NonNull GuildEntity guildEntity) {
        session.persist(guildEntity);
    }
}
