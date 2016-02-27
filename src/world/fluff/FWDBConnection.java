package world.fluff;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.ResultSet;

public class FWDBConnection {
	private Statement stmt = null;
    private ResultSet rs = null;
    private Connection connection = null;
    private String dbpass;
    private String dbuser;
	public FWDBConnection()
	{
		try{
			Scanner in = new Scanner(new FileReader("db.txt"));
			if(in.hasNextLine())
			{
				dbuser = in.nextLine();
			}
			if(in.hasNextLine())
			{
				dbpass = in.nextLine();
			}
			in.close();
		}
		catch(Exception ex)
		{
			System.out.println("db.txt not found.");
		}
		try { //We use a try catch to avoid errors, hopefully we don't get any.
            Class.forName("com.mysql.jdbc.Driver").newInstance(); //this accesses Driver in jdbc.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	try {
    		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/minecraft_mod?autoReconnect=true", dbuser, dbpass);

    	} catch (SQLException ex) {
    	    // handle any errors
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("VendorError: " + ex.getErrorCode());
    	}
	}
	
	public int addBlockBreakStat(String name)
	{
		try
	    {
	    	stmt = connection.createStatement();
	    	int rowCount = stmt.executeUpdate("UPDATE fwmc_points SET blocks_broken = blocks_broken + 1 WHERE name = '" + name + "'");
	    	if(rowCount > 0)
	    	{
	    		return rowCount;
	    	}
	    }
	    catch(SQLException ex)
	    {
	    	 System.out.println("SQLException: " + ex.getMessage());
	    	 System.out.println("SQLState: " + ex.getSQLState());
	    	 System.out.println("VendorError: " + ex.getErrorCode());
	    }
		return 0;
	}
	
	public int addBlockPlaceStat(String name)
	{
		try
	    {
	    	stmt = connection.createStatement();
	    	int rowCount = stmt.executeUpdate("UPDATE fwmc_points SET blocks_placed = blocks_placed + 1 WHERE name = '" + name + "'");
	    	if(rowCount > 0)
	    	{
	    		return rowCount;
	    	}
	    }
	    catch(SQLException ex)
	    {
	    	 System.out.println("SQLException: " + ex.getMessage());
	    	 System.out.println("SQLState: " + ex.getSQLState());
	    	 System.out.println("VendorError: " + ex.getErrorCode());
	    }
		return 0;
	}
	
	public int addXp(String name, int xp)
	{
		try
	    {
	    	stmt = connection.createStatement();
	    	int rowCount = stmt.executeUpdate("UPDATE fwmc_points SET lifetime_xp = lifetime_xp + " + xp + " WHERE name = '" + name + "'");
	    	if(rowCount > 0)
	    	{
	    		return rowCount;
	    	}
	    }
	    catch(SQLException ex)
	    {
	    	 System.out.println("SQLException: " + ex.getMessage());
	    	 System.out.println("SQLState: " + ex.getSQLState());
	    	 System.out.println("VendorError: " + ex.getErrorCode());
	    }
		return 0;
	}
	public int removeXp(String name, int xp)
	{
		try
	    {
	    	stmt = connection.createStatement();
	    	int rowCount = stmt.executeUpdate("UPDATE fwmc_points SET lifetime_xp = lifetime_xp - " + xp + " WHERE name = '" + name + "'");
	    	if(rowCount > 0)
	    	{
	    		return rowCount;
	    	}
	    }
	    catch(SQLException ex)
	    {
	    	 System.out.println("SQLException: " + ex.getMessage());
	    	 System.out.println("SQLState: " + ex.getSQLState());
	    	 System.out.println("VendorError: " + ex.getErrorCode());
	    }
		return 0;
	}
	
	public int changeChatColor(String name, String color)
	{
		try
	    {
	    	stmt = connection.createStatement();
	    	int rowCount = stmt.executeUpdate("UPDATE fwmc_points SET color = '" + color + "' WHERE name = '" + name + "'");
	    	if(rowCount > 0)
	    	{
	    		return rowCount;
	    	}
	    }
	    catch(SQLException ex)
	    {
	    	 System.out.println("SQLException: " + ex.getMessage());
	    	 System.out.println("SQLState: " + ex.getSQLState());
	    	 System.out.println("VendorError: " + ex.getErrorCode());
	    }
		return 0;
	}
	
	//fetches dummy value to make sure the connection is live
	public boolean checkConnection()
	{
		try
	    {
	    	stmt = connection.createStatement();
	    	rs = stmt.executeQuery("SELECT online FROM dummy WHERE online = 1");
	    	if(!rs.next())
	    	{
	    		return false;
	    	}
	    	else
	    	{
	    		if(rs.getInt(1) == 1)
	    		{
	    			return true;
	    		}
	    		else
	    		{
	    			return false;
	    		}
	    	}
	    }
	    catch(SQLException ex)
	    {
	    	 System.out.println("SQLException: " + ex.getMessage());
	    	 System.out.println("SQLState: " + ex.getSQLState());
	    	 System.out.println("VendorError: " + ex.getErrorCode());
	    	 return false;
	    }
	}
	
	public boolean checkIfNameInDb(String name)
	{
		rs = runSelectQuery("SELECT points FROM fwmc_points WHERE name = '" + name + "'");
		try {
			if(!rs.next())
			{
				return false;
			}
			else
			{
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public ChatColor getChatColor(String name)
	{
		ResultSet rsl = runSelectQuery("SELECT color FROM fwmc_points WHERE name = '" + name + "'");
		try {
			rsl.next();
			return ChatColor.valueOf(rsl.getString(1));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ChatColor.WHITE;
	}
	
	public int getPlayerPoints(String name)
	{
		if(!checkIfNameInDb(name))
		{
			insertNewUser(name);
		}
		ResultSet rsl = runSelectQuery("SELECT points FROM fwmc_points WHERE name = '" + name + "'");
		try {
			rsl.next();
			return rsl.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public int givePlayerPoints(String name, int points)
	{
		return givePlayerPoints(name, points, false);
	}
	
	public int givePlayerPoints(String name, int points, Boolean message)
	{
		if(points < 1)
		{
			return 0;
		}
		try
	    {
	    	stmt = connection.createStatement();
	    	int rowCount = stmt.executeUpdate("UPDATE fwmc_points SET points = points + " + points + ", pending_points = pending_points + " + points + " WHERE name = '" + name + "'");
	    	if(rowCount > 0)
	    	{
	    		if(message)
	    		{
	    			String point_string = " points.";
	    			if(points == 1)
	    			{
	    				point_string = " point.";
	    			}
	    			Bukkit.broadcastMessage(getChatColor(name) + name + ChatColor.RESET +  " just earned " + points + point_string);
	    		}
	    		return rowCount;
	    	}
	    }
	    catch(SQLException ex)
	    {
	    	 System.out.println("SQLException: " + ex.getMessage());
	    	 System.out.println("SQLState: " + ex.getSQLState());
	    	 System.out.println("VendorError: " + ex.getErrorCode());
	    }
		return 0;
	}
	
	public void insertNewUser(String name)
	{
		try
	    {
	    	stmt = connection.createStatement();
	    	/*int rowCount = */stmt.executeUpdate("INSERT INTO fwmc_points(name, fw_username, name_from_mc, passphrase, color, points, pending_points, blocks_broken, blocks_placed, lifetime_xp)VALUES('" + name + "', '', '', '', 'WHITE', 0, 0, 0, 0, 0)");
	    	Bukkit.broadcastMessage("Inserted new user '" + name + "' into database.");
	    	return;
	    }
	    catch(SQLException ex)
	    {
	    	 System.out.println("SQLException: " + ex.getMessage());
	    	 System.out.println("SQLState: " + ex.getSQLState());
	    	 System.out.println("VendorError: " + ex.getErrorCode());
	    }
	}
	
	public int registerFWMC(String name, String fwName)
	{
		try
	    {
	    	stmt = connection.createStatement();
	    	int rowCount = stmt.executeUpdate("UPDATE fwmc_points SET name_from_mc = '" + fwName + "' WHERE name = '" + name + "' AND fw_username = ''");
	    	if(rowCount > 0)
	    	{
	    		return rowCount;
	    	}
	    	else
	    	{
	    		return -1;
	    	}
	    }
	    catch(SQLException ex)
	    {
	    	 System.out.println("SQLException: " + ex.getMessage());
	    	 System.out.println("SQLState: " + ex.getSQLState());
	    	 System.out.println("VendorError: " + ex.getErrorCode());
	    }
		return 0;
	}
	
	public ResultSet runSelectQuery(String q)
	{
		try
	    {
	    	stmt = connection.createStatement();
	    	rs = stmt.executeQuery(q);
	    	return rs;
	    }
	    catch(SQLException ex)
	    {
	    	 System.out.println("SQLException: " + ex.getMessage());
	    	 System.out.println("SQLState: " + ex.getSQLState());
	    	 System.out.println("VendorError: " + ex.getErrorCode());
	    }
	    // Do something with the Connection
		return null;
	}
	
	public int subtractPlayerPoints(String name, int points)
	{
		return subtractPlayerPoints(name, points, false);
	}
	
	public int subtractPlayerPoints(String name, int points, Boolean message)
	{
		if(points < 1)
		{
			return 0;
		}
		try
	    {
	    	stmt = connection.createStatement();
	    	int rowCount = stmt.executeUpdate("UPDATE fwmc_points SET points = points - " + points + ", pending_points = pending_points - " + points + " WHERE name = '" + name + "'");
	    	if(rowCount > 0)
	    	{
	    		if(message)
	    		{
	    			Bukkit.broadcastMessage(getChatColor(name) + name + ChatColor.RESET + " just lost " + points + " points.");
	    		}
	    		return rowCount;
	    	}
	    }
	    catch(SQLException ex)
	    {
	    	 System.out.println("SQLException: " + ex.getMessage());
	    	 System.out.println("SQLState: " + ex.getSQLState());
	    	 System.out.println("VendorError: " + ex.getErrorCode());
	    }
		return 0;
	}
	
	public void kill()
	{
		if (rs != null) {
            try {
                rs.close();
            } catch (SQLException sqlEx) { } // ignore

            rs = null;
        }

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException sqlEx) { } // ignore

            stmt = null;
        }

	}
}
