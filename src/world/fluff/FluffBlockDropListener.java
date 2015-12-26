package world.fluff;

import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import java.util.concurrent.ThreadLocalRandom;
//import org.bukkit.event.inventory.FurnaceExtractEvent;

public class FluffBlockDropListener implements Listener {
	FWDBConnection fwdb;
	FluffsScoreboard fsb;
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
	}
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		//Bukkit.broadcastMessage(event.getBlock().getType().toString() + " has been broken by " + event.getPlayer().getDisplayName() + ".");
		String name = event.getPlayer().getName();
		Material block = event.getBlock().getType();
		ItemStack item = event.getPlayer().getItemInHand();
		fwdb.addBlockBreakStat(name);
		if(!item.containsEnchantment(Enchantment.SILK_TOUCH)) //no points for blocks broken with silk touch for obvious reasons
		{
			if(item.getType() == Material.IRON_PICKAXE || item.getType() == Material.DIAMOND_PICKAXE) //makes sure minerals are harvested and block is not wasted
			{
				if(block == Material.DIAMOND_ORE)
				{
					fwdb.givePlayerPoints(name, 10);
				}
				else if(block == Material.EMERALD_ORE)
				{
					fwdb.givePlayerPoints(name, 25);
				}
			}
			if(item.getType() == Material.STONE_PICKAXE || item.getType() == Material.IRON_PICKAXE || item.getType() == Material.DIAMOND_PICKAXE)
			{
				if(block == Material.LAPIS_ORE)
				{
					fwdb.givePlayerPoints(name, 3);
				}
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
		if(event.getEntity().getKiller() != null)
		{
			Player player = event.getEntity().getKiller().getPlayer();
			String name = event.getEntity().getKiller().getName();
			EntityType ent = event.getEntity().getType();
			if(ent == EntityType.ENDERMAN)
			{
				fwdb.givePlayerPoints(name, 8);
			}
			else if(ent == EntityType.CAVE_SPIDER)
			{
				fwdb.givePlayerPoints(name, 6);
			}
			else if(ent == EntityType.ENDER_DRAGON)
			{
				fwdb.givePlayerPoints(name, 2500);
			}
			else if(ent == EntityType.WITHER)
			{
				fwdb.givePlayerPoints(name,  500);
			}
			else if(ent == EntityType.BLAZE)
			{
				fwdb.givePlayerPoints(name, 5);
			}
			else if(ent == EntityType.SKELETON)
			{
				if(event.getEntity().getKiller().getWorld().getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ()) == Biome.HELL)
				{
					fwdb.givePlayerPoints(name, 4);
				}
				else
				{
					
					fwdb.givePlayerPoints(name, 1);
					
					//Without creepers, we need a source for gunpowder. Skeletons will drop 0-3 gunpowder.
					event.getDrops().add(new ItemStack(Material.SULPHUR, ThreadLocalRandom.current().nextInt(4)));
				}
				//Must check if Entity Data -> SkeletonType == 1 for Wither Skeleton
			}
			else if(ent == EntityType.ZOMBIE)
			{
				fwdb.givePlayerPoints(name, 1);
			}
			else if(ent == EntityType.CREEPER)
			{
				fwdb.givePlayerPoints(name, 2);
			}
			else if(ent == EntityType.GHAST)
			{
				fwdb.givePlayerPoints(name, 4);
			}
			else if(ent == EntityType.SPIDER)
			{
				long time = event.getEntity().getKiller().getWorld().getTime();
				if(time > 12000 && time < 24000) //12000 = 6pm, 23999 = 5:59am
				{
					fwdb.givePlayerPoints(name, 1);
				}
			}
			else if(ent == EntityType.WITCH)
			{
				fwdb.givePlayerPoints(name, 3);
			}
			else if(ent == EntityType.SILVERFISH)
			{
				fwdb.givePlayerPoints(name, 1);
			}
			else if(ent == EntityType.ENDERMITE)
			{
				fwdb.givePlayerPoints(name, 1);
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
				if(available_points > 50)
				{
					points_taken = 50;
				}
				else
				{
					points_taken = available_points;
				}
				fwdb.givePlayerPoints(killer.getName(), points_taken, false);
				fwdb.subtractPlayerPoints(died.getName(), points_taken, false);
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
				if(available_points > 10)
				{
					points_taken = 10;
				}
				else
				{
					points_taken = available_points;
				}
				fwdb.subtractPlayerPoints(died.getName(), points_taken, false);
				event.setDeathMessage(event.getDeathMessage() + " and lost " + points_taken + " points.");
			}
			Bukkit.broadcastMessage(fwdb.getChatColor(died.getName()) + died.getName() + " " + ChatColor.RESET + "died at X: " + death_loc.getBlockX() + "  Y: " + death_loc.getBlockY() + "  Z: " + death_loc.getBlockZ());
		}
	}
	
	// FurnaceExtractEvent.getItemAmount() doesn't work for amounts greater than 1; it'll just return 0
	/*@EventHandler
	public void onSmelt(FurnaceExtractEvent event)
	{
		Material block = event.getItemType();
		String name = event.getPlayer().getName();
		int amount = event.getItemAmount();
		if(amount != 0)
		{
			if(block == Material.GOLD_INGOT)
			{
				fwdb.givePlayerPoints(name, amount * 2);
			}
			else if(block == Material.IRON_INGOT)
			{
				fwdb.givePlayerPoints(name, amount);
			}
		}
	}*/
}
