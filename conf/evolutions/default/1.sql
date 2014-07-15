# --- !Ups

CREATE TABLE Gifts (
  code VARCHAR(255) NOT NULL,
  amount INT NOT NULL,
  message TEXT,
  creationMoment DATETIME NOT NULL,
  isLottery BOOLEAN NOT NULL,
  confirmed BOOLEAN NOT NULL DEFAULT 0,
  nbrTickets INT NOT NULL DEFAULT 0,
  nbrPacks INT NOT NULL DEFAULT 0,
  PRIMARY KEY (code)
);

CREATE TABLE ContributorInfo(
  giftCode VARCHAR(255) NOT NULL,
  lastName VARCHAR(255),
  firstName VARCHAR(255),
  emailAddress VARCHAR(255),
  PRIMARY KEY (giftCode),
  CONSTRAINT fk_gci FOREIGN KEY (giftCode) REFERENCES Gifts(code)
);

CREATE TABLE PrizeDefinitions (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  imagePath VARCHAR(255) NOT NULL,
  pdfPath VARCHAR(255) NOT NULL,
  isWinning BOOLEAN NOT NULL,
  probability INT,
  quantity INT,
  remainingQuantity INT,
  PRIMARY KEY (id)
);

CREATE TABLE Tickets (
  code VARCHAR(255) NOT NULL,
  revealed BOOLEAN NOT NULL DEFAULT 0,
  giftCode VARCHAR(255) NOT NULL,
  prizeDefinitionId BIGINT(20) NOT NULL,
  PRIMARY KEY (code),
  CONSTRAINT fk_tg FOREIGN KEY (giftCode) REFERENCES Gifts(code),
  CONSTRAINT fk_tpd FOREIGN KEY (prizeDefinitionId) REFERENCES PrizeDefinitions(id)
);

# --- !Downs

DROP TABLE Tickets;
DROP TABLE PrizeDefinitions;
DROP TABLE ContributorInfo;
DROP TABLE Gifts;
