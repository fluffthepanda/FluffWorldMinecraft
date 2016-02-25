package world.fluff;

import org.bukkit.entity.Player;

public class Util 
{
	
	//XP ORBS MATH FUNCTIONS
	//NOTE: x == Math.floor(expOrbsToLevels(levelsToExpOrbs(x)))
	//This should be true^
	
	public static final int NATURAL_PLAYER_LEVEL_CAP = 21863; //The experience level cap for players who earn exp without commands or mods
	
	//Source: http://minecraft.gamepedia.com/Experience#Leveling_up
	public static int levelsToExpOrbs(int level) {
		if(level > 0 && level <= 16) {
			return (int) (Math.pow(level,2) + 6*level);
		}
		else if(level >= 17 && level <= 31) {
			return (int) (2.5*Math.pow(level,2) - 40.5*level + 360);
		}
		else if(level >= 32) {
			return (int) (4.5*Math.pow(level,2) - 162.5*level + 2220);
		}
		return 0; //invalid scenario
	}
	
	//Source: http://www.wolframalpha.com/input/?i=inverse+function+of+f(x)%3Dx²%2B6x
	//Source: http://minecraft.gamepedia.com/Experience#Leveling_up
	public static int expOrbsToLevels(int orbs) {
		if(orbs > 0 && orbs <= 352) {
			return (int) Math.floor(-3 + Math.sqrt(orbs+9));
		}
		else if(orbs >= 353 && orbs <= 1507) {
			return (int) Math.floor(8.1 + 0.1*Math.sqrt(40*orbs-7839));
		}
		else if(orbs >= 1508) {
			return (int) Math.floor(18.0556 + 0.0555556*Math.sqrt(72*orbs-54215));
		}
		return 0; //invalid scenario
	}
	
	//Tells how many orbs it took from your last level to get to this level
	public static int levelWorth(int level) {
		level -= 1;
		if(level >= 0 && level <= 16) {
			return 2*level + 7;
		}
		else if(level >= 17 && level <= 31) {
			return 5*level - 38;
		}
		else if(level >= 32) {
			return 9*level - 158;
		}
		return 0; //invalid scenario
	}
	
	/*
	
	2[Current Level] + 7 (at levels 0-16)
	5[Current Level] - 38 (at levels 17-31)
	9[Current Level] - 158 (at level 32+)
	
	*/
	public static int getTotalExpOrbsFromPlayer(Player player) {
		return levelsToExpOrbs(player.getLevel())+player.getTotalExperience();
	}
	
	public static void subtractExpOrbsFromPlayer(Player player, int orbs) {
		int orbBuffer = player.getTotalExperience();
		//The player has enough orbs without a converting any levels to orbs
		if(orbBuffer >= orbs)
		{
			player.setTotalExperience(player.getTotalExperience()-orbs);
		}
		else
		{
			int levelWorth = 0;
			while(orbBuffer < orbs)
			{
				levelWorth = levelWorth(player.getLevel());
				player.setLevel(player.getLevel()-1);
				orbBuffer += levelWorth;
			}
			orbBuffer -= levelWorth; //Does this to prevent remainder orbs from being lost
			player.setTotalExperience(player.getTotalExperience()+orbBuffer); //Does this to prevent remainder orbs from being lost
		}
	}
	
	public static void addExpOrbsFromPlayer(Player player, int orbs) {
		//int orbs = Integer.parseInt(args[0]);
		int levels = Util.expOrbsToLevels(orbs);
		int remainder = orbs-Util.levelsToExpOrbs(levels);
		player.setLevel(player.getLevel()+levels);
		if(remainder > player.getExpToLevel())
		{
			remainder -= player.getExpToLevel();
			player.setLevel(player.getLevel()+1);
			player.setTotalExperience(remainder);
		}
		else
		{
			player.setTotalExperience(player.getTotalExperience()+remainder);
		}
		
	}

}
