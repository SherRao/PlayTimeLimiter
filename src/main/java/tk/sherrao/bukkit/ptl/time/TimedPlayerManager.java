package tk.sherrao.bukkit.ptl.time;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import tk.sherrao.bukkit.ptl.PlayTimeLimiter;
import tk.sherrao.bukkit.utils.plugin.SherTask;
import tk.sherrao.utils.collections.Pair;
import tk.sherrao.utils.strings.StringMultiJoiner;

public class TimedPlayerManager extends SherTask {

	private final class Lock {}
	private final Object lock = new Lock();
	
	protected YamlConfiguration data;
	protected Map<UUID, TimedPlayer> players;
	
	protected Map<Integer, Entry<String, Sound> > reminders;
	
	protected String kickMessage, annMessage;
	protected Sound kickSound, annSound;

	protected LocalDate date;
	protected int maxTimeAllowed;
	
	public TimedPlayerManager( PlayTimeLimiter pl ) {
		super(pl);
		
		this.data = pl.getData();
		this.players = Collections.synchronizedMap( new HashMap<>() );
		this.reminders = new HashMap<>();
		
		config.getIntegerList( "reminders.declare" ).forEach( k -> {
			String msg = config.getString( "reminders.messages." + k );
			Sound sound = config.getString( "reminders.sounds." + k ).equals( "null" ) ? null : config.getSound( "reminders.sounds." + k );
			reminders.put( k, new Pair<>( msg, sound ) );
			
		} );
		
		this.kickMessage = new StringMultiJoiner( "\n" )
				.add( config.getStringList( "messages.kick" ) )
				.toString();
		this.annMessage = config.getString( "messages.announce" );
		this.kickSound = config.getString( "sounds.kick" ).equals( "null" ) ? null : config.getSound( "sounds.kick" );
		this.annSound = config.getString( "sounds.announce" ).equals( "null" ) ? null : config.getSound( "sounds.announce" );

		this.date = pl.getDate();
		this.maxTimeAllowed = config.getInt( "timings.max-time-allowed" );
	
	}
	
	private void attemptKick( Player player ) {
		Bukkit.getScheduler().runTask( pl, () -> { 
			if( !kickMessage.equals( "null" ) ) 
				player.sendMessage( kickMessage.replace( "[player]", player.getName() ) );
				
			if( kickSound != null ) 
				player.playSound( player.getLocation(), kickSound, 1F, 1F );
					
			if( !annMessage.equals( "null" ) )
				Bukkit.broadcastMessage( annMessage.replace( "[player]", player.getName() ) );
				
			if( annSound != null )
				for( Player p : Bukkit.getOnlinePlayers() ) 
					p.playSound( p.getLocation(), annSound, 1F, 1F );
					
			player.kickPlayer( kickMessage ); 

		} );				
			
	}
	
	@Override
	public void fire() {
		Iterator< Entry<UUID, TimedPlayer> > it = players.entrySet().iterator();
		for( ;it.hasNext(); ) {
			Entry<UUID, TimedPlayer> entry = it.next();
			TimedPlayer tPlayer = entry.getValue();
			Player player = Bukkit.getPlayer( entry.getKey() );
			if( tPlayer.getTimePlayed() > maxTimeAllowed ) {
				attemptKick( player );
				
			} else {
				for( Entry<Integer, Entry<String, Sound> > rem : reminders.entrySet() ) {
					if( maxTimeAllowed - tPlayer.getTimePlayed() == rem.getKey() ) {
						String msg = rem.getValue().getKey();
						Sound sound = rem.getValue().getValue();
						if( !msg.equals( "null" ) ) 
							player.sendMessage( msg );
						
						if( sound != null )
							player.playSound( player.getLocation(), sound, 1F, 1F );
						
					} else
						continue;
					
				}
				
				tPlayer.incrementTimePlayed();
				System.out.println( tPlayer.getTimePlayed() );
				
			}
			
		}
	}

	public void loadInfo( UUID id ) {
		synchronized( lock ) {
			if( data.contains( "data." + id.toString()  + ".time" ) ) {
				TimedPlayer player = new TimedPlayer(id);
				player.setTimePlayed( data.getInt( "data." + id.toString() + ".time") );
				players.put( id, player );
			
			} else 
				players.put( id, new TimedPlayer(id) );
			
		}
	}
	
	public void writeInfo( UUID id, String name ) {
		synchronized( lock ) {
			TimedPlayer player = players.get(id);
			try {
				data.set( "data." + id.toString() + ".time", player.getTimePlayed() );
				data.set( "data." + id.toString() + ".ign", name );
				data.save( ((PlayTimeLimiter) pl).getDataFile() );

			} catch ( IOException e ) { log.warning( "I/O operation failed while attempting to write data to file for: " + name, e ); }
					
		}		
	}
	
	public void removeCachedInfo( UUID id ) {
		synchronized( lock ) {
			players.remove(id);
		
		}
	}
	
	public boolean shouldKick( UUID id ) {
		synchronized( lock ) {
			if( players.containsKey(id) ) {
				return players.get(id).getTimePlayed() > maxTimeAllowed;
		
			} else if( data.contains( "data." + id.toString() + ".time" ) ) {
				TimedPlayer tPlayer = new TimedPlayer(id);
				tPlayer.setTimePlayed( data.getInt( "data." + id.toString() + ".time" ) );
				players.put( id, tPlayer );
			
				return tPlayer.getTimePlayed() > maxTimeAllowed;
			
			} else {
				players.put( id, new TimedPlayer(id) );
				return 0 > maxTimeAllowed;
			
			}
		
		}
	}
	
	public TimedPlayer getPlayer( UUID id ) {
		return players.get(id);
		
	}
	
}