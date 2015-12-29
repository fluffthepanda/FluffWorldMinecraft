package world.fluff;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

public class FluffsScoreboard {
	private Scoreboard fwScoreboard;
	private FWDBConnection fwdb;
	public FluffsScoreboard(Scoreboard fsb, FWDBConnection conn)
	{
		fwScoreboard = fsb;
		fwdb = conn;
		createXPObjective();
		createHealthObjective();
		createPointsObjective();
	}
	
	private void createPointsObjective()
	{
		fwScoreboard.registerNewObjective("FWMCPoints", "FWMC_POINTS");
		fwScoreboard.getObjective("FWMCPoints").setDisplayName("FWMC Points");
		fwScoreboard.getObjective("FWMCPoints").setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	
	private void createXPObjective()
	{
		//Player Experience
		fwScoreboard.registerNewObjective("XP", "CURRENT_XP");
		fwScoreboard.getObjective("XP").setDisplaySlot(DisplaySlot.PLAYER_LIST);
	}
	
	private void createHealthObjective()
	{
		//Player Health
		fwScoreboard.registerNewObjective("health", "health"); //(String name, String criteriaType)
		fwScoreboard.getObjective("health").setDisplayName(ChatColor.RED+"\u2665");
		fwScoreboard.getObjective("health").setDisplaySlot(DisplaySlot.BELOW_NAME);
	}
	
	public Scoreboard getScoreboard()
	{
		return fwScoreboard;
	}
	
	public void kill()
	{
		fwScoreboard.getObjective("XP").unregister();
		fwScoreboard.getObjective("health").unregister();
	}
	
	public void refreshPlayerPoints(String name)
	{
		fwScoreboard.getObjective("FWMCPoints").getScore(fwdb.getChatColor(name) + name).setScore(fwdb.getPlayerPoints(name));
	}
	
	public void refreshPlayerXP(Player player)
	{
		fwScoreboard.getObjective("XP").getScore(player.getName()).setScore(player.getLevel());
	}
}
