package bartbokkers.sellblocks.commands;

import bartbokkers.sellblocks.Config.CustomConfig;
import bartbokkers.sellblocks.SellBlocks;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SellBlock implements CommandExecutor {

    private SellBlocks plugin;

    public Map<Player, Player> adderMap = new HashMap<>();

    public SellBlock(SellBlocks plugin) {
        this.plugin = plugin;
        plugin.getCommand("sellblock").setExecutor(this);
        plugin.getCommand("sellblock").setTabCompleter(new TabCompleter());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if ((commandSender instanceof Player)) {
            Player p = (Player) commandSender;
            Economy eco = SellBlocks.getEconomy();
            CustomConfig.reload();
            plugin.reloadConfig();
            if (args.length == 0) {
                String string = CustomConfig.get().getString("commands.sellblock");
                p.sendMessage(string);
            } else if (args[0].equalsIgnoreCase("give") && !args[1].isEmpty() && !args[2].isEmpty()) {
                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer != null) {
                    if (!args[2].isEmpty()) {
                        try {
                            int intValue = Integer.parseInt(args[2]);
                            System.out.println("Input is an integer: " + intValue);
                        } catch (NumberFormatException e) {
                            System.out.println("Input is not an integer.");
                            return true;
                        }
                    }
                    ItemStack soulSand = new ItemStack(Material.SOUL_SAND, Integer.valueOf(args[2]));
                    ItemMeta itemMeta = soulSand.getItemMeta();
                    itemMeta.setDisplayName(targetPlayer.getDisplayName());
                    soulSand.setItemMeta(itemMeta);
                    targetPlayer.getInventory().addItem(soulSand);
                    String string = CustomConfig.get().getString("commands.sellblock give");
                    if (string.contains("[playername]")) {
                        String string1 = targetPlayer.getName();
                        string = string.replace("[playername]", string1);
                    }
                    p.sendMessage(string);
                }
            } else if (args.length > 0 && args[0].equalsIgnoreCase("add") && args.length == 2) {
                Player adder = p;
                Player addedPlayer = Bukkit.getPlayer(args[1]);
                if (addedPlayer != null) {
                    adderMap.put(addedPlayer, adder);
                    String string = CustomConfig.get().getString("commands.sellblock add");
                    if (string.contains("[playername]")) {
                        String string1 = args[1];
                        string = string.replace("[playername]", string1);
                    }
                    p.sendMessage(string);
                } else {
                    p.sendMessage("Player not found or not online.");
                }
                return true;
            } else if (args.length > 1 && args[0].equalsIgnoreCase("collect")) {
                Player collector = p;
                if (adderMap.containsKey(collector)) {
                    Player adder = adderMap.get(collector);
                    if (plugin.balance.containsKey(adder)) {
                        int collectedAmount = plugin.balance.get(adder);
                        if (collectedAmount==0 || !plugin.balance.containsKey(adder)){
                            p.sendMessage(CustomConfig.get().getString("errors.insufficient balance"));
                            return true;
                        }
                        EconomyResponse response = eco.depositPlayer(p, collectedAmount);
                        if (response.transactionSuccess()) {
                            String string = CustomConfig.get().getString("commands.sellblock collect player");
                            int balance123 = collectedAmount;
                            String string1 = args[1];
                            if (string.contains("[playername]") || (string.contains("[amount]"))) {
                                string = string.replace("[playername]", string1);
                                string = string.replace("[amount]", String.valueOf(balance123));
                            }
                            p.sendMessage(string);
                        }
                        plugin.balance.put(adder, 0);
                    }
                }
            } else if (args.length > 0 && args[0].equalsIgnoreCase("remove") && args.length == 2) {
                Player remover = p;
                Player removedPlayer = Bukkit.getPlayer(args[1]);

                if (removedPlayer != null) {
                    if (adderMap.containsKey(removedPlayer) && adderMap.get(removedPlayer).equals(remover)) {
                        adderMap.remove(removedPlayer);
                        String string = CustomConfig.get().getString("commands.sellblock remove");
                        if (string.contains("[playername]")) {
                            String string1 = removedPlayer.getName();
                            string = string.replace("[playername]", string1);
                        }
                        p.sendMessage(string);
                    } else {
                        p.sendMessage("You did not add " + removedPlayer.getName() + " to your collection list.");
                    }
                } else {
                    p.sendMessage("Invalid command usage or argument!");
                }
            }
            if (args[0].equalsIgnoreCase("collect") && args.length == 1) {
                if (!adderMap.containsValue(p)) {
                    int money_balance = plugin.balance.get(p);
                    if (money_balance==0 || !plugin.balance.containsKey(p)){
                        p.sendMessage(CustomConfig.get().getString("errors.insufficient balance"));
                        return true;
                    }
                    EconomyResponse response = eco.depositPlayer(p, money_balance);
                    if (response.transactionSuccess()) {
                        String string = CustomConfig.get().getString("commands.sellblock collect");
                        String string1 = p.getName();
                        if (string.contains("[playername]") || string.contains("[amount]")) {
                            string = string.replace("[playername]", string1);
                            string = string.replace("[amount]", String.valueOf(money_balance));
                        }
                        plugin.balance.put(p,0);
                        p.sendMessage(string);
                    }
                }

            }
        }
        return true;
    }
}
