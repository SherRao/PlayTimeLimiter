package tk.sherrao.bukkit.ptl;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import org.bukkit.configuration.file.YamlConfiguration;

import tk.sherrao.bukkit.ptl.listeners.PlayerDisconnectListener;
import tk.sherrao.bukkit.ptl.listeners.PlayerLoginListener;
import tk.sherrao.bukkit.ptl.time.TimedPlayerManager;
import tk.sherrao.bukkit.utils.plugin.SherPlugin;

public class PlayTimeLimiter extends SherPlugin {

	protected File dataFile;
	protected YamlConfiguration data;
	protected LocalDate date;
	
	protected TimedPlayerManager tpm;
	
	public PlayTimeLimiter() {
		super();
		
	}
	
	@Override
	public void onLoad() {
		super.onLoad();
		
	}
	
	@Override
	public void onEnable() {
		super.onEnable();
		
		dataFile = super.createFile( "data.yml" );
		data = YamlConfiguration.loadConfiguration( dataFile );
		date = LocalDate.now();
		
		if( data.contains( "last-date" ) ) 
			checkFileRefresh();
			
		data.set( "last-date", date.toString() );
		
		try {
			data.save( dataFile );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		
		super.registerAsyncRepeatingTask( tpm = new TimedPlayerManager( this ), 1, config.getInt( "timings.refresh-rate" ) );
		super.registerEventListener( new PlayerLoginListener( this ) );
		super.registerEventListener( new PlayerDisconnectListener( this ) );
		super.complete();
		
	}
	
	@Override
	public void onDisable() {
		super.onDisable();

	}
	
	private void checkFileRefresh() {
		String[] lastdDateString = data.getString( "last-date" ).split( "-" );
		LocalDate lastDate = LocalDate.of( 
				Integer.valueOf( lastdDateString[0] ), Integer.valueOf( lastdDateString[1] ), Integer.valueOf( lastdDateString[2] ) );	
		
		
		if( date.isAfter( lastDate ) ) {
			try {
				dataFile.delete();
				dataFile.createNewFile();
				log.info( "It's a new dawn of a new day! Refreshing data file for play-time (./data.yml)" );
				
			} catch ( IOException e ) { e.printStackTrace(); }
		
			data = YamlConfiguration.loadConfiguration( dataFile );
			
		}
	
	}
	
	public YamlConfiguration getData() {
		return data;

	}
	
	public File getDataFile() {
		return dataFile;
		
	}
	
	public LocalDate getDate() {
		return date;
		
	}
	
	public TimedPlayerManager getTimedPlayerManager() {
		return tpm;
		
	}
	
}