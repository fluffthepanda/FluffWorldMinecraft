package world.fluff;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FluffWorldIntegration extends JavaPlugin {
	private FWDBConnection fwdb;
	private FluffsScoreboard fsb;
    //Connection vars
    //static Connection connection; //This is the variable we will use to connect to database
	// Fired when plugin is first enabled
    @Override
    public void onEnable() {
    	fwdb = new FWDBConnection();
    	fsb = new FluffsScoreboard(Bukkit.getScoreboardManager().getNewScoreboard(), fwdb);
    	getServer().getPluginManager().registerEvents(new FluffBlockDropListener(fwdb, fsb), this);
    }
    // Fired when plugin is disabled
    @Override
    public void onDisable() {
    	fwdb.kill();
    	fsb.kill();
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("points"))
        {
        	
        	if(args.length == 0)
        	{
        		int p = fwdb.getPlayerPoints(sender.getName());
                sender.sendMessage("You have " + p + " FWMC points.");
                return true;
        	}
        	else
        	{        		
        		if(fwdb.checkIfNameInDb(args[0]))
        		{
        			//the requested user is in the database
        			int p = fwdb.getPlayerPoints(args[0]);
                    sender.sendMessage("Player "+ fwdb.getChatColor(args[0])+args[0]+ChatColor.RESET+ " has " + p + " FWMC points.");
                    return true;
        		}
        		else 
        		{
        			//the requested user is NOT in the database
        			sender.sendMessage("Player "+args[0]+" is not in the database.");
        			return false;
        		}
        	}
        	
        }
        else if(command.getName().equalsIgnoreCase("chatcolor"))
        {
        	if(args.length == 0)
        	{
        		sender.sendMessage("You must specify a color! You can use:");
        		sender.sendMessage(ChatColor.BLACK + "black" + ChatColor.DARK_BLUE + " dark_blue" + ChatColor.DARK_GREEN + " dark_green" + ChatColor.DARK_AQUA + " dark_aqua" + ChatColor.DARK_RED + " dark_red" + ChatColor.DARK_PURPLE + " dark_purple" + ChatColor.GOLD + " gold" + ChatColor.GRAY + " gray" + ChatColor.DARK_GRAY + " dark_gray" + ChatColor.BLUE + " blue" + ChatColor.GREEN + " green" + ChatColor.AQUA + " aqua" + ChatColor.RED + " red" + ChatColor.LIGHT_PURPLE + " light_purple" + ChatColor.YELLOW + " yellow" + ChatColor.WHITE + " white");
        	}
        	else
        	{
        		ChatColor color = null;
        		try{
        		color = ChatColor.valueOf(args[0].toUpperCase());
        		}
        		catch(Exception e)
        		{
        			color = null;
        		}
        		if(color != null)
        		{
        			/*try{
    					fsb.removePlayerFromSidebar(sender.getName());
    				}
    				catch(Exception ex){}
    				try{
    					fsb.removePlayerFromSidebar(fwdb.getChatColor(sender.getName()) + sender.getName());
    				}
    				catch(Exception ex){}*/
        			fsb.removePlayerFromSidebar(sender.getServer().getPlayer(sender.getName()).getDisplayName()); //Name should include current color formatting
        			if(fwdb.changeChatColor(sender.getName(), args[0].toUpperCase()) == 1)
        			{
        				sender.getServer().getPlayer(sender.getName()).setDisplayName(color + sender.getName() + ChatColor.RESET);
        				sender.getServer().getPlayer(sender.getName()).setPlayerListName(color + sender.getName() + ChatColor.RESET);
        				sender.sendMessage(color + "Color successfully changed.");
        			}
        			else
        			{
        				sender.sendMessage("An error occurred. Your color was not changed.");
        			}
        			return true;
        		}
        		else
        		{
        			sender.sendMessage("Invalid color! You can use:");
            		sender.sendMessage(ChatColor.BLACK + "black" + ChatColor.DARK_BLUE + " dark_blue" + ChatColor.DARK_GREEN + " dark_green" + ChatColor.DARK_AQUA + " dark_aqua" + ChatColor.DARK_RED + " dark_red" + ChatColor.DARK_PURPLE + " dark_purple" + ChatColor.GOLD + " gold" + ChatColor.GRAY + " gray" + ChatColor.DARK_GRAY + " dark_gray" + ChatColor.BLUE + " blue" + ChatColor.GREEN + " green" + ChatColor.AQUA + " aqua" + ChatColor.RED + " red" + ChatColor.LIGHT_PURPLE + " light_purple" + ChatColor.YELLOW + " yellow" + ChatColor.WHITE + " white");
        			return false;
        		}
        	}
        }
        else if(command.getName().equalsIgnoreCase("coords"))
        {
        	try{
        	Player p = sender.getServer().getPlayer(sender.getName());
        	Location loc = p.getLocation();
        	ChatColor color = fwdb.getChatColor(sender.getName());
        	Bukkit.broadcastMessage(color + sender.getName() + ChatColor.RESET + " X: " + loc.getBlockX() + "  Y: " + loc.getBlockY() + "  Z: " + loc.getBlockZ());
        	return true;
        	}
        	catch(Exception e)
        	{
        		System.out.println("Only online players can access this function.");
        		return true;
        	}
        }
        else if(command.getName().equalsIgnoreCase("register"))
        {
        	if(args.length == 0)
        	{
        		sender.sendMessage("You must enter your Fluff World username.");
        		return false;
        	}
        	else
        	{
        		String fwName = args[0];
        		String name = sender.getName();
        		int registered = fwdb.registerFWMC(name, fwName);
        		if(registered == 1)
        		{
        			sender.sendMessage("Updated, but your account isn't fully linked yet. Go to your MC settings on FW to continue.");
            		return true;
        		}
        		else if(registered == -1)
        		{
        			sender.sendMessage("The account you specified is already registered.");
            		return true;
        		}
        	}
        }
        return false;
    }
}
