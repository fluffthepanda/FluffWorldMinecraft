/*
 * Decompiled from com.earth2me.essentials.craftbukkit.SetExpFix
 * 
 * This Java class is from: https://hub.spigotmc.org/jenkins/job/Spigot-Essentials/
 * 
 * It is not an original creation of github users: fluffthepanda or au5ton
 * 
 */


package world.fluff;

import org.bukkit.entity.Player;

public class SetExpFix {
    public static void setTotalExperience(Player player, int exp) {
        if (exp < 0) {
            throw new IllegalArgumentException("Experience is negative!");
        }
        player.setExp(0.0f);
        player.setLevel(0);
        player.setTotalExperience(0);
        int amount = exp;
        while (amount > 0) {
            int expToLevel = SetExpFix.getExpAtLevel(player);
            if ((amount -= expToLevel) >= 0) {
                player.giveExp(expToLevel);
                continue;
            }
            player.giveExp(amount += expToLevel);
            amount = 0;
        }
    }

    private static int getExpAtLevel(Player player) {
        return SetExpFix.getExpAtLevel(player.getLevel());
    }

    public static int getExpAtLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        }
        if (level >= 16 && level <= 30) {
            return 5 * level - 38;
        }
        return 9 * level - 158;
    }

    public static int getExpToLevel(int level) {
        int exp = 0;
        for (int currentLevel = 0; currentLevel < level; ++currentLevel) {
            exp += SetExpFix.getExpAtLevel(currentLevel);
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }

    public static int getTotalExperience(Player player) {
        int exp = Math.round((float)SetExpFix.getExpAtLevel(player) * player.getExp());
        int currentLevel = player.getLevel();
        while (currentLevel > 0) {
            exp += SetExpFix.getExpAtLevel(--currentLevel);
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }

    public static int getExpUntilNextLevel(Player player) {
        int exp = Math.round((float)SetExpFix.getExpAtLevel(player) * player.getExp());
        int nextLevel = player.getLevel();
        return SetExpFix.getExpAtLevel(nextLevel) - exp;
    }
}

