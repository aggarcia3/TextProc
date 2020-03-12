-- ----------------------------------------- --
--	Example DDL statements for TextProc  --
-- Tweak as needed for your domain and RDBMS --
-- ----------------------------------------- --
BEGIN TRANSACTION;

-- DROP objects created by this script
DROP VIEW tokenized_submission;
DROP VIEW tokenized_comment;
DROP VIEW stopword_filtered_submission;
DROP VIEW stopword_filtered_comment;
DROP VIEW lemmatized_submission;
DROP VIEW lemmatized_comment;
DROP VIEW mention_filtered_submission;
DROP VIEW mention_filtered_comment;
DROP VIEW non_empty_submission;
DROP VIEW non_empty_comment;
DROP TABLE tokenized_text_with_title_document;
DROP TABLE tokenized_text_document;
DROP TABLE stopword_filtered_text_with_title_document;
DROP TABLE stopword_filtered_text_document;
DROP TABLE lemmatized_text_with_title_document;
DROP TABLE lemmatized_text_document;
DROP TABLE mention_filtered_text_with_title_document;
DROP TABLE mention_filtered_text_document;
DROP TABLE non_empty_text_with_title_document;
DROP TABLE non_empty_text_document;

-- CREATE JPA entity tables
-- (you may need to modify the foreign key constraint)
CREATE TABLE tokenized_text_with_title_document (
	id INT PRIMARY KEY,
	text TEXT NOT NULL,
	title TEXT NOT NULL,
	FOREIGN KEY (id) REFERENCES submission(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE tokenized_text_document (
	id INT PRIMARY KEY,
	text TEXT NOT NULL,
	FOREIGN KEY (id) REFERENCES comment(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE stopword_filtered_text_with_title_document (
	id INT PRIMARY KEY,
	text TEXT NOT NULL,
	title TEXT NOT NULL,
	FOREIGN KEY (id) REFERENCES submission(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE stopword_filtered_text_document (
	id INT PRIMARY KEY,
	text TEXT NOT NULL,
	FOREIGN KEY (id) REFERENCES comment(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE lemmatized_text_with_title_document (
	id INT PRIMARY KEY,
	text TEXT NOT NULL,
	title TEXT NOT NULL,
	FOREIGN KEY (id) REFERENCES submission(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE lemmatized_text_document (
	id INT PRIMARY KEY,
	text TEXT NOT NULL,
	FOREIGN KEY (id) REFERENCES comment(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE mention_filtered_text_with_title_document (
	id INT PRIMARY KEY,
	text TEXT NOT NULL,
	title TEXT NOT NULL,
	FOREIGN KEY (id) REFERENCES submission(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE mention_filtered_text_document (
	id INT PRIMARY KEY,
	text TEXT NOT NULL,
	FOREIGN KEY (id) REFERENCES comment(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE non_empty_text_with_title_document (
	id INT PRIMARY KEY,
	text TEXT NOT NULL,
	title TEXT NOT NULL,
	FOREIGN KEY (id) REFERENCES submission(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE non_empty_text_document (
	id INT PRIMARY KEY,
	text TEXT NOT NULL,
	FOREIGN KEY (id) REFERENCES comment(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- CREATE views to provide more semantic names
-- to JPA entity tables (optional)
CREATE VIEW tokenized_submission AS SELECT * FROM tokenized_text_with_title_document;
CREATE VIEW tokenized_comment AS SELECT * FROM tokenized_text_document;
CREATE VIEW stopword_filtered_submission AS SELECT * FROM stopword_filtered_text_with_title_document;
CREATE VIEW stopword_filtered_comment AS SELECT * FROM stopword_filtered_text_document;
CREATE VIEW lemmatized_submission AS SELECT * FROM lemmatized_text_with_title_document;
CREATE VIEW lemmatized_comment AS SELECT * FROM lemmatized_text_document;
CREATE VIEW mention_filtered_submission AS SELECT * FROM mention_filtered_text_with_title_document;
CREATE VIEW mention_filtered_comment AS SELECT * FROM mention_filtered_text_document;
CREATE VIEW non_empty_submission AS SELECT * FROM non_empty_text_with_title_document;
CREATE VIEW non_empty_comment AS SELECT * FROM non_empty_text_document;

COMMIT;
