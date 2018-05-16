
use hockey;

update scoring
set goals = round( goals * round((82/70),4) )
where playerid in 
   ( select playerid
     from   players
     where  lastname = 'Howe'
     and    firstname = 'Gordie'
);

update scoring
set goals = round( goals * round((82/76),4) )
where playerid in 
   ( select playerid
     from   players
     where  lastname = 'Hull'
     and    firstname = 'Bobby'
);

