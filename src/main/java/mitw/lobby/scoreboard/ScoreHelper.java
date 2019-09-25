package mitw.lobby.scoreboard;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 *
 * @author crisdev333
 *
 */
public class ScoreHelper {

	private static HashMap<UUID, ScoreHelper> players = new HashMap<>();

	public static boolean hasScore(Player player) {
		return players.containsKey(player.getUniqueId());
	}

	public static ScoreHelper createScore(Player player) {
		return new ScoreHelper(player);
	}

	public static ScoreHelper getByPlayer(Player player) {
		return players.get(player.getUniqueId());
	}

	public static ScoreHelper removeScore(Player player) {
		return players.remove(player.getUniqueId());
	}

	private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	private Objective sidebar;

	private ScoreHelper(Player player) {
		sidebar = scoreboard.registerNewObjective("sidebar", "dummy");
		sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
		// Create Teams
		for (int i = 1; i <= 15; i++) {
			Team team = scoreboard.registerNewTeam("SLOT_" + i);
			team.addEntry(genEntry(i));
		}
		player.setScoreboard(scoreboard);
		players.put(player.getUniqueId(), this);
	}

	public void setTitle(String title) {
		title = ChatColor.translateAlternateColorCodes('&', title);
		sidebar.setDisplayName(title.length() > 32 ? title.substring(0, 32) : title);
	}

	public void setSlot(int slot, String text) {
		Team team = scoreboard.getTeam("SLOT_" + slot);
		String entry = genEntry(slot);
		if (!scoreboard.getEntries().contains(entry)) {
			sidebar.getScore(entry).setScore(slot);
		}

		text = ChatColor.translateAlternateColorCodes('&', text);
		String[] ts = this.splitStringLine(text);
		team.setPrefix(ts[0]);
		team.setSuffix(ts[1]);
	}

	public void removeSlot(int slot) {
		String entry = genEntry(slot);
		if (scoreboard.getEntries().contains(entry)) {
			scoreboard.resetScores(entry);
		}
	}

	public void setSlotsFromList(List<String> list) {
		while (list.size() > 15) {
			list.remove(list.size() - 1);
		}

		int slot = list.size();

		if (slot < 15) {
			for (int i = (slot + 1); i <= 15; i++) {
				removeSlot(i);
			}
		}

		for (String line : list) {
			setSlot(slot, line);
			slot--;
		}
	}

	private String genEntry(int slot) {
		return ChatColor.values()[slot].toString();
	}

	private String[] splitStringLine(String string) {

		StringBuilder prefix = new StringBuilder(string.substring(0, string.length() >= 16 ? 16 : string.length()));
		StringBuilder suffix = new StringBuilder(string.length() > 16 ? string.substring(16) : "");

		if ((prefix.toString().length() > 1) && (prefix.charAt(prefix.length() - 1) == '��')) {
			prefix.deleteCharAt(prefix.length() - 1);
			suffix.insert(0, '��');
		}

		String last = "";

		int i = 0;

		while (i < prefix.toString().length()) {

			char c = prefix.toString().charAt(i);

			if (c == '��') {
				if (i < prefix.toString().length() - 1) {
					last = last + "��" + prefix.toString().charAt(i + 1);
				}
			}

			++i;
		}

		String s2 = "" + suffix;

		if (prefix.length() > 14) {
			s2 = !last.isEmpty() ? String.valueOf(last) + s2 : "��" + s2;
		}

		return new String[] { prefix.toString().length() > 16 ? prefix.toString().substring(0, 16) : prefix.toString(),

				s2.toString().length() > 16 ? s2.toString().substring(0, 16) : s2.toString() };

	}

}