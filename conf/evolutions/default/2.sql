# --- !Ups

INSERT INTO PrizeDefinitions(`name`, imagePath, pdfPath, isWinning, probability, quantity, remainingQuantity)
VALUES ('lost', 'lost.jpg', 'lost.pdf', 0, 50, 0, 0);

# --- !Downs

DELETE FROM PrizeDefinitions WHERE isWinning = 0;