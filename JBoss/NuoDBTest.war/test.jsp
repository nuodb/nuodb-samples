<%@ taglib uri="http://java.sun.com/jsp/jstl/sql" prefix="sql" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<sql:setDataSource dataSource="jdbc/NuoDBHockey" /> 
<sql:query var="rs">
select id, number, name, position from hockey
</sql:query>

<html>
  <head>
    <title>DB Test</title>
  </head>
  <body>

  <h2>Results</h2>
  
  <table border="1">
  <tr><td>Number</td><td>Name</td><td>Position</td></tr>
<c:forEach var="row" items="${rs.rows}">
  <tr>
    <td>${row.number}</td>
    <td>${row.name}</td>
    <td>${row.position}</td>
  </tr>
</c:forEach>
  </table>

  </body>
</html>