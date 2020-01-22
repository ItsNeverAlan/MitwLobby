package mitw.lobby.player;

import net.development.mitw.player.*;
import org.bukkit.entity.*;
import mitw.lobby.*;
import org.bson.*;
import java.util.*;

public class LobbyPlayer extends PlayerInfo
{
    private static Map<UUID, LobbyPlayer> lobbyPlayers;
    private boolean showPlayers;

    public static LobbyPlayer getLobbyPlayer(final UUID uuid) {
        return LobbyPlayer.lobbyPlayers.get(uuid);
    }

    public static boolean isLoaded(final UUID uuid) {
        return LobbyPlayer.lobbyPlayers.containsKey(uuid);
    }

    public LobbyPlayer(final Player player) {
        super(player);
        this.showPlayers = true;
        LobbyPlayer.lobbyPlayers.put(this.getUuid(), this);
    }

    public LobbyPlayer(final UUID uuid, final String name) {
        super(uuid, name);
        this.showPlayers = true;
        LobbyPlayer.lobbyPlayers.put(this.getUuid(), this);
    }

    public void load() {
        final Document document = MitwLobby.getInstance().getMongo().getPlayer(this.getUuid());
        if (document == null) {
            return;
        }
        this.showPlayers = document.getBoolean((Object)"showPlayers");
    }

    public void save() {
        final Document document = new Document();
        document.put("uuid", (Object)this.getUuid().toString());
        document.put("showPlayers", (Object)this.isShowPlayers());
        MitwLobby.getInstance().getMongo().replacePlayer(this.getUuid(), document);
    }

    public boolean isShowPlayers() {
        return this.showPlayers;
    }

    public void setShowPlayers(final boolean showPlayers) {
        this.showPlayers = showPlayers;
    }

    public static Map<UUID, LobbyPlayer> getLobbyPlayers() {
        return LobbyPlayer.lobbyPlayers;
    }

    static {
        LobbyPlayer.lobbyPlayers = new HashMap<UUID, LobbyPlayer>();
    }
}
