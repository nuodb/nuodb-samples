
CREATE TABLE IF NOT EXISTS usertable (
        YCSB_KEY VARCHAR(255),
        FIELD0 string, FIELD1 string,
        FIELD2 char(200), FIELD3 char(200),
        FIELD4 varchar(200), FIELD5 varchar(200),
        FIELD6 TEXT, FIELD7 TEXT,
        FIELD8 TEXT, FIELD9 TEXT
);

CREATE INDEX idx_usertable_ycsb_key ON usertable(ycsb_key);
