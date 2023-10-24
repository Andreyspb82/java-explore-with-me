DROP TABLE IF EXISTS hits;

CREATE TABLE IF NOT EXISTS hits
(
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    app varchar (255)                          NOT NULL,
    uri varchar(255)                           NOT NULL,
    ip varchar(255)                            NOT NULL,
    created timestamp                          WITHOUT TIME ZONE
);