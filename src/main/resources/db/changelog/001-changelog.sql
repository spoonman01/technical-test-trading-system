-- liquibase formatted sql

-- changeset lucarospocher:0.0.1
CREATE TABLE wallet (id UUID, balance DECIMAL(18, 2), created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP, PRIMARY KEY (id));
CREATE TABLE charge (id UUID, amount DECIMAL(18, 2) NOT NULL, payment_id VARCHAR(255) NOT NULL, wallet_id UUID NOT NULL, PRIMARY KEY (id), created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP, PRIMARY KEY (id), FOREIGN KEY (wallet_id) REFERENCES wallet(id));
-- rollback drop table charge; drop table wallet;