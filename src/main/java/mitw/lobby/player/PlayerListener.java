package mitw.lobby.player;

import java.util.*;
import org.bukkit.event.*;
import org.bukkit.entity.*;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener
{
    @EventHandler
    public void onAsyncPlayerPreLogin(final AsyncPlayerPreLoginEvent event) {
        final UUID uuid = event.getUniqueId();
        final String name = event.getName();
        final LobbyPlayer lobbyPlayer = new LobbyPlayer(uuid, name);
        lobbyPlayer.load();
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final LobbyPlayer lobbyPlayer = LobbyPlayer.getLobbyPlayer(player.getUniqueId());
        lobbyPlayer.save();
    }
}
