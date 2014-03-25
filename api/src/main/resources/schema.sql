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

CREATE TABLE IF NOT EXISTS uploads
(
  id                TEXT    NOT NULL,
  uploadurl         TEXT,
  videoid           TEXT,
  FILE              TEXT    NOT NULL,
  enddir            TEXT,
  thumbnail         TEXT,
  dateTimeOfStart   TIMESTAMP,
  dateTimeOfRelease TIMESTAMP,
  dateTimeOfEnd     TIMESTAMP,
  "ORDER"           INTEGER NOT NULL,
  progress          REAL    NOT NULL,
  stopAfter         BOOLEAN NOT NULL,
  fileSize          INTEGER,
  status            TEXT    NOT NULL,
  accountId         TEXT,
  PRIMARY KEY (id),
  FOREIGN KEY (accountId) REFERENCES accounts (youtubeId) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS uploads_social
(
  uploadId TEXT    NOT NULL,
  message  TEXT    NOT NULL,
  facebook BOOLEAN NOT NULL,
  twitter  BOOLEAN NOT NULL,
  gplus    BOOLEAN NOT NULL,
  PRIMARY KEY (uploadId),
  FOREIGN KEY (uploadId) REFERENCES uploads (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS uploads_permission
(
  uploadId            TEXT    NOT NULL,
  visibility          TEXT    NOT NULL,
  threedD             TEXT    NOT NULL,
  comment             TEXT    NOT NULL,
  commentvote         BOOLEAN NOT NULL,
  embed               BOOLEAN NOT NULL,
  rate                BOOLEAN NOT NULL,
  ageRestricted       BOOLEAN NOT NULL,
  publicStatsViewable BOOLEAN NOT NULL,
  PRIMARY KEY (uploadId),
  FOREIGN KEY (uploadId) REFERENCES uploads (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS uploads_metadata
(
  uploadId    TEXT    NOT NULL,
  category    INTEGER NOT NULL,
  license     TEXT    NOT NULL,
  title       TEXT    NOT NULL,
  description TEXT    NOT NULL,
  tags        TEXT    NOT NULL,
  PRIMARY KEY (uploadId),
  FOREIGN KEY (uploadId) REFERENCES uploads (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS uploads_monetization
(
  uploadId         TEXT    NOT NULL,
  syndication      TEXT    NOT NULL,
  claimType        TEXT    NOT NULL,
  claimOption      TEXT    NOT NULL,
  asset            TEXT    NOT NULL,
  instreamDefaults BOOLEAN NOT NULL,
  claim            BOOLEAN NOT NULL,
  overlay          BOOLEAN NOT NULL,
  trueview         BOOLEAN NOT NULL,
  instream         BOOLEAN NOT NULL,
  product          BOOLEAN NOT NULL,
  partner          BOOLEAN NOT NULL,
  title            TEXT    NOT NULL,
  description      TEXT    NOT NULL,
  customId         TEXT    NOT NULL,
  notes            TEXT    NOT NULL,
  tmsid            TEXT    NOT NULL,
  isan             TEXT    NOT NULL,
  eidr             TEXT    NOT NULL,
  episodeTitle     TEXT    NOT NULL,
  seasonNumber     TEXT    NOT NULL,
  episodeNumber    TEXT    NOT NULL,
  PRIMARY KEY (uploadId),
  FOREIGN KEY (uploadId) REFERENCES uploads (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS uploads_playlists
(
  uploadId   TEXT NOT NULL,
  playlistId TEXT NOT NULL,

  PRIMARY KEY (uploadId, playlistId),
  FOREIGN KEY (uploadId) REFERENCES uploads (id) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (playlistId) REFERENCES playlists (id) ON DELETE CASCADE ON UPDATE CASCADE
);