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

CREATE TYPE poll_type AS ENUM ('SCHEDULE','AGENT');
CREATE TABLE active_poll
(
    id         BIGINT       NOT NULL PRIMARY KEY,
    start_at   TIMESTAMP(6) NOT NULL,
    message_id BIGINT UNIQUE,
    end_at     TIMESTAMP(6) NOT NULL,
    poll_type  POLL_TYPE    NOT NULL,
    guild_id   BIGINT       NOT NULL
        CONSTRAINT guild_id_guild_fk
            REFERENCES guild ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (guild_id, poll_type)
);

CREATE TABLE active_poll_event_reference
(
    active_poll_id BIGINT NOT NULL
        CONSTRAINT active_poll_id_active_poll_fk
            REFERENCES active_poll ON DELETE CASCADE ON UPDATE CASCADE,
    event_id       BIGINT NOT NULL
        CONSTRAINT event_id_event_fk
            REFERENCES event ON DELETE CASCADE ON UPDATE CASCADE,
    order_index    BIGINT NOT NULL,
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

CREATE TYPE agent_role AS ENUM ('DUELIST','INITIATOR','CONTROLLER', 'SENTINEL');
CREATE TABLE agent_vote
(
    id              BIGINT      NOT NULL PRIMARY KEY,
    discord_user_id BIGINT      NOT NULL,
    agent_name      VARCHAR(20) NOT NULL,
    agent_role      AGENT_ROLE  NOT NULL,
    active_poll_id  BIGINT      NOT NULL
        CONSTRAINT active_poll_id_active_poll_fk
            REFERENCES active_poll ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE OR REPLACE FUNCTION verify_agent_poll_type()
    RETURNS TRIGGER AS
$$
BEGIN
    IF NOT EXISTS (SELECT 1
                   FROM active_poll
                   WHERE id = NEW.active_poll_id
                     AND poll_type = 'AGENT') THEN
        RAISE EXCEPTION 'Foreign key violation: The referenced poll (ID %) is not an AGENT poll.', NEW.active_poll_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER enforce_agent_vote_poll_type
    BEFORE INSERT OR UPDATE
    ON agent_vote
    FOR EACH ROW
EXECUTE FUNCTION verify_agent_poll_type();


