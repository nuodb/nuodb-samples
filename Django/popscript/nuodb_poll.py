import pynuodb

class PynuodbPopScript():
    con = None
    cursor = None

    def _connect(self):
        host = "localhost:48004" 
        return pynuodb.connect("test", host, "dba", "goalie", options = {"schema":"user"})
    
    def populate(self):
        self.con = self._connect()
        self.cursor = self.con.cursor()
        for i in xrange(0, 1000):
            test_vals = (i, str(i) + "?", "now")
            self.cursor.execute("insert into polls_poll values (?, ?, ?)", tuple(test_vals))
          
if __name__ == '__main__':
    PynuodbPopScript.populate()    