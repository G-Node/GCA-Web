# --- DB evalulation for anorm db access


# --- !Ups

CREATE TABLE Tokens (
    id varchar(36) NOT NULL,
    email varchar(255) NOT NULL,
    creationTime DATETIME NOT NULL,
    expirationTime DATETIME NOT NULL,
    isSignUp boolean NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE Users (
    id varchar(255) NOT NULL,
    provider varchar(255) NOT NULL,

    firstName varchar(255) NOT NULL,
    lastName varchar(255) NOT NULL,
    fullName varchar(255) NOT NULL,

    email varchar(255),
    avatar varchar(1024),
    authMethod varchar(255) NOT NULL,
    oa1Token varchar(255),
    oa1Secret varchar(255),
    oa2Token varchar(1024),
    oa2Type varchar(255),
    oa2ExpiresIn integer,
    oa2RefreshToken varchar(1024),

    pwHasher varchar(1024),
    pwPassword varchar(1014),
    pwSalt varchar(1024),

    PRIMARY KEY(id, provider)
);


# --- !Downs

DROP TABLE Tokens;
DROP TABLE Users;