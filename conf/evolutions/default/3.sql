# --- !Ups

CREATE TABLE Confirmations (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  lastName VARCHAR(255) NOT NULL,
  firstName VARCHAR(255),
  isComing BOOLEAN NOT NULL,
  nbrComing INT NOT NULL DEFAULT 0,
  comment TEXT,
  PRIMARY KEY (id)
);


# --- !Downs

DROP TABLE Confirmations;