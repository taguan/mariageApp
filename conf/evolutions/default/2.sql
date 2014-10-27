# --- !Ups

INSERT INTO PrizeDefinitions(`name`, isWinning, probability, quantity, remainingQuantity)
VALUES ('lost',  0, 50, 0, 0);

# --- !Downs

DELETE FROM PrizeDefinitions WHERE isWinning = 0;