import pynuodb

con = pynuodb.connect("Thousand", "localhost", "dba", "goalie", options = {"schema":"user"})

cursor = con.cursor()
for i in xrange(0, 1000):
    test_vals = (i, str(i) + "?", "now")
    cursor.execute("insert into user.polls_poll values (?, ?, ?)", test_vals)
con.commit() 
