package world.fluff.transaction;

import org.bukkit.entity.Player;

public class XPDonation extends PlayerTransaction 
{
	private int amount;
	
	public XPDonation(Player sender, Player receiver, int amount)
	{
		this.sender = sender;
		this.receiver = receiver;
		this.amount = amount;
	}
	
	public XPDonation(Player sender, Player receiver, int amount, boolean autoOpen)
	{
		this.sender = sender;
		this.receiver = receiver;
		this.amount = amount;
		if(autoOpen)
		{
			this.open();
		}
	}

	@Override
	public boolean isValid() {
		
		if(this.id == 0)
		{
			return false;
		}
		else
		{
			if(sender.getLevel() > amount) //The player has the levels to spare
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}

	@Override
	public void open() {
		this.id = TransactionManager.getNextID();
		TransactionManager.add(this);
		this.isOpen = true;
	}

	@Override
	public void close() {
		TransactionManager.remove(this.id);
		this.isOpen = false;
	}

	@Override
	protected void transact() {
		//Actually give the levels
		this.sender.setLevel(this.sender.getLevel()-amount);
		this.receiver.setLevel(this.receiver.getLevel()+amount);
	}

}
