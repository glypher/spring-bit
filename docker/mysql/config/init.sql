CREATE DATABASE springbit;
USE springbit;

GRANT SELECT, INSERT, UPDATE, DELETE ON springbit.* TO 'spring-bit-user'@'%';
FLUSH PRIVILEGES;

CREATE TABLE blocks (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    symbol       VARCHAR(80),
    block_hash   VARCHAR(80),
    block_size   FLOAT NOT NULL
);

CREATE TABLE crypto_quote (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(80),
    symbol       VARCHAR(80),
    date         TIMESTAMP,
    usd          FLOAT NOT NULL
);

INSERT INTO blocks VALUES (default, "bbb", "sds", 2.0);