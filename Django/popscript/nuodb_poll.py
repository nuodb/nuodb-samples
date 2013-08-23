import pynuodb

con = pynuodb.connect("Thousand", "localhost", "dba", "goalie", options = {"schema":"user"})
con.auto_commit = True

cursor = con.cursor()
# Insert 10 million rows
for i in range(1, 5001):
    test_batch = []
    for j in xrange(0, 2000):
        test_batch.append([i * j, str(i * j) + "?", "now"])
        cursor.executemany("insert into user.polls_poll values (?, ?, ?)", test_batch)

