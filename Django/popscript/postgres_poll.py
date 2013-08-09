import psycopg2

con = psycopg2.connect("dbname=this user=postgres")

cursor = con.cursor()
for i in xrange(0, 1000):
    cursor.execute("insert into polls_poll values (%d, %s, %s)", 
                   (i, str(i) + "?", "now"))
con.commit()
cursor.close()
con.close() 