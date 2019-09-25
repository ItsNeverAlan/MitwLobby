package mitw.lobby;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.lgdev.iselector.iSelector;
import org.lgdev.iselector.api.iSelectorAPI;
import org.lgdev.iselector.api.events.ProfileLoadingEvent;

import me.clip.placeholderapi.PlaceholderAPI;
import mitw.lobby.gui.LanguageGUI;
import mitw.lobby.scoreboard.ScoreHelper;
import net.development.mitw.Mitw;
import net.development.mitw.language.ILanguageData.ChangeLanguageEvent;
import net.development.mitw.language.LanguageAPI;
import net.development.mitw.language.LanguageAPI.LangType;
import net.development.mitw.utils.ItemBuilder;
import net.md_5.bungee.api.ChatColor;

public class MitwLobby extends JavaPlugin implements Listener, CommandExecutor, Runnable {

	private Location loc;

	public LanguageAPI lang;
	public static MitwLobby ins;
	protected List<String> countdown = new ArrayList<>();

	@Override
	public void onEnable() {
		ins = this;
		for (final Chunk chunk : Bukkit.getWorld("world").getLoadedChunks()) {
			for (final Entity entity : chunk.getEntities()) {
				entity.remove();
			}
		}
		Bukkit.getPluginManager().registerEvents(this, this);
		saveDefaultConfig();
		getConfig().options().copyDefaults();
		saveConfig();
		this.lang = new LanguageAPI(LangType.CLASS, this, Mitw.getInstance().getLanguageData(), new Lang());
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Basic plugin enabled!");
		loc = Utils.stringToLocModes(getConfig().getString("spawn"));
		final World world = Bukkit.getWorld("world");
		world.setAutoSave(false);
		world.setDifficulty(Difficulty.PEACEFUL);
		world.setPVP(false);
		getServer().getScheduler().runTaskTimerAsynchronously(this, this, 20L, 20L);
	}

	@Override
	public void onDisable() {
		saveConfig();
		Bukkit.getScheduler().cancelTasks(this);
	}

	public void scoreboard(final Player p) {
		ScoreHelper.createScore(p).setTitle("&6&lMitw&f&l Network");
	}

	@EventHandler
	public void onSpawn(final EntitySpawnEvent e) {
		if (e.getEntity().getType().equals(EntityType.ARROW) || e.getEntity() instanceof Player)
			return;
		e.setCancelled(true);
	}

	@EventHandler
	public void onShot(final ProjectileHitEvent e) {
		if (e.getEntityType().equals(EntityType.ARROW) && e.getEntity().getShooter() != null
				&& e.getEntity().getShooter() instanceof Player) {
			final Player p = (Player) e.getEntity().getShooter();
			final Location loc = e.getEntity().getLocation();
			loc.setYaw(p.getLocation().getYaw());
			loc.setPitch(p.getLocation().getPitch());
			p.teleport(loc);
			e.getEntity().remove();
			p.playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1f);
		}
	}

	@EventHandler
	public void onShot1(final EntityShootBowEvent e) {
		if (e.getEntity() instanceof Player) {
			final Player p = (Player) e.getEntity();
			Bukkit.getScheduler().runTaskLater(this, () -> {
				e.getBow().setDurability((short) 0);
				p.getInventory().setItem(9, new ItemStack(Material.ARROW));
				p.updateInventory();
				Bukkit.getScheduler().runTaskLater(ins, () -> {
					if (e.getProjectile() != null || !e.getProjectile().isDead() || !e.getProjectile().isOnGround()) {
						e.getProjectile().remove();
					}
				}, 200l);
			}, 40l);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(final PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		e.setJoinMessage(null);
		scoreboard(p);
		p.setGameMode(GameMode.SURVIVAL);
		p.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(p::removePotionEffect);
		p.setWalkSpeed(0.3f);
		spawnItems(p);
		for (final Player p1 : Bukkit.getOnlinePlayers()) {
			if (p != p1) {
				p.hidePlayer(p1);
				p1.hidePlayer(p);
			}
		}
		if (p.hasPermission("rank.vip")) {
			p.setAllowFlight(true);
			p.setFlySpeed(0.3f);
		} else {
			p.setAllowFlight(false);
		}
		p.teleport(loc);
		Bukkit.getScheduler().runTaskLater(this, () -> {
			if (!p.hasPlayedBefore()) {
				new LanguageGUI().openMenu(p);
			}
		}, 10l);
	}

	public void spawnItems(final Player p) {
		if (p == null)
			return;
		p.getInventory().clear();
		p.getInventory().setItem(0, ItemStackBuilder.createItem1(Material.COMPASS, 1, 0, lang.translate(p, "server"),
				" ", lang.translate(p, "server.lore")));
		p.getInventory().setItem(4, new ItemBuilder(SkullCreator.itemFromName("0qt")).name(lang.translate(p, "language"))
				.lore("", lang.translate(p, "lang.lore")).build());
		p.getInventory().setItem(8, ItemStackBuilder.createItem1(Material.BOW, 1, 0, lang.translate(p, "bow"), " ",
				lang.translate(p, "bow.lore")));
		p.getInventory().setItem(9, new ItemStack(Material.ARROW));
		p.updateInventory();
	}

	@EventHandler
	public void onBlockBreak(final BlockBreakEvent e) {
		if (!e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent e) {
		if (!e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onLanguageChange(final ChangeLanguageEvent e) {
		if (Mitw.getInstance().getLanguageData().hasLang(e.getPlayer())) {
			final String from = Mitw.getInstance().getLanguageData().getLang(e.getPlayer());
			if (from.equals(e.getLanguage()))
				return;
		}
		iSelectorAPI.setLanguage(e.getPlayer(), e.getLanguage());
		Bukkit.getScheduler().runTaskLater(this, () -> spawnItems(e.getPlayer()), 2l);
	}

	@EventHandler
	public void onQuit(final PlayerQuitEvent e) {
		final Player p = e.getPlayer();
		ScoreHelper.removeScore(p);
	}

	@EventHandler
	public void onDrop(final PlayerDropItemEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onClick(final InventoryClickEvent e) {
		if (!e.getWhoClicked().getGameMode().equals(GameMode.CREATIVE)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onDamage(final EntityDamageEvent e) {
		e.setCancelled(true);
		if (e.getCause().equals(DamageCause.VOID)) {
			e.getEntity().teleport(loc);
		}
	}

	@EventHandler
	public void onWeatherChange(final WeatherChangeEvent e) {
		if (e.toWeatherState()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onFood(final FoodLevelChangeEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onCraft(final CraftItemEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onBoom(final EntityExplodeEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void noUproot(final PlayerInteractEvent event) {
		if (event.getAction().equals(Action.PHYSICAL) && event.getClickedBlock().getType().equals(Material.SOIL)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onRightClick(final PlayerInteractEvent e) {
		if ((e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
				&& !e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
			final ItemStack item = e.getItem();
			final Player p = e.getPlayer();
			if (item == null)
				return;
			if (item.getType().equals(Material.BOW))
				return;
			e.setCancelled(true);
			if (item.getType().equals(Material.COMPASS)) {
				iSelector.getInstance().getSelectorManager().getSelector("servers").openMenu(p);
			} else if (item.getType().equals(Material.SKULL_ITEM)) {
				new LanguageGUI().openMenu(p);
			}
		} else if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
			e.setCancelled(true);
		}
	}

	@EventHandler( priority = EventPriority.HIGH)
	public void onCreatureSpawn(final CreatureSpawnEvent event) {
		if (event.getEntity() instanceof Player)
			return;
		event.setCancelled(true);
	}



	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (!(sender instanceof Player))
			return true;
		final Player p = (Player) sender;
		if (cmd.getName().equalsIgnoreCase("lang")) {
			new LanguageGUI().openMenu(p);
			return true;
		} else if (!sender.hasPermission("mitw.admin"))
			return true;
		getConfig().set("spawn", Utils.locationToStringModes(p.getLocation()));
		saveConfig();
		return false;
	}

	@Override
	public void run() {
		for (final Player player : this.getServer().getOnlinePlayers()) {
			final List<String> str = new ArrayList<>();
			for (String s : lang.translateArrays(player, "sidebar")) {
				if (s.contains("<bungee_count>")) {
					s = s.replaceAll("<bungee_count>", PlaceholderAPI.setPlaceholders(player, "%bungee_total%"));
				}
				if (s.contains("<ping>")) {
					s = s.replaceAll("<ping>", player.spigot().getPing() + "");
				}
				str.add(s);
			}
			ScoreHelper.getByPlayer(player).setSlotsFromList(str);
		}
	}

	@EventHandler
	public void onProfileLoading(final ProfileLoadingEvent event) {
		event.setLanguage(Mitw.getInstance().getLanguageData().getLang(event.getPlayer()));
	}

}
