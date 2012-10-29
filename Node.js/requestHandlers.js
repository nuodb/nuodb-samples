/*
    Copyright (c) 2012, NuoDB, Inc.
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
    * Neither the name of NuoDB, Inc. nor the names of its contributors may
    be used to endorse or promote products derived from this software
    without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL NUODB, INC. BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
    LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
    OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
    OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
    ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
var querystring = require("querystring");

var sql = {}
sql["Insert"] = doInsert;
sql["Update"] = doUpdate;
sql["Delete"] = doDelete;

var responseStart = 
    '<html>'+
        '<head>'+
            '<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />'+
        '</head>'+
        '<body>';

var responseEnd =
        '</body>'+
    '</html>';

// show all the data in the hockey table
function start(response, postData, dbPool) {
    console.log("Request handler 'start' was called.");
    response.writeHead(200, {"Content-Type": "text/html; charset=UTF-8"});

    dbPool.acquire(function(err, db) {
        if (err) {
            return res.end("CONNECTION error: " + err);
        }

        db.query()
            .select(["NUMBER", "NAME", "POSITION"])
            .from("hockey")
            .execute(function(error, rows, columns){
                dbPool.release(db);
                if (error) {
                    console.log('ERROR: ' + error);
                    response.write(responseStart + '<p>' + error + '</p>' + responseEnd);
                    response.end();
                    return;
                }
                // Go through result set
                var body = '<form action="/upload" method="post">'+
                             '<table border="1"><tr><td>Number</td><td>Name</td><td>Position</td></tr>';
                for (i = 0; i < rows.length; i++) {
                    body += '<tr>'+
                        '<td>'+rows[i].NUMBER+'</td>'+
                        '<td>'+rows[i].NAME+'</td>'+
                        '<td>'+rows[i].POSITION+'</td>'+
                        '<td><input type="radio" name="selected" value="select_'+rows[i].NUMBER+'"/></td>'+
                        '</tr>';
                }
                body += '</table><br/>'+
                    'Number: <input name="number" type="text"></input>'+
                    'Name: <input name="name" type="text"/>'+
                    'Position: <input name="position" type="text"/>'+
                    '<input type="submit" name="submit_action" value="Insert" />/'+
                    '<input type="submit" name="submit_action" value="Update" /><br/>'+
                    'Choose radio button and <input type="submit" name="submit_action" value="Delete" />'+
                  '</form>';
    
                response.write(responseStart + body + responseEnd);
                response.end();
    
                console.log(rows);
          });
    });
}

// handle the POST action (insert/update/delete)
function upload(response, postData, dbPool) {
    console.log("Request handler 'upload' was called.");
  
    var queryData = querystring.parse(postData);

    var body = 
        '<p>You\'ve sent the number: ' + queryData.number + '</p>'+
        '<p>You\'ve sent the name: ' + queryData.name + '</p>'+
        '<p>You\'ve sent the position: ' + queryData.position + '</p>'+
        '<p>You\'ve selected: ' + queryData.selected + '</p>'+
        '<p>You\'ve requested the action: ' + queryData.submit_action + '</p>';

    console.log(body);

    // dispatch based on the action
    sql[queryData.submit_action](response, postData, dbPool);

}

function doInsert(response, postData, dbPool) {

    var queryData = querystring.parse(postData);

    dbPool.acquire(function(err, db) {
        if (err) {
            return res.end("CONNECTION error: " + err);
        }

        db.query()
            .insert("hockey", ["NUMBER", "NAME", "POSITION", "TEAM"], [queryData.number, queryData.name, queryData.position, "Bruins"], true)
            .execute(function(error, result) {
                dbPool.release(db);
                if (error) {
                    console.log('ERROR: ' + error);
                    response.write(responseStart + '<p>' + error + '</p>' + responseEnd);
                    response.end();
                    return;
                }
                // insert was good
                console.log("Insert succeeded");
                start(response, postData, dbPool);
        });     
    });
}

function doUpdate(response, postData, dbPool) {
    var queryData = querystring.parse(postData);

    dbPool.acquire(function(err, db) {
        if (err) {
          return res.end("CONNECTION error: " + err);
        }
        db.query()
          .update('hockey')
          .set({ 'NAME': queryData.name, 'POSITION': queryData.position })
          .where('NUMBER = ?', [queryData.number])
          .execute(function(error, result) {
              dbPool.release(db);
              if (error) {
                  console.log('ERROR: ' + error);
                  response.write(responseStart + '<p>' + error + '</p>' + responseEnd);
                  response.end();
                  return;
              }
              // update was good
              start(response, postData, dbPool);
        });     
    });
}

function doDelete(response, postData, dbPool) {
    var queryData = querystring.parse(postData);

    dbPool.acquire(function(err, db) {
        if (err) {
            return res.end("CONNECTION error: " + err);
        }
        db.query()
          .delete()
          .from('hockey')
          .where('NUMBER = ?', [queryData.selected.substring(7)])
          .execute(function(error, result) {
              dbPool.release(db);
              console.log("Done delete");
              if (error) {
                  console.log('ERROR: ' + error);
                  response.write(responseStart + '<p>' + error + '</p>' + responseEnd);
                  response.end();
                  return;
              }
              // delete was good
              start(response, postData, dbPool);
        });     
    });
}

exports.start = start;
exports.upload = upload;

