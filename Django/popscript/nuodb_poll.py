import pynuodb

con = pynuodb.connect("Thousand", "localhost", "dba", "goalie", options = {"schema":"user"})
con.auto_commit = True

cursor = con.cursor()
# Insert 10 million rows
for i in xrange(0, 5000):
    test_batch = []
    for j in xrange(0, 2000):
        id = (i * 2000) + j
        test_batch.append([id, str(id) + "?", "now"])
        cursor.executemany("insert into user.polls_poll values (?, ?, ?)", test_batch)

