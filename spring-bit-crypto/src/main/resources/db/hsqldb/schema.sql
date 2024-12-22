CREATE TABLE dodge IF NOT EXISTS (
    id           IDENTITY NOT NULL PRIMARY KEY,
    block_hash   VARCHAR(80),
    block_size   INTEGER NOT NULL
);
