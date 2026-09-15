CREATE TABLE season
(
    id          BIGINT       NOT NULL PRIMARY KEY,
    external_id UUID         NOT NULL UNIQUE,
    start_at    TIMESTAMP(6) NOT NULL,
    end_at      TIMESTAMP(6) NOT NULL
);

CREATE TABLE event
(
    id          BIGINT       NOT NULL PRIMARY KEY,
    map         VARCHAR(20)  NOT NULL,
    start_at    TIMESTAMP(6) NOT NULL,
    end_at      TIMESTAMP(6) NOT NULL,
    is_practice BOOLEAN      NOT NULL DEFAULT TRUE,
    conference  VARCHAR(20)  NOT NULL,
    season_id   BIGINT       NOT NULL
        CONSTRAINT season_id_season_fk
            REFERENCES season ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE guild
(
    id         BIGINT NOT NULL PRIMARY KEY,
    discord_id VARCHAR(30) UNIQUE,
    name       VARCHAR(100)
);

CREATE TABLE active_poll
(
    id         BIGINT       NOT NULL PRIMARY KEY,
    start_at   TIMESTAMP(6) NOT NULL,
    message_id BIGINT UNIQUE,
    end_at     TIMESTAMP(6) NOT NULL,
    guild_id   BIGINT       NOT NULL
        CONSTRAINT guild_id_guild_fk
            REFERENCES guild ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE active_poll_event_reference
(
    active_poll_id BIGINT NOT NULL
        CONSTRAINT active_poll_id_active_poll_fk
            REFERENCES active_poll ON DELETE CASCADE ON UPDATE CASCADE,
    event_id       BIGINT NOT NULL
        CONSTRAINT event_id_event_fk
            REFERENCES event ON DELETE CASCADE ON UPDATE CASCADE,
    order_index BIGINT NOT NULL,
    CONSTRAINT active_poll_event_reference_pk
        PRIMARY KEY (active_poll_id, event_id),
    CONSTRAINT active_poll_event_reference_order_unique
        UNIQUE (active_poll_id, order_index)
);

CREATE TABLE active_poll_vote
(
    id             BIGINT NOT NULL PRIMARY KEY,
    active_poll_id BIGINT NOT NULL,
    event_id       BIGINT NOT NULL,
    user_id        BIGINT NOT NULL,
    CONSTRAINT active_poll_vote_reference_fk
        FOREIGN KEY (active_poll_id, event_id)
            REFERENCES active_poll_event_reference (active_poll_id, event_id)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT active_poll_vote_unique
        UNIQUE (active_poll_id, event_id, user_id)
);

CREATE TABLE hibernate_sequences
(
    sequence_name VARCHAR(255) NOT NULL PRIMARY KEY,
    next_val      BIGINT
);
