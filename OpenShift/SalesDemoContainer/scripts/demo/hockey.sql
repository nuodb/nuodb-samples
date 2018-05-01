
use hockey;

select /* top 10 goal scorers in single season */ 
  firstname, lastname, birthyear, s.year "YEAR PLAYED", t.name "TEAM", s.goals
from scoring s, players p, teams t
where s.playerid = p.playerid
and   s.year     = t.year
and   s.teamid   = t.teamid
order by s.goals desc
limit 10;

select /* top all-time cummulative goal scorers */
  firstname, lastname, birthyear,
  count(s.year) "YEARS PLAYED", round(avg(s.goals),2) "AVG GOALS/YR", sum(s.goals) "TOT GOALS"
from scoring s, players p, teams t
where s.playerid = p.playerid
and   s.year     = t.year
and   s.teamid   = t.teamid
group by firstname, lastname, birthyear
order by sum(s.goals) desc
limit 10;


