package world.fluff;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import me.dpohvar.powernbt.PowerNBT;
import me.dpohvar.powernbt.api.NBTCompound;
import me.dpohvar.powernbt.api.NBTList;
import me.dpohvar.powernbt.api.NBTManager;

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
        if(sender instanceof Player)
        {
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
            			try{
        					fsb.removePlayerFromSidebar(sender.getName());
        				}
        				catch(Exception ex){}
        				try{
        					fsb.removePlayerFromSidebar(fwdb.getChatColor(sender.getName()) + sender.getName());
        				}
        				catch(Exception ex){}
            			//fsb.removePlayerFromSidebar(sender.getServer().getPlayer(sender.getName()).getDisplayName()); //Name should include current color formatting
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
            else if(command.getName().equalsIgnoreCase("withdraw"))
            {
            	if(args.length == 0)
            	{
            		sender.sendMessage("You must provide an amount of points to withdraw.");
            		return false;
            	}
            	else
            	{
            		try 
            		{
            			int attemptedWithdrawal = Integer.parseInt(args[0]);
            			int currentStoredPoints = fwdb.getPlayerPoints(sender.getName());
            			//int currentStoredPoints = 1000; //Temporary value because I can't use the database
            			
            			//If the player doesn't have enough points
            			if(currentStoredPoints < attemptedWithdrawal)
            			{
            				sender.sendMessage(ChatColor.RED+"You don't have enough points to withdraw "+ChatColor.GOLD+attemptedWithdrawal+" points"+ChatColor.RED+".\nYou currently have "+ChatColor.GOLD+currentStoredPoints+" points"+ChatColor.RED+".");
                    		return true;
            			}
            			if(attemptedWithdrawal <= 0)
            			{
            				sender.sendMessage(ChatColor.RED+"You can't withdraw 0 or negative points.");
                    		return true;
            			}
            			else 
            			{
            				NBTManager manager = PowerNBT.getApi();
            				Player player = ((Player) sender);
            				ItemStack ticket = new ItemStack(Material.PAPER);
            				ItemMeta ticketMeta = ticket.getItemMeta();
            				
            				//Uses Bukkit methods to customize the item
            				ticketMeta.setDisplayName(ChatColor.GREEN+""+ChatColor.BOLD+"Fluff World Points Ticket");
            				List<String> lore = new ArrayList<String>();
            				lore.add("This piece of paper is redeemabled");
            				lore.add("for actual Fluff World MC points.");
            				lore.add("Worth: "+ChatColor.WHITE+""+ChatColor.BOLD+attemptedWithdrawal+" points");
            				lore.add("Right-click to redeem these points.");
            				lore.add("Ticket issued to: "+player.getName());
            				ticketMeta.setLore(lore);
            				
            				ticket.setItemMeta(ticketMeta);
            				
            				//Bukkit won't let us modify the NBT directly (what we need to do to store custom metadata), so we have to use a library.
            				//Follows NBT structure as listed here: http://minecraft.gamepedia.com/Tutorials/Command_NBT_tags
            				
            				//Gets the ticket item's NBT data
            				NBTCompound ticketNBT = manager.read(ticket);
            				
            				//Gives item a glow effect by giving it an empty "ench" (enchantment) list of enchantments
            				ticketNBT.put("ench", new NBTList());
            				
            				//Apply custom NBT tag that contains the points (custom NBT tags go under the item.tag compound)
            				ticketNBT.put("item", new NBTCompound());
            				ticketNBT.getCompound("item").put("tag", new NBTCompound());
            				ticketNBT.getCompound("item").getCompound("tag").put("FWMCTicketPointValue", attemptedWithdrawal);
           		 
            				//Prints out the mojangson (pseudo-JSON) string of NBT data for inspection
            				//System.out.println(ticketNBT);
            				
            				//Applies the NBT data to the item
            				manager.write(ticket, ticketNBT);
            				
            				//If the player has no room in their inventory
            		        if(player.getInventory().firstEmpty() == -1)
            		        {
            		        	sender.sendMessage(ChatColor.RED+"You don't have room in your inventory for a Points Ticket.\nTransaction cancelled.");
                        		return true;
            		        }
            		        else
            		        {
            		        	fwdb.subtractPlayerPoints(((Player)sender).getName(), attemptedWithdrawal);
            		        	System.out.println("Player "+((Player)sender).getName()+" had "+attemptedWithdrawal+" points subtracted via Ticket withdrawal.");
            		        	player.getInventory().addItem(ticket);
            		        	System.out.println("Granted player "+((Player)sender).getName()+" a FWMC Points Ticket for: "+attemptedWithdrawal+" points.");
            		        	sender.sendMessage(ChatColor.GREEN+"Here's your ticket for: "+attemptedWithdrawal+" points. Don't lose it.");
            		        	fsb.refreshPlayerPoints(((Player)sender).getName());
            		        	return true;
            		        }
            			}
            			
            		}
            		catch(NumberFormatException e)
            		{
            			sender.sendMessage("That's not a valid integer number.");
                		return false;
            		}
            	}
            }
            else if(command.getName().equalsIgnoreCase("bedspawn"))
            {
            	try 
            	{
            		Player p = sender.getServer().getPlayer(sender.getName());
                	Location bukkit_bed = p.getBedSpawnLocation();
                	ChatColor color = fwdb.getChatColor(sender.getName());
                	//ChatColor color = ChatColor.WHITE; //Temporary value because I can't use the database
                	
                	if(args.length == 0)
                	{
                		if(p.getPlayer().getBedSpawnLocation() == null)
                    	{
                			//Player doesn't have any record of a bed
                    		p.sendMessage(ChatColor.GRAY+""+ChatColor.ITALIC+"Couldn't locate your home bed.");
        					p.sendMessage(ChatColor.GRAY+""+ChatColor.ITALIC+"To fix this message, try sleeping in your bed again.");
                    	}
                		else
                		{
                			sender.sendMessage(color + "Your bed:" + ChatColor.RESET + " X: " + bukkit_bed.getBlockX() + "  Y: " + bukkit_bed.getBlockY() + "  Z: " + bukkit_bed.getBlockZ());
                		}
                	}
                	else if(args[0].equals("broadcast"))
                	{
                		if(p.getPlayer().getBedSpawnLocation() == null)
                    	{
                    		
                			//Player doesn't have any record of a bed
                    		p.sendMessage(ChatColor.GRAY+""+ChatColor.ITALIC+"Couldn't locate your home bed.");
        					p.sendMessage(ChatColor.GRAY+""+ChatColor.ITALIC+"To fix this message, try sleeping in your bed again.");			
                    	}
                		else
                		{
                			Bukkit.broadcastMessage(color + p.getName() +"'s bed:" + ChatColor.RESET + " X: " + bukkit_bed.getBlockX() + "  Y: " + bukkit_bed.getBlockY() + "  Z: " + bukkit_bed.getBlockZ());
                		}
                	}
            	}
            	catch(Exception e)
            	{
            		sender.sendMessage("Sorry, only online users may use this command.");
            		return false;
            	}
            	return true;
            }
            else if(command.getName().equalsIgnoreCase("dropxp"))
            {
        		try
        		{
        			Player player = sender.getServer().getPlayer(sender.getName());
            		int requestedDrop = Integer.parseInt(args[0]);
            		ExperienceManager manager = new ExperienceManager(player);
                	if(manager.getTotalExperience() >= requestedDrop)
                	{
                		//Adjusting variables
                		manager.setTotalExperience(manager.getTotalExperience() - requestedDrop);
                		//fwdb.removeXp(player.getName(), requestedDrop);
                		
                		//Dropping the orbs
                        ExperienceOrb orb = null;
                        Entity ent = player.getWorld().spawnEntity(player.getLocation().add(7, 0, 0), EntityType.EXPERIENCE_ORB);
                        orb = (ExperienceOrb)ent;
                        orb.setExperience(requestedDrop);
                	}
                	else
                	{
                		sender.sendMessage("You don\'t have enough experience to drop "+requestedDrop+" orbs.");
                	}
                	
                	
                	
        		}
        		catch(Exception e)
        		{
        			return false;
        		}
            }
            else if(command.getName().equalsIgnoreCase("convertxp"))
            {
            	try
            	{
            		if(args[1].toLowerCase().equals("orbs") || args[1].toLowerCase().equals("orb"))
            		{
            			int orbs = Integer.parseInt(args[0]);
            			int levels = Util.expOrbsToLevels(orbs);
            			int remainder = orbs-Util.levelsToExpOrbs(levels);
            			sender.sendMessage(ChatColor.GRAY+""+ChatColor.ITALIC+""+orbs+" orbs = "+levels+" levels + "+remainder+" orbs remaining.");
            			return true;
            		}
            		else if(args[1].toLowerCase().equals("levels") || args[1].toLowerCase().equals("level")) 
            		{
            			int levels = Integer.parseInt(args[0]);
            			int orbs = Util.levelsToExpOrbs(levels);
            			sender.sendMessage(ChatColor.GRAY+""+ChatColor.ITALIC+""+levels+" levels = "+orbs+" orbs.");
            			return true;
            		}
            		else
            		{
            			return false;
            		}
            	}
            	catch(Exception e)
            	{
            		return false;
            	}
            }
        }
        else
        {
        	sender.sendMessage("Sorry, you have to be a player to use that command.");
        }
        return false;
    }
    
}
