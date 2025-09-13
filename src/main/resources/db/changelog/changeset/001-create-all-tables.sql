-- liquibase formatted sql

-- changeset dev:001-create-users-table

CREATE TABLE users
(
    id                   UUID PRIMARY KEY,
    phone_number         VARCHAR(32)  NOT NULL UNIQUE,
    first_name           VARCHAR(100) NOT NULL,
    last_name            VARCHAR(100) NOT NULL,
    password             VARCHAR(255) NOT NULL,
    role                 VARCHAR(20)  NOT NULL, -- Enum STRING (Role): ADMIN/USER
    created_at           TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    refresh_token        TEXT,
    refresh_token_expiry TIMESTAMP WITHOUT TIME ZONE
);
-- rollback DROP TABLE users;

-- changeset dev:002-create-cards-table
CREATE TABLE cards
(
    id            UUID         NOT NULL,
    user_id       UUID         NOT NULL,
    pan_encrypted VARCHAR(512) NOT NULL,
    pan_last4     VARCHAR(4)   NOT NULL,
    fingerprint   VARCHAR(64)  NOT NULL,
    card_holder   VARCHAR(255) NOT NULL,
    expiry_date   date         NOT NULL,
    balance       NUMERIC(36,2) NOT NULL DEFAULT 0,
    status        VARCHAR(16) NOT NULL,
    CONSTRAINT pk_cards PRIMARY KEY (id),
    CONSTRAINT fk_cards_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_card_fingerprint UNIQUE (fingerprint),
    CONSTRAINT uq_user_last4 UNIQUE (user_id, pan_last4)
);

-- rollback DROP TABLE cards;

-- changeset dev:003-indexes
CREATE INDEX ix_cards_last4 ON cards (pan_last4);

CREATE INDEX ix_cards_user_last4 ON cards (user_id, pan_last4);

-- rollback DROP INDEX IF EXISTS ix_cards_user_last4;
-- rollback DROP INDEX IF EXISTS ix_cards_last4;
