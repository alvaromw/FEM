package com.cadiducho.fem.pic;

import com.cadiducho.fem.core.api.FEMServer;
import com.cadiducho.fem.core.api.FEMUser;
import com.cadiducho.fem.core.util.ItemUtil;
import com.cadiducho.fem.pic.listener.*;
import com.cadiducho.fem.pic.manager.ArenaManager;
import com.cadiducho.fem.pic.manager.GameManager;
import com.cadiducho.fem.pic.manager.GameState;
import com.cadiducho.fem.pic.util.Messages;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import lombok.Getter;
import org.bukkit.DyeColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Pictograma extends JavaPlugin {

    @Getter private static Pictograma instance;
    
    public static ArrayList<PicPlayer> players = new ArrayList<>();

    @Getter private ArenaManager am;
    @Getter private GameManager gm;
    @Getter private Messages msg;
    
    public static final List<String> palabras = Arrays.asList("noche", "rio", "mar", "coche", "avion", "ocelote", "cono", "fresa", "manzana", "pera");
    public Inventory colorPicker;

    @Override
    public void onEnable() {
        instance = this;
        
        File fConf = new File(getDataFolder(), "config.yml");
        if (!fConf.exists()) {
            try {
                getConfig().options().copyDefaults(true);
                saveConfig();
            } catch (Exception e) {}
        }

        gm = new GameManager(instance);
        am = new ArenaManager(instance);
        am.prepareWorld(getServer().getWorld(getConfig().getString("Pictograma.Arena.mundo")));
        msg = new Messages(instance);
        msg.init();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(instance), instance);
        pm.registerEvents(new WorldListener(instance), instance);
        pm.registerEvents(new GameListener(instance), instance);
        
        colorPicker = getServer().createInventory(null, 9, "Escoge el color del pincel");
        colorPicker.setItem(0, ItemUtil.createWool("Blanco", DyeColor.WHITE));
        colorPicker.setItem(1, ItemUtil.createWool("Negro", DyeColor.BLACK));
        colorPicker.setItem(2, ItemUtil.createWool("Rojo", DyeColor.RED));
        colorPicker.setItem(3, ItemUtil.createWool("Naranja", DyeColor.ORANGE));
        colorPicker.setItem(4, ItemUtil.createWool("Amarillo", DyeColor.YELLOW));
        colorPicker.setItem(5, ItemUtil.createWool("Verde", DyeColor.GREEN));
        colorPicker.setItem(6, ItemUtil.createWool("Azul", DyeColor.BLUE));
        colorPicker.setItem(7, ItemUtil.createWool("Morado", DyeColor.PURPLE));
        colorPicker.setItem(8, ItemUtil.createWool("Marron", DyeColor.BROWN));
        
        GameState.state = GameState.LOBBY;
        getLogger().log(Level.INFO, "ByD: Activado correctamente");
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "ByD: Desativado correctamente");
    }
    
    public String getRandomWord() {
        int i = palabras.size();
        Random r = new Random();
        return palabras.get(r.nextInt(i));
    }
    
    public static PicPlayer getPlayer(OfflinePlayer p) {
        FEMUser u = FEMServer.getUser(p);
        for (PicPlayer pl : players) {
            if (pl.getBase().getUuid() == null) {
                continue;
            }
            if (pl.getBase().getUuid().equals(p.getUniqueId())) {
                return pl;
            }
        }
        PicPlayer us = new PicPlayer(u);
        if (us.getBase().getBase().isOnline()) {
            players.add(us);
        }
        return us;
    }
}
