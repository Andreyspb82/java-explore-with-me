DROP TABLE IF EXISTS users, categories, locations, events, requests, compilations, compilations_event;

CREATE TABLE IF NOT EXISTS users
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    name                 varchar (250)         NOT NULL,
    email                varchar(254)          NOT NULL unique
);

CREATE TABLE IF NOT EXISTS categories
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    name                 varchar (50)          NOT NULL unique
);

CREATE TABLE IF NOT EXISTS locations
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    lat                  float8                NOT NULL,
    lon                  float8                NOT NULL
);


CREATE TABLE IF NOT EXISTS events
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    annotation           varchar (2000)        NOT NULL,
    category_id          BIGINT                references categories (id) ON DELETE NO ACTION NOT NULL,
    confirmed_requests   integer,
    created_on           timestamp             WITHOUT TIME ZONE,
    description          varchar (7000),
    event_date           timestamp             WITHOUT TIME ZONE,
    initiator_id         BIGINT                references users (id) ON DELETE CASCADE,
    paid                 boolean,
    participant_limit    BIGINT,
    published_on         timestamp             WITHOUT TIME ZONE,
    request_moderation   boolean,
    state                varchar (50),
    title                varchar (120),
    location_id          BIGINT                references locations (id),
    views                BIGINT
);

CREATE TABLE IF NOT EXISTS requests
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    created              timestamp,
    event_id             BIGINT                references events (id) ON DELETE CASCADE,
    requester_id         BIGINT                references users (id) ON DELETE CASCADE,
    status               varchar (50),
    CONSTRAINT           UC_requests UNIQUE (event_id,requester_id)
);

CREATE TABLE IF NOT EXISTS compilations
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    pinned              boolean,
    title               varchar (50)
);

CREATE TABLE IF NOT EXISTS compilations_event
(
    compilation_id      BIGINT                 references compilations (id) ON DELETE CASCADE,
    event_id            BIGINT                 references events (id) ON DELETE CASCADE
);


