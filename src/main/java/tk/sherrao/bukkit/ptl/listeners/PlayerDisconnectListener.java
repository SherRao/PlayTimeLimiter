package tk.sherrao.bukkit.ptl.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import tk.sherrao.bukkit.ptl.PlayTimeLimiter;
import tk.sherrao.bukkit.ptl.time.TimedPlayerManager;
import tk.sherrao.bukkit.utils.plugin.SherEventListener;

public class PlayerDisconnectListener extends SherEventListener {

	protected TimedPlayerManager tpm;
	
	public PlayerDisconnectListener( PlayTimeLimiter pl ) {
		super(pl);
		
		this.tpm = pl.getTimedPlayerManager();
		
	}
	
	@EventHandler( priority = EventPriority.HIGH )
	public void onPlayerDisconnect( PlayerQuitEvent event ) {
		Player player = event.getPlayer();
		if( !player.hasPermission( "ptl.bypass" ) ) {
			tpm.writeInfo( player.getUniqueId(), player.getName() );
			tpm.removeCachedInfo( player.getUniqueId() );
		
		} else
			return;
		
	}
}
