package mitw.lobby.scoreboard.adapter;

import me.clip.placeholderapi.PlaceholderAPI;
import mitw.lobby.MitwLobby;
import mitw.lobby.scoreboard.FrameAdapter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LobbyAdapter implements FrameAdapter {

    @Override
    public String getTitle(Player player) {
        return "§6§lMitw §f§lNetwork";
    }

    @Override
    public List<String> getLines(Player player) {
        List<String> lines = new ArrayList<>();

        for (String s : MitwLobby.getInstance().language.translateArrays(player, "sidebar")) {
            if (s.contains("<bungee_count>")) {
                s = s.replaceAll("<bungee_count>", PlaceholderAPI.setPlaceholders(player, "%bungee_total%"));
            }
            if (s.contains("<ping>")) {
                s = s.replaceAll("<ping>", player.spigot().getPing() + "");
            }
            lines.add(s);
        }

        return lines;
    }
}
