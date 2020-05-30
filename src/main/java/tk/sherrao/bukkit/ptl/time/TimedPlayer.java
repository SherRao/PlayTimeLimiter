package tk.sherrao.bukkit.ptl.time;

import java.util.UUID;

public class TimedPlayer {
	
	protected UUID uuid;
	protected int timePlayed;
	
	public TimedPlayer( UUID uuid ) {
		this.uuid = uuid;
		
	}
	
	public void incrementTimePlayed() { 
		timePlayed++; 
		
	}
	
	public void setTimePlayed( int timePlayed ) {
		this.timePlayed = timePlayed;
		
	}
	
	public UUID getUUID() {
		return uuid;
		
	}
	
	public int getTimePlayed() { 
		return timePlayed; 
		
	}
	
}