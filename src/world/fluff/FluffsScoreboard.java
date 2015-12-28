package world.fluff;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

public class FluffsScoreboard {
	private Scoreboard fwScoreboard;
	public FluffsScoreboard(Scoreboard fsb)
	{
		fwScoreboard = fsb;
		createXPObjective();
		createHealthObjective();
		//createPointsObjective();
	}
	
	/*private void createPointsObjective()
	{
		fwScoreboard.registerNewObjective("FWMC Points", "FWMC_POINTS");
		fwScoreboard.getObjective("FWMC Points").setDisplaySlot(DisplaySlot.SIDEBAR);
	}*/
	
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
	
	/*public void refreshPlayerPoints(String name)
	{
		//fwScoreboard.getObjective("XP").getScore(name).setScore();
	}*/
	
	public void refreshPlayerXP(Player player)
	{
		fwScoreboard.getObjective("XP").getScore(player.getName()).setScore(player.getLevel());
	}
}
