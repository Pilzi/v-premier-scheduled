CREATE TABLE season
(
    id        BIGINT       NOT NULL PRIMARY KEY,
    season_id UUID         NOT NULL UNIQUE,
    start_at  timestamp(6) NOT NULL,
    end_at    timestamp(6) NOT NULL
);

CREATE TABLE event
(
    id         BIGINT       NOT NULL PRIMARY KEY,
    map        varchar(20)  NOT NULL,
    start_at   timestamp(6) NOT NULL,
    end_at     timestamp(6) NOT NULL,
    conference varchar(20)  NOT NULL,
    season_id  BIGINT       NOT NULL
        CONSTRAINT season_id_season_fk
            REFERENCES season ON DELETE CASCADE ON UPDATE CASCADE
)
