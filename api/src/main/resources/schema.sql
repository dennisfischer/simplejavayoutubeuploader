CREATE TABLE IF NOT EXISTS categories (
  youtubeId INTEGER NOT NULL PRIMARY KEY,
  name      TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts
(
  youtubeId    TEXT PRIMARY KEY NOT NULL,
  name         TEXT             NOT NULL,
  email        TEXT             NOT NULL,
  refreshToken TEXT,
  type         INTEGER          NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts_fields
(
  accountId     TEXT      NOT NULL,
  name          TEXT      NOT NULL,
  last_modified TIMESTAMP NOT NULL,
  PRIMARY KEY (accountId, name),
  FOREIGN KEY (accountId) REFERENCES accounts (youtubeId) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS accounts_cookies
(
  accountId     TEXT      NOT NULL,
  name          TEXT      NOT NULL,
  value         TEXT      NOT NULL,
  domain        TEXT      NOT NULL,
  discard       BOOLEAN   NOT NULL,
  path          TEXT      NOT NULL,
  maxAge        INTEGER   NOT NULL,
  secure        BOOLEAN   NOT NULL,
  version       INTEGER   NOT NULL,
  last_modified TIMESTAMP NOT NULL,
  PRIMARY KEY (accountId, name),
  FOREIGN KEY (accountId) REFERENCES accounts (youtubeId) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS playlists
(
  youtubeId     TEXT      NOT NULL,
  title         TEXT      NOT NULL,
  thumbnail     TEXT,
  privacyStatus BOOLEAN   NOT NULL,
  itemCount     INTEGER   NOT NULL,
  description   TEXT      NOT NULL,
  accountId     TEXT      NOT NULL,
  last_modified TIMESTAMP NOT NULL,
  PRIMARY KEY (youtubeId),
  FOREIGN KEY (accountId) REFERENCES accounts (youtubeId) ON DELETE CASCADE ON UPDATE CASCADE
);