package world.fluff.transaction;

import java.util.ArrayList;

public class TransactionManager 
{
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
