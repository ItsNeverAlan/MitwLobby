package mitw.lobby.player;

import net.development.mitw.database.*;
import net.development.mitw.shaded.mongodb.client.*;
import org.bson.*;
import java.util.*;
import net.development.mitw.shaded.mongodb.client.model.*;

public class LobbyMongo extends MongoDB
{
    private MongoCollection<Document> players;

    public LobbyMongo() {
        super("lobby");
        this.players = (MongoCollection<Document>)this.getDatabase().getCollection("players");
    }

    public Document getPlayer(final UUID uuid) {
        return (Document)this.players.find(Filters.eq("uuid", uuid.toString())).first();
    }

    public void replacePlayer(final UUID uuid, final Document document) {
        this.players.replaceOne(Filters.eq("uuid", uuid.toString()), document, this.replaceOptions());
    }
}
