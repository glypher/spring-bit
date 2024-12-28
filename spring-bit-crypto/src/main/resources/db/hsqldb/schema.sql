CREATE TABLE blocks (
    id           IDENTITY NOT NULL PRIMARY KEY,
    symbol       VARCHAR(80),
    block_hash   VARCHAR(80),
    block_size   INTEGER NOT NULL
);

CREATE TABLE crypto_quote (
    id           IDENTITY NOT NULL PRIMARY KEY,
    name         VARCHAR(80),
    symbol       VARCHAR(80),
    date         TIMESTAMP,
    usd          INTEGER NOT NULL
);
