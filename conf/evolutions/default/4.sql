# --- !Ups

CREATE TABLE Messages (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  message TEXT,
  PRIMARY KEY (id)
);


# --- !Downs

DROP TABLE Messages;