package mitw.lobby;

import lombok.Getter;
import mitw.lobby.scoreboard.Frame;
import mitw.lobby.scoreboard.adapter.LobbyAdapter;
import mitw.lobby.util.Lang;
import mitw.lobby.util.SkullCreator;
import mitw.lobby.util.Utils;
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
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.lgdev.iselector.iSelector;
import org.lgdev.iselector.api.iSelectorAPI;
import org.lgdev.iselector.api.events.ProfileLoadingEvent;

import mitw.lobby.gui.LanguageGUI;
import net.development.mitw.Mitw;
import net.development.mitw.language.ILanguageData.ChangeLanguageEvent;
import net.development.mitw.language.LanguageAPI;
import net.development.mitw.language.LanguageAPI.LangType;
import net.development.mitw.utils.ItemBuilder;
import net.md_5.bungee.api.ChatColor;

public class MitwLobby extends JavaPlugin implements Listener, CommandExecutor {

	private Location location;

	public LanguageAPI language;
	@Getter
	private static MitwLobby instance;

	@Override
	public void onEnable() {
		instance = this;
		for (final Chunk chunk : Bukkit.getWorld("world").getLoadedChunks()) {
			for (final Entity entity : chunk.getEntities()) {
				entity.remove();
			}
		}
		Bukkit.getPluginManager().registerEvents(this, this);
		saveDefaultConfig();
		getConfig().options().copyDefaults();
		saveConfig();
		this.language = new LanguageAPI(LangType.CLASS, this, Mitw.getInstance().getLanguageData(), new Lang());
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Mitw lobby plugin enabled!");
		location = Utils.stringToLocModes(getConfig().getString("spawn"));
		final World world = Bukkit.getWorld("world");
		world.setAutoSave(false);
		world.setDifficulty(Difficulty.PEACEFUL);
		world.setPVP(false);
		new Frame(this, new LobbyAdapter());
	}

	@Override
	public void onDisable() {
		saveConfig();
		Bukkit.getScheduler().cancelTasks(this);
	}

	@EventHandler
	public void onSpawn(final EntitySpawnEvent e) {
		if (e.getEntity().getType().equals(EntityType.ARROW) || e.getEntity() instanceof Player) {
            return;
        }
		e.setCancelled(true);
	}

	@EventHandler
	public void onShot(final ProjectileHitEvent event) {
		if (event.getEntityType().equals(EntityType.ARROW) && event.getEntity().getShooter() != null
				&& event.getEntity().getShooter() instanceof Player) {
			final Player player = (Player) event.getEntity().getShooter();
			final Location location = event.getEntity().getLocation();
			location.setYaw(player.getLocation().getYaw());
			location.setPitch(player.getLocation().getPitch());
			player.teleport(location);
			event.getEntity().remove();
			player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1f);
		}
	}

	@EventHandler
	public void onShot1(final EntityShootBowEvent event) {
		if (event.getEntity() instanceof Player) {
			final Player player = (Player) event.getEntity();
			this.getServer().getScheduler().runTaskLater(this, () -> {
				event.getBow().setDurability((short) 0);
				player.getInventory().setItem(9, new ItemStack(Material.ARROW));
				player.updateInventory();
				this.getServer().getScheduler().runTaskLater(instance, () -> {
					if (!event.getProjectile().isDead() || !event.getProjectile().isOnGround()) {
						event.getProjectile().remove();
					}
				}, 200L);
			}, 40L);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(final PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		e.setJoinMessage(null);
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
		p.teleport(location);
		Bukkit.getScheduler().runTaskLater(this, () -> {
			if (!p.hasPlayedBefore()) {
				new LanguageGUI().openMenu(p);
			}
		}, 10l);
	}

	private void spawnItems(final Player player) {
		if (player == null) {
			return;
		}
		player.getInventory().clear();
		player.getInventory().setItem(0, new ItemBuilder(Material.COMPASS).name(language.translate(player, "server")).lore(" ", language.translate(player, "server.lore")).build());
		player.getInventory().setItem(4, new ItemBuilder(SkullCreator.itemFromName("0qt")).name(language.translate(player, "language")).lore("", language.translate(player, "lang.lore")).build());
		player.getInventory().setItem(8, new ItemBuilder(Material.BOW).name(language.translate(player, "bow")).lore(" ", language.translate(player, "bow.lore")).build());
		player.getInventory().setItem(9, new ItemStack(Material.ARROW));
		player.updateInventory();
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
			if (from.equals(e.getLanguage())) {
				return;
			}
		}
		iSelectorAPI.setLanguage(e.getPlayer(), e.getLanguage());
		Bukkit.getScheduler().runTaskLater(this, () -> spawnItems(e.getPlayer()), 2l);
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
			e.getEntity().teleport(location);
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
	public void onRightClick(final PlayerInteractEvent event) {
		if ((event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
				&& !event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
			final ItemStack itemStack = event.getItem();
			final Player player = event.getPlayer();
			if (itemStack == null) {
				return;
			}
			if (itemStack.getType().equals(Material.BOW)) {
				return;
			}
			event.setCancelled(true);
			if (itemStack.getType().equals(Material.COMPASS)) {
				iSelector.getInstance().getSelectorManager().getSelector("servers").openMenu(player);
			} else if (itemStack.getType().equals(Material.SKULL_ITEM)) {
				new LanguageGUI().openMenu(player);
			}
		} else if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
			event.setCancelled(true);
		}
	}

	@EventHandler( priority = EventPriority.HIGH)
	public void onCreatureSpawn(final CreatureSpawnEvent event) {
		if (event.getEntity() instanceof Player) {
			return;
		}
		event.setCancelled(true);
	}



	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		final Player p = (Player) sender;
		if (cmd.getName().equalsIgnoreCase("lang")) {
			new LanguageGUI().openMenu(p);
			return true;
		} else if (sender.hasPermission("mitw.admin")) {
			getConfig().set("spawn", Utils.locationToStringModes(p.getLocation()));
			saveConfig();
		}
		return false;
	}

	@EventHandler
	public void onProfileLoading(final ProfileLoadingEvent event) {
		event.setLanguage(Mitw.getInstance().getLanguageData().getLang(event.getPlayer()));
	}

}
