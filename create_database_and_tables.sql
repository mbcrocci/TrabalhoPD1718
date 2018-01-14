CREATE DATABASE trabalhopdg10;
USE trabalhopdg10;

CREATE TABLE users(
	username VARCHAR(45) NOT NULL PRIMARY KEY,
    password VARCHAR(45) NOT NULL,
    address VARCHAR(45) NOT NULL,
    port INTEGER NOT NULL,
    authenticated TINYINT(0)
);

CREATE TABLE pairs(
	user1 VARCHAR(45) NOT NULL PRIMARY KEY,
    user2 VARCHAR(45) NOT NULL,
    confirmed TINYINT(0)
);