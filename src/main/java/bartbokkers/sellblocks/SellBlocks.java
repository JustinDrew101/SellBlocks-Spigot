package bartbokkers.sellblocks;

import bartbokkers.sellblocks.Config.*;
import bartbokkers.sellblocks.commands.SellBlock;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;



public final class SellBlocks extends JavaPlugin {


    private static Economy econ = null;
    public Map<Player,Integer> balance = new HashMap<Player, Integer>();

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        CustomConfig.setup();
        CustomConfig.get().addDefault("commands.sellblock", "SellBlocks Help:\n1: /sellblock give [PlayerName] [Amount] - Give Special Block to the person \n2: /sellblock add [PlayerName] - To add player that can also withdraw the Money from the block \n3: /sellblock remove [PlayerName] - To remove the player that could also withdraw the Money from the block \n4: /sellblock collect - To collect the money of the owner who added you.");
        CustomConfig.get().addDefault("commands.sellblock give","Soul Sand added to [playername] inventory.");
        CustomConfig.get().addDefault("commands.sellblock add","Added [playername] to your balance collectors list.");
        CustomConfig.get().addDefault("commands.sellblock remove","Removed [playername] from your balance collectors list.");
        CustomConfig.get().addDefault("commands.sellblock collect player","Collected [amount] from [playername] balance.");
        CustomConfig.get().addDefault("commands.sellblock collect","Successfully Collected [amount].");
        CustomConfig.get().addDefault("block_place.on place","Your Special Block Has Been Placed!");
        CustomConfig.get().addDefault("errors.insufficient balance","Insufficient balance.");
        CustomConfig.get().addDefault("errors.already placed","You already have an active block.");
        CustomConfig.get().addDefault("holograms.text","balance [balance]");
        CustomConfig.get().options().copyDefaults(true);
        CustomConfig.save();
        new SellBlock(this);
        new BlockPlace(this);
        getLogger().info("SellBlocks Plugin has been started!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy(){
        return econ;
    }

    @Override
    public void onDisable(){
        Economy eco = SellBlocks.getEconomy();
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (balance == null){
                return;
            }
            EconomyResponse response = eco.depositPlayer(p, balance.get(p));
            if (response.transactionSuccess()){
                balance.put(p,0);
            }
        }
        getLogger().info("All Players balance has been deposited.");
        getLogger().info("SellBlocks plugin has been stopped");
    }
}
