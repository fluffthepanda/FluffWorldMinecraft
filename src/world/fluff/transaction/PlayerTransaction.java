package world.fluff.transaction;

import org.bukkit.entity.Player;

public abstract class PlayerTransaction 
{
	//Default values to detect for:
	protected long id = -1;
	protected Player sender = null;
	protected Player receiver = null;
	protected TransactionType transactionType = null;
	protected boolean isOpen = false;
	
	public abstract boolean isValid(); //Check if the transact() method can be called appropriately
	
	//Generate a new transaction ID and add the trade to the ledger
	public void open() {
		this.id = TransactionManager.getNextID();
		TransactionManager.add(this);
		this.isOpen = true;
	}
	
	//Remove the trade from the ledger, doesn't matter if the transaction finished or not
	public void close() {
		TransactionManager.remove(this.id);
		this.isOpen = false;
	}
	
	protected abstract void transact(); //Actually commences the transaction, depends on the user calling isValid() for predictability
	public void doTransaction()
	{
		//This is simply for logging purposes.
		//Subclasses that extend PlayerTransaction can still customize transact(), but should use doTransaction() for logging.
		transact();
		System.out.println("Transaction of type "+transactionType+" has been done between "+sender.getName()+" and "+receiver.getName()+" (id: "+id+")");
	}
	
}
