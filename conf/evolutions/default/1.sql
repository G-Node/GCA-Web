# --- DB evalulation for anorm db access


# --- !Ups

CREATE TABLE Tokens (
    id varchar(36) NOT NULL,
    email varchar(255) NOT NULL,
    creationTime TIMESTAMP NOT NULL,
    expirationTime TIMESTAMP NOT NULL,
    isSignUp boolean NOT NULL,
    PRIMARY KEY (id)
);


# --- !Downs

DROP TABLE Tokens;