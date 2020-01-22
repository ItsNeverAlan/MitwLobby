package mitw.lobby;

import mitw.lobby.gui.LanguageGUI;
import mitw.lobby.scoreboard.Frame;
import mitw.lobby.scoreboard.adapter.LobbyAdapter;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.*;
import net.development.mitw.language.*;
import org.bukkit.plugin.*;
import net.development.mitw.*;
import net.md_5.bungee.api.*;
import mitw.lobby.util.*;
import mitw.lobby.player.*;
import mitw.lobby.listener.*;
import java.util.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.lgdev.iselector.api.events.*;
import org.bukkit.event.*;

public class MitwLobby extends JavaPlugin implements CommandExecutor
{
	private Location location;
	public LanguageAPI language;
	private static MitwLobby instance;
	private LobbyMongo mongo;

	public void onEnable() {
		MitwLobby.instance = this;
		for (final Chunk chunk : Bukkit.getWorld("world").getLoadedChunks()) {
			for (final Entity entity : chunk.getEntities()) {
				entity.remove();
			}
		}
		Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)this);
		this.saveDefaultConfig();
		this.getConfig().options().copyDefaults();
		this.saveConfig();
		this.language = new LanguageAPI(LanguageAPI.LangType.CLASS, (Plugin)this, Mitw.getInstance().getLanguageData(), (Object)new Lang());
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Mitw lobby plugin enabled!");
		this.location = Utils.stringToLocModes(this.getConfig().getString("spawn"));
		this.mongo = new LobbyMongo();
		Arrays.asList(new PlayerListener(), new LobbyListener()).forEach(listener -> this.getServer().getPluginManager().registerEvents(listener, (Plugin)this));
		final World world = Bukkit.getWorld("world");
		world.setAutoSave(false);
		world.setDifficulty(Difficulty.PEACEFUL);
		world.setPVP(false);
		new Frame(this, new LobbyAdapter());
	}

	public void onDisable() {
		this.saveConfig();
		Bukkit.getScheduler().cancelTasks((Plugin)this);
	}

	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		final Player p = (Player) sender;
		if (cmd.getName().equalsIgnoreCase("lang")) {
			new LanguageGUI().openMenu(p);
			return true;
		}
		if (sender.hasPermission("mitw.admin")) {
			this.getConfig().set("spawn", (Object) Utils.locationToStringModes(p.getLocation()));
			this.saveConfig();
		}
		return false;
	}

	public Location getLocation() {
		return this.location;
	}

	public LanguageAPI getLanguage() {
		return this.language;
	}

	public LobbyMongo getMongo() {
		return this.mongo;
	}

	public static MitwLobby getInstance() {
		return MitwLobby.instance;
	}
}
