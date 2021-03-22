CREATE TABLE `version` (
    `version` VARCHAR(20) PRIMARY KEY
);

CREATE TABLE `lent` (
    `user` int NOT NULL,
    `asset` int NOT NULL PRIMARY KEY,
    KEY(`user`),
);

CREATE TABLE `log` (
    `time` TIMESTAMP NOT,
    `msg` text NOT NULL,
    KEY(`time`) (`time`)
);