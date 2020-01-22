package mitw.lobby.event;

import org.bukkit.event.player.*;
import org.bukkit.event.*;
import mitw.lobby.player.*;

public class LobbyProfileLoadedEvent extends PlayerEvent
{
    private static final HandlerList handlerlist;
    private LobbyPlayer lobbyPlayer;

    public static HandlerList getHandlerList() {
        return LobbyProfileLoadedEvent.handlerlist;
    }

    public HandlerList getHandlers() {
        return LobbyProfileLoadedEvent.handlerlist;
    }

    public LobbyProfileLoadedEvent(final LobbyPlayer lobbyPlayer) {
        super(lobbyPlayer.toBukkitPlayer());
        this.lobbyPlayer = lobbyPlayer;
    }

    public LobbyPlayer getLobbyPlayer() {
        return this.lobbyPlayer;
    }

    static {
        handlerlist = new HandlerList();
    }
}
