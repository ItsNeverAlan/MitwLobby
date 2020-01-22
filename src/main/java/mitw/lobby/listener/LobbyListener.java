package mitw.lobby.listener;

import mitw.lobby.gui.LanguageGUI;
import net.development.mitw.utils.ttl.*;
import java.util.concurrent.*;
import org.bukkit.entity.*;
import mitw.lobby.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.*;
import org.lgdev.iselector.api.events.ProfileLoadingEvent;
import org.spigotmc.event.player.*;
import org.bukkit.potion.*;
import org.bukkit.event.*;
import net.development.mitw.events.player.*;
import mitw.lobby.player.*;
import net.development.mitw.utils.*;
import mitw.lobby.util.*;
import net.development.mitw.language.*;
import org.lgdev.iselector.api.*;
import org.bukkit.*;
import net.development.mitw.events.*;
import net.development.mitw.*;
import org.bukkit.event.weather.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.block.*;
import org.lgdev.iselector.*;
import java.util.*;
import org.bukkit.event.entity.*;

public class LobbyListener implements Listener
{
    private TtlArrayList<UUID> list;

    public LobbyListener() {
        this.list = (TtlArrayList<UUID>)new TtlArrayList(TimeUnit.SECONDS, 3L);
    }

    @EventHandler
    public void onSpawn(final EntitySpawnEvent e) {
        if (e.getEntity().getType().equals((Object)EntityType.ARROW) || e.getEntity() instanceof Player) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onShot(final ProjectileHitEvent event) {
        if (event.getEntityType().equals((Object)EntityType.ARROW) && event.getEntity().getShooter() != null && event.getEntity().getShooter() instanceof Player) {
            final Player player = (Player)event.getEntity().getShooter();
            final Location location = event.getEntity().getLocation();
            location.setYaw(player.getLocation().getYaw());
            location.setPitch(player.getLocation().getPitch());
            player.teleport(location);
            event.getEntity().remove();
            player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onShot1(final EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            final Player player = (Player)event.getEntity();
            MitwLobby.getInstance().getServer().getScheduler().runTaskLater((Plugin)MitwLobby.getInstance(), () -> {
                event.getBow().setDurability((short)0);
                player.getInventory().setItem(9, new ItemStack(Material.ARROW));
                player.updateInventory();
                MitwLobby.getInstance().getServer().getScheduler().runTaskLater((Plugin)MitwLobby.getInstance(), () -> {
                    if (!event.getProjectile().isDead() || !event.getProjectile().isOnGround()) {
                        event.getProjectile().remove();
                    }
                }, 200L);
            }, 40L);
        }
    }

    @EventHandler
    public void onPlayerSpawnLocation(final PlayerSpawnLocationEvent event) {
        event.setSpawnLocation(MitwLobby.getInstance().getLocation());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(final PlayerJoinEvent event) {
        event.setJoinMessage((String)null);
        final Player player = event.getPlayer();
        player.setGameMode(GameMode.SURVIVAL);
        player.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(player::removePotionEffect);
        player.setWalkSpeed(0.3f);
        if (player.hasPermission("rank.vip")) {
            player.setAllowFlight(true);
            player.setFlySpeed(0.3f);
        }
        else {
            player.setAllowFlight(false);
        }
    }

    @EventHandler
    public void onPlayerFirstJoin(final PlayerFirstJoinEvent event) {
        new LanguageGUI().openMenu(event.getPlayer());
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        event.setQuitMessage((String)null);
        this.list.remove((Object)event.getPlayer().getUniqueId());
    }

    private void spawnItems(final Player player) {
        if (!LobbyPlayer.isLoaded(player.getUniqueId())) {
            return;
        }
        this.spawnItems(player, LobbyPlayer.getLobbyPlayer(player.getUniqueId()));
    }

    private void spawnItems(final Player player, final LobbyPlayer lobbyPlayer) {
        if (player == null) {
            return;
        }
        player.getInventory().clear();
        player.getInventory().setItem(0, new ItemBuilder(Material.COMPASS).name(MitwLobby.getInstance().getLanguage().translate(player, "server")).lore(new String[] { " ", MitwLobby.getInstance().getLanguage().translate(player, "server.lore") }).build());
        player.getInventory().setItem(1, new ItemBuilder(Material.NETHER_STAR).name(MitwLobby.getInstance().getLanguage().translate(player, "lobbies")).lore(new String[] { "", MitwLobby.getInstance().getLanguage().translate(player, "lobbies_lore") }).build());
        player.getInventory().setItem(4, new ItemBuilder(SkullCreator.itemFromName("0qt")).name(MitwLobby.getInstance().getLanguage().translate(player, "language")).lore(new String[] { "", MitwLobby.getInstance().getLanguage().translate(player, "lang.lore") }).build());
        if (lobbyPlayer.isShowPlayers()) {
            player.getInventory().setItem(5, new ItemBuilder(Material.INK_SACK).durability(10).name(MitwLobby.getInstance().getLanguage().translate(player, "hidePlayers")).lore(new String[] { "", MitwLobby.getInstance().getLanguage().translate(player, "hidePlayers_lore") }).build());
        }
        else {
            player.getInventory().setItem(5, new ItemBuilder(Material.INK_SACK).durability(8).name(MitwLobby.getInstance().getLanguage().translate(player, "showPlayers")).lore(new String[] { "", MitwLobby.getInstance().getLanguage().translate(player, "showPlayers_lore") }).build());
        }
        player.getInventory().setItem(8, new ItemBuilder(Material.BOW).name(MitwLobby.getInstance().getLanguage().translate(player, "bow")).lore(new String[] { " ", MitwLobby.getInstance().getLanguage().translate(player, "bow.lore") }).build());
        player.getInventory().setItem(9, new ItemStack(Material.ARROW));
        player.updateInventory();
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent e) {
        if (!e.getPlayer().getGameMode().equals((Object)GameMode.CREATIVE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent e) {
        if (!e.getPlayer().getGameMode().equals((Object)GameMode.CREATIVE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onLanguageChange(final ILanguageData.ChangeLanguageEvent e) {
        iSelectorAPI.setLanguage(e.getPlayer(), e.getLanguage());
        Bukkit.getScheduler().runTaskLater((Plugin)MitwLobby.getInstance(), () -> this.spawnItems(e.getPlayer()), 2L);
    }

    @EventHandler
    public void onLanguageLoaded(final LanguageLoadedEvent event) {
        final Player player = event.getPlayer();
        iSelectorAPI.setLanguage(player, event.getLanguage());
        this.spawnItems(player);
        LobbyPlayer lobbyPlayer = LobbyPlayer.getLobbyPlayer(player.getUniqueId());
        if (!lobbyPlayer.isShowPlayers()) {
            Bukkit.getOnlinePlayers().forEach(player1 -> {
                if (player != player1) {
                    player.hidePlayer(player1);
                    LobbyPlayer lobbyPlayer2 = LobbyPlayer.getLobbyPlayer(player1.getUniqueId());
                    if (lobbyPlayer2 != null && !lobbyPlayer2.isShowPlayers()) {
                        player1.hidePlayer(player);
                    }
                }
            });
        }
    }

    @EventHandler
    public void onDrop(final PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onClick(final InventoryClickEvent e) {
        if (!e.getWhoClicked().getGameMode().equals((Object)GameMode.CREATIVE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(final EntityDamageEvent e) {
        e.setCancelled(true);
        if (e.getCause().equals((Object)EntityDamageEvent.DamageCause.VOID)) {
            e.getEntity().teleport(MitwLobby.getInstance().getLocation());
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
        if (event.getAction().equals((Object)Action.PHYSICAL) && event.getClickedBlock().getType().equals((Object)Material.SOIL)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRightClick(final PlayerInteractEvent event) {
        if ((event.getAction().equals((Object)Action.RIGHT_CLICK_AIR) || event.getAction().equals((Object)Action.RIGHT_CLICK_BLOCK)) && !event.getPlayer().getGameMode().equals((Object)GameMode.CREATIVE)) {
            final ItemStack itemStack = event.getItem();
            final Player player = event.getPlayer();
            if (itemStack == null) {
                return;
            }
            if (itemStack.getType().equals((Object)Material.BOW)) {
                return;
            }
            if (this.list.contains((Object)player.getUniqueId())) {
                player.sendMessage("§cDon't spam items!");
                return;
            }
            this.list.add(player.getUniqueId());
            event.setCancelled(true);
            switch (itemStack.getType()) {
                case COMPASS: {
                    iSelector.getInstance().getSelectorManager().getSelector("servers").openMenu(player);
                    break;
                }
                case SKULL_ITEM: {
                    new LanguageGUI().openMenu(player);
                    break;
                }
                case NETHER_STAR: {
                    iSelector.getInstance().getSelectorManager().getSelector("lobbies").openMenu(player);
                    break;
                }
                case INK_SACK: {
                    final LobbyPlayer lobbyPlayer = LobbyPlayer.getLobbyPlayer(player.getUniqueId());
                    lobbyPlayer.setShowPlayers(!lobbyPlayer.isShowPlayers());
                    if (lobbyPlayer.isShowPlayers()) {
                        for (final Player player2 : Bukkit.getOnlinePlayers()) {
                            if (player != player2) {
                                player.showPlayer(player2);
                            }
                        }
                        player.sendMessage(MitwLobby.getInstance().getLanguage().translate(player, "playerShowed"));
                        player.getInventory().setItem(5, new ItemBuilder(Material.INK_SACK).durability(10).name(MitwLobby.getInstance().getLanguage().translate(player, "hidePlayers")).lore(new String[] { "", MitwLobby.getInstance().getLanguage().translate(player, "hidePlayers_lore") }).build());
                        break;
                    }
                    for (final Player player2 : Bukkit.getOnlinePlayers()) {
                        if (player != player2) {
                            player.hidePlayer(player2);
                        }
                    }
                    player.sendMessage(MitwLobby.getInstance().getLanguage().translate(player, "playerHided"));
                    player.getInventory().setItem(5, new ItemBuilder(Material.INK_SACK).durability(8).name(MitwLobby.getInstance().getLanguage().translate(player, "showPlayers")).lore(new String[] { "", MitwLobby.getInstance().getLanguage().translate(player, "showPlayers_lore") }).build());
                    break;
                }
            }
        }
        else if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onProfileLoading(final ProfileLoadingEvent event) {
        event.setLanguage(Mitw.getInstance().getLanguageData().getLang(event.getPlayer()));
    }
}
