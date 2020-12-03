-- Spring Boot automatically runs the SQL in this file
--
-- We could get Hibernate to do this for us, but often you want more control.
-- Your choice.

DROP TABLE Accounts IF EXISTS;
CREATE TABLE Accounts (id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, name STRING);
ALTER TABLE Accounts ADD COLUMN balance INT;
