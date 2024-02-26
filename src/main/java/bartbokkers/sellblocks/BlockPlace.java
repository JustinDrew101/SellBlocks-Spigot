package bartbokkers.sellblocks;

import bartbokkers.sellblocks.Config.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BlockPlace implements Listener {

    private SellBlocks plugin;

    public BlockPlace(SellBlocks plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public Map<Player, BukkitTask> playerTasks = new HashMap<Player, BukkitTask>();
    public Map<Player, BukkitTask> holoTasks = new HashMap<Player, BukkitTask>();
    public Map<Location, Player> placedBlocks = new HashMap<Location, Player>();
    public Map<Player, TextDisplay> holograms = new HashMap<Player, TextDisplay>();

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        plugin.reloadConfig();
        CustomConfig.reload();
        Block placedBlock = e.getBlockPlaced();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasDisplayName()) {
            ItemMeta itemMeta = itemInHand.getItemMeta();
            String itemName = itemMeta.getDisplayName();
            if (itemName.equalsIgnoreCase(player.getName())) {
                if (placedBlocks.containsValue(player)){
                    player.sendMessage(CustomConfig.get().getString("errors.already placed"));
                    e.setCancelled(true);
                    return;
                }
                if (CustomConfig.get().getString("block_place.on place") != null) {
                    player.sendMessage(CustomConfig.get().getString("block_place.on place"));
                }
                plugin.balance.put(player, 0);
                placedBlocks.put(placedBlock.getLocation(),player);
                playerTasks.put(placedBlocks.get(placedBlock.getLocation()), new BukkitRunnable() {
                    public void run() {
                        checkItems(placedBlock.getLocation());
                    }
                }.runTaskTimer(plugin, 5*20L, 5*20L));
                new BukkitRunnable(){
                    @Override
                    public void run(){
                        hologram(placedBlock.getLocation());
                    }
                }.runTaskTimer(plugin, 0,30*20);
            }
        }
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        if (!playerTasks.get(p).isCancelled()) {
            playerTasks.get(p).cancel();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
            for (Map.Entry<Location, Player> entry : placedBlocks.entrySet()) {
                if (entry.getValue().equals(p)) {
                    playerTasks.put(p, new BukkitRunnable() {
                        public void run() {
                            checkItems(entry.getKey());
                            hologram(entry.getKey());
                        }
                    }.runTaskTimer(plugin, 5 * 20L, 5 * 20L));
                }
            }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Player p = e.getPlayer();
        if (e.getBlock().getType().equals(Material.SOUL_SAND)) {
            if (placedBlocks.containsValue(p) && placedBlocks.containsKey(e.getBlock().getLocation())) {
                playerTasks.get(p).cancel();
                playerTasks.remove(p);
                placedBlocks.remove(e.getBlock().getLocation());
                holograms.get(p).remove();
                holograms.remove(p);
                holoTasks.get(p).cancel();
                holoTasks.remove(p);
            } else {
                for(Map.Entry<Location, Player> entry: placedBlocks.entrySet()) {
                    System.out.println(entry.getKey());
                    System.out.println(entry.getValue());
                }
                e.setCancelled(true);
            }
        }
    }

    private void hologram(Location location){
        plugin.reloadConfig();
        Player player = placedBlocks.get(location);
        Collection<Location> values = placedBlocks.keySet();
        holoTasks.put(player, new BukkitRunnable() {
            public void run() {
                for (Location value : values) {
                    if (!plugin.balance.containsKey(player)){
                        plugin.balance.put(player, 0);
                    }
                    if (holograms.containsKey(placedBlocks.get(value))){
                        String string = CustomConfig.get().getString("holograms.text");
                        if (string.contains("[balance]")){
                            int str_balance = plugin.balance.get(player);
                            string = string.replace("[balance]", String.valueOf(str_balance));
                        }
                        holograms.get(placedBlocks.get(value)).setText(string);
                    }else{
                        holograms.put(placedBlocks.get(value),location.getWorld().spawn(location.add(0,1,0), TextDisplay.class, display -> {

                            String string = CustomConfig.get().getString("holograms.text");
                            if (string.contains("[balance]")){
                                int str_balance = plugin.balance.get(player);
                                string = string.replace("[balance]", String.valueOf(str_balance));
                            }
                            display.setText(string);
                            display.setBillboard(Display.Billboard.CENTER);
                        }));
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 30*20L));
    }

    private void checkItems(Location location){
        FileConfiguration config = plugin.getConfig();
        for (Entity entity : location.getWorld().getNearbyEntities(location,25, 25, 25)) {
            if (entity instanceof Item) {
                Item droppedItem = (Item) entity;
                ItemStack itemStack = droppedItem.getItemStack();
                int quantity = itemStack.getAmount();
                Material itemType = itemStack.getType();
                if (plugin.getConfig().getConfigurationSection("items").getKeys(false).contains(itemType.name().toUpperCase())){
                    int price = config.getInt("items." + itemType.name().toUpperCase() + ".price", 0);
                    plugin.balance.put(placedBlocks.get(location), plugin.balance.get(placedBlocks.get(location)) + price*quantity);
                    plugin.getLogger().info(placedBlocks.get(location).toString());
                    droppedItem.remove();
                }
            }
        }
    }
}
