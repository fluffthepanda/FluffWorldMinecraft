package world.fluff.transaction;

public class TransactionType 
{
	public static final int XP_DONATION = 0;
	
	private int type;
	
	public boolean equals(TransactionType type)
	{
		return type.type == this.type;
	}
	
	public String toString()
	{
		switch(this.type)
		{
			case XP_DONATION:
				return "XP_DONATION";
			default:
				return "NONE";
		}
	}
	
}
