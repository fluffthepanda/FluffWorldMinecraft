package world.fluff;

import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import me.dpohvar.powernbt.PowerNBT;
import me.dpohvar.powernbt.api.NBTCompound;
import me.dpohvar.powernbt.api.NBTManager;

import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
//import org.bukkit.event.inventory.FurnaceExtractEvent;

public class FluffBlockDropListener implements Listener {
	FWDBConnection fwdb;
	FluffsScoreboard fsb;
	ArrayList<Integer> mobsFromSpawnBlocks = new ArrayList<Integer>();
	public FluffBlockDropListener(FWDBConnection conn, FluffsScoreboard fwScoreboard)
	{
		fwdb = conn;
		fsb = fwScoreboard;
	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		//Database times out after 8 hours.
		//checkConnection() tries to read a dummy value from the database
		//The JDBC driver in FWDBConnection.java is set to autoReconnect=true
		//If checkConnection fails, the driver automatically knows to reconnect, so the next DB access attempt will have a live connection.
		
		if(!fwdb.checkConnection())
		{
			Bukkit.broadcastMessage(ChatColor.RED + "Database connection timed out. " + ChatColor.GREEN + "Reconnecting..." + ChatColor.RESET);
		}
		
		String name = event.getPlayer().getName();
		Player player = event.getPlayer();
		int p = fwdb.getPlayerPoints(name);
		ChatColor color = fwdb.getChatColor(name);
		
		event.getPlayer().setPlayerListName(color + name + ChatColor.RESET);
		event.getPlayer().setDisplayName(color + name + ChatColor.RESET);
		Bukkit.broadcastMessage("Welcome, " + color + name + ChatColor.RESET + ".");
		Bukkit.broadcastMessage(color + name + ChatColor.RESET + " has " + p + " FWMC points.");
		event.setJoinMessage(null);
		player.setScoreboard(fsb.getScoreboard()); //set custom scoreboard (XP tracker)
		fsb.refreshPlayerXP(player);
		fsb.refreshPlayerPoints(player.getName());
		
		//After everything else is good to go, let's get that bed compass fired up again
		if(event.getPlayer().getBedSpawnLocation() != null)
		{
			event.getPlayer().setCompassTarget(event.getPlayer().getBedSpawnLocation());
		}
		else
		{
			if(event.getPlayer().getInventory().contains(Material.COMPASS))
			{
				event.getPlayer().sendMessage(ChatColor.GRAY+""+ChatColor.ITALIC+"Couldn't locate your home bed. Your compass will not point to it.");
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event)
	{
		String name = event.getPlayer().getName();
		ChatColor color = fwdb.getChatColor(name);
		fsb.removePlayerFromSidebar(color + name);
		event.setQuitMessage(fwdb.getChatColor(name) + name + ChatColor.YELLOW + " left the game.");
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		//Bukkit.broadcastMessage(event.getBlock().getType().toString() + " has been broken by " + event.getPlayer().getDisplayName() + ".");
		String name = event.getPlayer().getName();
		Material block = event.getBlock().getType();
		ItemStack item = event.getPlayer().getItemInHand();
		int rows = 0;
		fwdb.addBlockBreakStat(name);
		if(!item.containsEnchantment(Enchantment.SILK_TOUCH)) //no points for blocks broken with silk touch for obvious reasons
		{
			if(item.getType() == Material.IRON_PICKAXE || item.getType() == Material.DIAMOND_PICKAXE) //makes sure minerals are harvested and block is not wasted
			{
				if(block == Material.DIAMOND_ORE)
				{
					rows = fwdb.givePlayerPoints(name, 10);
				}
				else if(block == Material.EMERALD_ORE)
				{
					rows = fwdb.givePlayerPoints(name, 25);
				}
			}
			if(item.getType() == Material.STONE_PICKAXE || item.getType() == Material.IRON_PICKAXE || item.getType() == Material.DIAMOND_PICKAXE)
			{
				if(block == Material.LAPIS_ORE)
				{
					rows = fwdb.givePlayerPoints(name, 3);
				}
			}
			if(rows > 0)
			{
				fsb.refreshPlayerPoints(name);
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		String name = event.getPlayer().getName();
		if(!event.isCancelled())
		{
			fwdb.addBlockPlaceStat(name);
		}
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event)
	{
		String message = event.getMessage();
		ChatColor color = fwdb.getChatColor(event.getPlayer().getName());
		event.setMessage(color + message);
	}
	
	@EventHandler
	public void onCreatureKill(EntityDeathEvent event)
	{
		if(mobsFromSpawnBlocks.contains((Integer)event.getEntity().getEntityId()))
		{
			mobsFromSpawnBlocks.remove((Integer)event.getEntity().getEntityId());
			return;
		}
		if(event.getEntity().getKiller() != null)
		{
			Player player = event.getEntity().getKiller().getPlayer();
			String name = event.getEntity().getKiller().getName();
			EntityType ent = event.getEntity().getType();
			int rows = 0;
			if(ent == EntityType.ENDERMAN)
			{
				if(player.getWorld().getEnvironment().equals(World.Environment.THE_END))
				{
					rows = fwdb.givePlayerPoints(name, 2);
				}
				else
				{
					rows = fwdb.givePlayerPoints(name, 8);
				}
			}
			else if(ent == EntityType.CAVE_SPIDER)
			{
				rows = fwdb.givePlayerPoints(name, 6);
			}
			else if(ent == EntityType.ENDER_DRAGON)
			{
				rows = fwdb.givePlayerPoints(name, 2500);
			}
			else if(ent == EntityType.WITHER)
			{
				rows = fwdb.givePlayerPoints(name,  500);
			}
			else if(ent == EntityType.BLAZE)
			{
				rows = fwdb.givePlayerPoints(name, 5);
			}
			else if(ent == EntityType.SKELETON)
			{
				Skeleton s = (Skeleton) event.getEntity();
				
				if(s.getSkeletonType() == SkeletonType.WITHER)
				{
					rows = fwdb.givePlayerPoints(name, 4);
				}
				else
				{
					rows = fwdb.givePlayerPoints(name, 1);
					
					if(player.getInventory().getItemInHand().containsEnchantment(Enchantment.LOOT_BONUS_MOBS)) //has Looting enchantment
					{
						if(ThreadLocalRandom.current().nextInt(3) == 2) //33% chance of dropping
						{
							//Skeletons will drop 0-3 gunpowder.
							event.getDrops().add(new ItemStack(Material.SULPHUR, ThreadLocalRandom.current().nextInt(4)));
						}
					}
					else {
						if(ThreadLocalRandom.current().nextInt(5) == 3) //20% chance of dropping
						{
							//Skeletons will drop 0-2 gunpowder.
							event.getDrops().add(new ItemStack(Material.SULPHUR, ThreadLocalRandom.current().nextInt(3)));
						}
					}
				}
			}
			else if(ent == EntityType.ZOMBIE)
			{
				rows = fwdb.givePlayerPoints(name, 1);
			}
			else if(ent == EntityType.CREEPER)
			{
				rows = fwdb.givePlayerPoints(name, 2);
			}
			else if(ent == EntityType.GHAST)
			{
				rows = fwdb.givePlayerPoints(name, 6); //point count subject to change
			}
			else if(ent == EntityType.SPIDER)
			{
				long time = event.getEntity().getKiller().getWorld().getTime();
				if(time > 12000 && time < 24000) //12000 = 6pm, 23999 = 5:59am
				{
					rows = fwdb.givePlayerPoints(name, 1);
				}
			}
			else if(ent == EntityType.WITCH)
			{
				rows = fwdb.givePlayerPoints(name, 3);
			}
			else if(ent == EntityType.SILVERFISH)
			{
				rows = fwdb.givePlayerPoints(name, 1);
			}
			else if(ent == EntityType.ENDERMITE)
			{
				rows = fwdb.givePlayerPoints(name, 1);
			}
			else if(ent == EntityType.GUARDIAN)
			{
				Guardian g = (Guardian) event.getEntity();
				if(g.isElder()) {
					//The big boss Guardian
					rows = fwdb.givePlayerPoints(name, 350); //point count subject to change
				}
				else
				{
					rows = fwdb.givePlayerPoints(name, 15); //point count subject to change
				}
			}
			if(rows > 0)
			{
				fsb.refreshPlayerPoints(name);
			}
		}
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		EntityType ent = event.getEntityType();
		if(ent == EntityType.CREEPER)
		{
			event.setCancelled(true); //cause fuck creepers.
		}
	}
	
	@EventHandler
	public void onItemDespawn(ItemDespawnEvent event)
	{
		Material item = event.getEntity().getItemStack().getType();
		if(item == Material.BONE || item == Material.ROTTEN_FLESH || item == Material.STRING || item == Material.EGG || item == Material.ARROW)
		{
			return;
		}
		int amount = event.getEntity().getItemStack().getAmount();
		String itemName = event.getEntity().getItemStack().getType().toString();
		if(amount > 1)
		{
			Bukkit.broadcastMessage(ChatColor.YELLOW + "" + amount + " " + ChatColor.RESET + itemName + ChatColor.YELLOW + " despawned.");
		}
		else
		{
			Bukkit.broadcastMessage(itemName + ChatColor.YELLOW + " despawned.");
		}
	}
	
	@EventHandler
	public void onExpGain(PlayerExpChangeEvent event)
	{
		String name = event.getPlayer().getName();
		int amount = event.getAmount();
		fwdb.addXp(name, amount);
	}
	
	@EventHandler
	public void onLevelChange(PlayerLevelChangeEvent event)
	{
		fsb.refreshPlayerXP(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		Player killer = event.getEntity().getKiller();
		Player died = event.getEntity();
		if(killer != null)
		{
			int available_points = fwdb.getPlayerPoints(died.getName());
			if(available_points > 0)
			{
				int points_taken = 0;
				if(available_points > 100)
				{
					points_taken = 100;
				}
				else
				{
					points_taken = available_points;
				}
				fwdb.givePlayerPoints(killer.getName(), points_taken, false);
				fwdb.subtractPlayerPoints(died.getName(), points_taken, false);
				fsb.refreshPlayerPoints(killer.getName());
				fsb.refreshPlayerPoints(died.getName());
				event.setDeathMessage(event.getDeathMessage() + "; took " + points_taken + " points.");
			}
		}
		else
		{
			Location death_loc = died.getLocation();
			int available_points = fwdb.getPlayerPoints(died.getName());
			if(available_points > 0)
			{
				int points_taken = 0;
				if(available_points > 100)
				{
					points_taken = 100;
				}
				else
				{
					points_taken = available_points;
				}
				fwdb.subtractPlayerPoints(died.getName(), points_taken, false);
				fsb.refreshPlayerPoints(died.getName());
				event.setDeathMessage(event.getDeathMessage() + " and lost " + points_taken + " points.");
			}
			Bukkit.broadcastMessage(fwdb.getChatColor(died.getName()) + died.getName() + " " + ChatColor.RESET + "died at X: " + death_loc.getBlockX() + "  Y: " + death_loc.getBlockY() + "  Z: " + death_loc.getBlockZ());
		}
	}
	
	@EventHandler
	public void onSmelt(FurnaceExtractEvent event)
	{
		Material block = event.getItemType();
		String name = event.getPlayer().getName();
		int exp = event.getExpToDrop(); //getItemAmount() doesn't work for values > 1. We can get the # of items by getting the total EXP and dividing by EXP-per-item
		int rows = 0;
		if(block == Material.GOLD_INGOT)
		{
			double expPerItem = 1.0;
			int amount = (int)Math.ceil(exp/expPerItem);
			rows = fwdb.givePlayerPoints(name, amount * 2);
		}
		else if(block == Material.IRON_INGOT)
		{
			double expPerItem = 0.7;
			int amount = (int)Math.ceil(exp/expPerItem);
			rows = fwdb.givePlayerPoints(name, amount);
		}
		if(rows > 0)
		{
			fsb.refreshPlayerPoints(name);
		}
	}
	
	@EventHandler
	public void onSpawnerSpawn(SpawnerSpawnEvent event)
	{
		mobsFromSpawnBlocks.add(event.getEntity().getEntityId());
	}
	
	@EventHandler
	public void onPlayerLeftBed(PlayerBedLeaveEvent event)
	{
		event.getPlayer().setCompassTarget(event.getPlayer().getLocation());
		event.getPlayer().setBedSpawnLocation(event.getPlayer().getLocation());
		event.getPlayer().sendMessage(ChatColor.GREEN+"Your compass will now point to this bed.");
	}
	
	@EventHandler
	public void onPlayerUse(PlayerInteractEvent event)
	{
	    Player player = event.getPlayer();
	 
	    //If the player right clicks in the air with paper (a Ticket)
	    if(player.getItemInHand().getType() == Material.PAPER && event.getAction() == Action.RIGHT_CLICK_AIR)
	    {
	    	
	    	ItemStack ticket = new ItemStack(player.getItemInHand());
	    	NBTManager manager = PowerNBT.getApi();
	    	NBTCompound ticketNBT = manager.read(ticket);
	    	
	    	//Prints out the mojangson (pseudo-JSON) string of NBT data for inspection
	    	//System.out.println(ticketNBT);
	    	
	    	//If the ticket has the FWMCTicketPointValue integer
	    	if(ticketNBT.containsKey("item") && ticketNBT.getCompound("item").containsKey("tag") && ticketNBT.getCompound("item").getCompound("tag").containsKey("FWMCTicketPointValue"))
	    	{
	    		int pointValue;
	    		try
	    		{
	    			//Check to make sure the value in the compound is valid
	    			pointValue = (Integer) ticketNBT.getCompound("item").getCompound("tag").get("FWMCTicketPointValue");
	    		}
	    		catch(NumberFormatException e)
	    		{
	    			player.sendMessage(ChatColor.RED+"This ticket has an invalid point value. Contact the server admin.");
	    			pointValue = -1;
	    		}
	    		if(pointValue <= 0)
	    		{
	    			player.sendMessage(ChatColor.RED+"This ticket has an invalid point value. Contact the server admin.");
	    		}
	    		else if(pointValue > 0) 
	    		{
	    			//Remove one instance of an item from the stack
	    			if(ticket.getAmount() > 1)
	    			{
	    				ticket.setAmount(ticket.getAmount()-1);
	    				player.getInventory().setItem(player.getInventory().getHeldItemSlot(), ticket);
	    			}
	    			else
	    			{
	    				player.getInventory().setItem(player.getInventory().getHeldItemSlot(), new ItemStack(Material.AIR));
	    			}
	    			
		        	System.out.println("Player "+player.getName()+" redeemed a FWMC Points Ticket for: "+pointValue+" points.");
		        	fwdb.givePlayerPoints(player.getName(), pointValue);
		        	System.out.println("Player "+player.getName()+" had "+pointValue+" points added via Ticket redemption.");
		        	fsb.refreshPlayerPoints(player.getName());
		        	player.sendMessage(ChatColor.GREEN+""+pointValue+" points redeemed.");
	    		}
	    	}
	    }
	}
	
	@EventHandler
	public void onPlayerCraftItem(CraftItemEvent event)
	{
    	//If the player tries to craft something with paper
		if(event.getInventory().contains(Material.PAPER))
		{
			NBTManager manager = PowerNBT.getApi();
			for(ItemStack item : event.getInventory())
			{
				if(item == null)
				{
					//
				}
				else
				{
					//System.out.println(item);
					NBTCompound nbtData = manager.read(item);
					if(nbtData == null)
					{
						//System.out.println("No NBT data here.");
					}
					else
					{
						if(nbtData.containsKey("item"))
						{
							if(nbtData.getCompound("item").containsKey("tag"))
							{
								if(nbtData.getCompound("item").getCompound("tag") != null)
								{
									if(nbtData.getCompound("item").getCompound("tag").containsKey("FWMCTicketPointValue"))
									{
										//This is a FWMC Potions Ticket, so we don't want them to try and craft with it by mistake
										event.setCancelled(true);
										event.getWhoClicked().sendMessage(ChatColor.YELLOW+"You cannot craft using a FWMC Points Ticket.");
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
