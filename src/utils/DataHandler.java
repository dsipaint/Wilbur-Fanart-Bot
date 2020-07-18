package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataHandler
{
	public static Connection getConnection()
	{
		Connection con = null;
		
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			String usr = "mcph804597";
			String pwd = "548c7d482a";
			String ip = "66.85.144.162";
			String port = "3306";
			String databasename = "mcph804597";
			String url = "jdbc:mysql://" + ip + ":" + port + "/" + databasename;
			
			con = DriverManager.getConnection(url, usr, pwd);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return con;
	}
	
	public static int getResultSize(String query) throws SQLException
	{
		ResultSet rs = getConnection().createStatement().executeQuery(query);
		int count = 0;
		
		while(rs.next())
			count++;
		
		return count;
	}
	
	public static String getArtByIndex(int index, String userID)
	{	
		try
		{
			ResultSet rs = getConnection().createStatement().executeQuery("select art from Wilbur_approvedart "
					+ "where discord_id = \"" + userID + "\"");
			
			rs.absolute(index);
			return rs.getString("art");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static boolean isImageUrl(String url)
	{
		if(url.matches("http(s?):\\/\\/.*(\\.png|\\.jpeg|\\.jpg|\\.JPG|\\.PNG)"))
			return true;
		
		return false;
	}
}
