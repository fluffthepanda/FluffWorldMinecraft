package world.fluff.transaction;

import java.util.ArrayList;

public class TransactionManager 
{
	
	/*
	 * IMPORTANT
	 * 
	 * Ideally, there shouldn't be a transaction of the same type between the same two players, 
	 * thus making them identifiable to the client-code throughout the plugin.
	 * It is imperative to recognize when an interaction is over 
	 * (even if a player didn't intend for it to happen, such as accidentally closing the inventory)
	 * and calling the close() method for a transaction, canceling the transaction completely.
	 * 
	 * With this in mind, we can effectively use this transaction system to store information 
	 * temporarily for independent confrontations. In addition, this system isn't built around
	 * what the player is trading and most around who is involved.
	 * 
	 */
	
	//The ledger of active transactions that are still in progress
	private static ArrayList<PlayerTransaction> activeLedger = new ArrayList<PlayerTransaction>();
	public static long lastId = 0;
	
	public static long getNextID()
	{
		return ++lastId; //Always has a new ID, even if the transaction was removed from the active ledger.
		//Uses plugin config file to keep track of this. See FluffWorldIntegration.java
	}

	public static void remove(long id) {
		//Progresses backwards in the array just in case there's multiple instances of a transaction by accident
		for(int i = activeLedger.size()-1; i >= 0; i--)
		{
			if(activeLedger.get(i).id == id)
			{
				activeLedger.remove(i);
			}
		}
	}

	public static void add(PlayerTransaction transaction) {
		activeLedger.add(transaction);
	}
	
	public PlayerTransaction getTransactionById(long id)
	{
		for(PlayerTransaction item : activeLedger)
		{
			if(item.id == id)
			{
				return item;
			}
		}
		return null;
	}

}
