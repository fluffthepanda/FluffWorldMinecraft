package world.fluff;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

public class FluffsScoreboard {
	private Scoreboard fwScoreboard;
	public FluffsScoreboard(Scoreboard fsb)
	{
		fwScoreboard = fsb;
		createXPObjective();
		//createPointsObjective();
	}
	
	/*private void createPointsObjective()
	{
		fwScoreboard.registerNewObjective("FWMC Points", "FWMC_POINTS");
		fwScoreboard.getObjective("FWMC Points").setDisplaySlot(DisplaySlot.SIDEBAR);
	}*/
	
	private void createXPObjective()
	{
		fwScoreboard.registerNewObjective("XP", "CURRENT_XP");
		fwScoreboard.getObjective("XP").setDisplaySlot(DisplaySlot.PLAYER_LIST);
	}
	
	public Scoreboard getScoreboard()
	{
		return fwScoreboard;
	}
	
	public void kill()
	{
		fwScoreboard.getObjective("XP").unregister();
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
