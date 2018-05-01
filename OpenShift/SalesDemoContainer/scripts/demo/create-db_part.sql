
use hockey;

DROP TABLE IF EXISTS Hockey;
DROP TABLE IF EXISTS vw_player_stats;
DROP TABLE IF EXISTS scoring;
DROP TABLE IF EXISTS teams;
DROP TABLE IF EXISTS players;

create table Hockey
(
   Id       BIGINT not NULL generated always as identity primary key,
   Number   Integer,
   Name     String,
   Position String,
   Team     String
);
create unique index player_idx on Hockey (Number, Name, Team);

insert into Hockey (Number,Name,Position,Team)  values (37,'PATRICE BERGERON','Forward','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (48,'CHRIS BOURQUE','Forward','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (11,'GREGORY CAMPBELL','Forward','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (18,'NATHAN HORTON','Forward','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (23,'CHRIS KELLY','Forward','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (46,'DAVID KREJCI','Forward','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (17,'MILAN LUCIC','Forward','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (64,'LANE MACDERMID','Forward','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (63,'BRAD MARCHAND','Forward','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (20,'DANIEL PAILLE','Forward','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (49,'RICH PEVERLEY','Forward','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (91,'MARC SAVARD','Forward','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (19,'TYLER SEGUIN','Forward','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (22,'SHAWN THORNTON','Forward','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (55,'JOHNNY BOYCHUK','Defense','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (33,'ZDENO CHARA','Defense','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (21,'ANDREW FERENCE','Defense','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (27,'DOUGIE HAMILTON','Defense','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (45,'AARON JOHNSON','Defense','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (54,'ADAM MCQUAID','Defense','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (44,'DENNIS SEIDENBERG','Defense','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (35,'ANTON KHUDOBIN','Goalie','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (40,'TUUKKA RASK','Goalie','Bruins');
insert into Hockey (Number,Name,Position,Team)  values (1,'MAX SUMMIT','Fan','Bruins');



CREATE TABLE players(
  playerID varchar(10) NOT NULL,
  firstName varchar(32),
  lastName varchar(32),
  height integer,
  weight integer,
  firstNHL integer NOT NULL DEFAULT 0,
  lastNHL integer NOT NULL DEFAULT 0,
  position varchar(8),
  birthYear integer,
  birthMon integer,
  birthDay integer,
  birthCountry varchar(32),
  birthState varchar(32),
  birthCity varchar(32),
  PRIMARY KEY (playerID)
);


CREATE TABLE teams
(
  year integer NOT NULL,
  teamID varchar(3) NOT NULL,
  name varchar(48) NOT NULL,
  conferenceID varchar(2) NOT NULL DEFAULT '',
  divisionID varchar(2) NOT NULL DEFAULT '',
  rank integer NOT NULL,
  playoff varchar(6) NOT NULL DEFAULT '',
  games integer NOT NULL,
  wins integer NOT NULL DEFAULT 0,
  losses integer NOT NULL DEFAULT 0,
  ties integer NOT NULL DEFAULT 0,
  overtimeLosses integer,
  PRIMARY KEY (year , teamID )
);


UNMAP PARTITION p01 if exists;
UNMAP PARTITION p02 if exists;
UNMAP PARTITION p03 if exists;
UNMAP PARTITION p04 if exists;
UNMAP PARTITION p05 if exists;
UNMAP PARTITION p06 if exists;
UNMAP PARTITION p07 if exists;


MAP PARTITION p01 STORE IN sg1;
MAP PARTITION p02 STORE IN sg1;
MAP PARTITION p03 STORE IN sg1;
MAP PARTITION p04 STORE IN sg1;

MAP PARTITION p05 STORE IN sg2;
MAP PARTITION p06 STORE IN sg2;
MAP PARTITION p07 STORE IN sg2;


CREATE TABLE scoring
(
  playerID varchar(10) NOT NULL REFERENCES players(playerid),
  year integer NOT NULL,
  stint integer NOT NULL,
  teamID varchar(3) NOT NULL,
  position varchar(4) NOT NULL,
  gamesPlayed integer NOT NULL DEFAULT 0,
  goals integer NOT NULL DEFAULT 0,
  assists integer NOT NULL DEFAULT 0,
  penaltyMinutes integer NOT NULL DEFAULT 0,
  PRIMARY KEY (playerID , year , stint , teamID , position ),
  FOREIGN KEY (year, teamID) REFERENCES teams(year, teamID))
partition BY range (year)
    partition p01 values less than (1920),
    partition p02 values less than (1940),
    partition p03 values less than (1960),
    partition p04 values less than (1980),
    partition p05 values less than (2000),
    partition p06 values less than (2020),
    partition p07 values less than (MAXVALUE)
);


create view vw_player_stats as 
select p.playerid, p.lastname||','||p.firstname as playername,s.year,s.stint,s.position,
       s.gamesplayed,s.goals,s.assists,s.penaltyminutes 
from players p
 join scoring s on s.playerid = p.playerid;


