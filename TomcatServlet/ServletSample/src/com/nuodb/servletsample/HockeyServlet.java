package com.nuodb.servletsample;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 * Servlet implementation class HockeyServlet
 */
@WebServlet("/HockeyServlet")
public class HockeyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public HockeyServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Context envContext;
		Connection con = null;
		try {
			envContext = new InitialContext();
			Context initContext  = (Context)envContext.lookup("java:/comp/env");
            DataSource ds = (DataSource)initContext.lookup("jdbc/nuoDB");
            con = ds.getConnection();
            Statement stmt = con.createStatement();
            String query = "select * from hockey";
            ResultSet rs = stmt.executeQuery(query);
 
            PrintWriter out = response.getWriter();
            response.setContentType("text/html");
            out.print("<center><h1>Hockey Players Details</h1></center>");
            out.print("<html><body><table border=\"1\" cellspacing=10 cellpadding=5>");
            out.print("<th>Number</th>");
            out.print("<th>Name</th>");
            out.print("<th>Team</th>");
            out.print("<th>Position</th>");
 
            while(rs.next())
            {
                out.print("<tr>");
                out.print("<td>" + rs.getInt("number") + "</td>");
                out.print("<td>" + rs.getString("name") + "</td>");
                out.print("<td>" + rs.getString("team") + "</td>");
                out.print("<td>" + rs.getString("position") + "</td>");
                out.print("</tr>");
            }
            out.print("</table></body></html>");

 		} catch(SQLException sqle) {
			sqle.printStackTrace();			
 		} catch (NamingException ne) {
			ne.printStackTrace();
		} finally {
			if(con != null)
				try {
					con.close();
				} catch (SQLException sqle) {
					sqle.printStackTrace();
				}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
