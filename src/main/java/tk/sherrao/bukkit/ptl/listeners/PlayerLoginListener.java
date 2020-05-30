package tk.sherrao.bukkit.ptl.listeners;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;

import tk.sherrao.bukkit.ptl.PlayTimeLimiter;
import tk.sherrao.bukkit.ptl.time.TimedPlayerManager;
import tk.sherrao.bukkit.utils.plugin.SherEventListener;
import tk.sherrao.utils.strings.StringMultiJoiner;

public class PlayerLoginListener extends SherEventListener {

	protected TimedPlayerManager tpm;
	protected String kickMessage;
	
	public PlayerLoginListener( PlayTimeLimiter pl ) {
		super(pl);
		
		this.tpm = pl.getTimedPlayerManager();
		this.kickMessage = new StringMultiJoiner( "\n" )
				.add( config.getStringList( "messages.kick" ) )
				.toString();
		
	}
	

	@EventHandler( priority = EventPriority.HIGHEST )
	public void onPlayerLogin( PlayerLoginEvent event ) {
		Player player = event.getPlayer();
		UUID id = player.getUniqueId();

		/** If the player can bypass the Limiter */
		if( player.hasPermission( "ptl.bypass" ) ) 
			return;
			
		else {
			if( tpm.shouldKick(id) ) {
				tpm.removeCachedInfo(id);
				log.info( "Player with UUID (" + id + ") tried connected with max play-time surpased"  );

				if( !kickMessage.equals( "null" ) )
					event.disallow( PlayerLoginEvent.Result.KICK_OTHER, kickMessage.replace( "[player]", event.getPlayer().getName() ) );
				
				else
					event.disallow( PlayerLoginEvent.Result.KICK_OTHER, null );
				
				
			} else
				tpm.loadInfo(id);
				
		}
	}
	
}