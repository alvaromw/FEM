package com.cadiducho.fem.core.api;

import com.cadiducho.fem.core.FEMCore;
import com.cadiducho.fem.core.api.FEMServer.GameID;
import com.cadiducho.fem.core.cmds.FEMCmd.Grupo;
import com.cadiducho.fem.core.taks.LobbyMessageTask;
import com.cadiducho.fem.core.util.FEMFileLoader;
import com.cadiducho.fem.core.util.Messages;
import com.cadiducho.fem.core.util.Metodos;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class FEMUser {
    
    @Getter private final UUID uuid;
    @Getter private FileConfiguration userLang;
    private static final FEMCore plugin = FEMCore.getInstance();
    
    @Getter @Setter private UserData userData;
    
    public FEMUser(OfflinePlayer p) {
        this(p.getUniqueId());
    }
    
    public FEMUser(UUID id) {
        uuid = id;
        setUserData(plugin.getMysql().loadUserData(uuid));
    }

    public void save() {
        plugin.getMysql().saveUser(this);
        FEMServer.users.remove(this);
        plugin.getMysql().loadUserData(uuid);
        FEMServer.users.add(this);
        setUserLang();
    }
    
    public OfflinePlayer getOfflinePlayer() {
        return plugin.getServer().getOfflinePlayer(uuid);
    }

    public Player getPlayer() {
        return plugin.getServer().getPlayer(uuid);
    }

    public String getName() {
        return getOfflinePlayer().getName(); 
    }
    
    public boolean isOnline() { 
        return getOfflinePlayer().isOnline();
    }
    
    // FEM
    public void sendMessage(String str, Object... obj) {
        String msg;
        if (str.startsWith("*")) { //Intentar reemplazar por mensaje predefinido
            msg = getUserLang().getString(str.substring(1), "&o" + str);
            if (obj != null) {
                int i = 0;
                for (Object re : obj) {
                    msg = msg.replace("{" + i + "}", re.toString());
                    i++;
                }
            }
        } else msg = str;
        for (String split : msg.split("\\{n\\}")) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (isOnline()) {
                    getPlayer().sendMessage(Metodos.colorizar(plugin.getTag() + " " + split));
                }
            });
        }

    }
    
    public void sendRawMessage(String str) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (isOnline()) {
                getPlayer().sendMessage(str);
            }
        });
    }
    
    public boolean isOnRank(Grupo rank) {
        return rank.getRank() <= getUserData().getGrupo().getRank();
    }
    
    public void throwMe(String[] args) {
        String accion = Metodos.colorizar("&e* " + getDisplayName() + "&e ") + Metodos.buildString(args);
        plugin.getServer().broadcastMessage(accion);
    }
    
    public void toggleAdminChat() {
        if (!FEMServer.adminChatMode.contains(this)) {
            sendMessage("*adminchat.activado");
            FEMServer.adminChatMode.add(this);
        } else {
            sendMessage("*adminchat.desactivado");
            FEMServer.adminChatMode.remove(this);
        }
    }

    public void suicide() {
        EntityDamageEvent ede = new EntityDamageEvent(getPlayer(), EntityDamageEvent.DamageCause.SUICIDE, Short.MAX_VALUE);
        plugin.getServer().getPluginManager().callEvent(ede);
        getPlayer().damage(Short.MAX_VALUE);
        if (getPlayer().getHealth() > 0) {
            getPlayer().setHealth(0);
	}
        
        plugin.getServer().broadcastMessage(Metodos.colorizar(FEMFileLoader.getEsLang().getString("suicide.mensaje").replace("{0}", getDisplayName())));
    }
    
    public String getDisplayName() {
        if (getUserData().getNickname() != null) {
            return getUserData().getNickname();
        }
        if (getPlayer().isOnline()) {
            if (getPlayer().getDisplayName() != null) {
                return getPlayer().getDisplayName();
            }
        }
        return getPlayer().getName();
    }
    
    public void tryHidePlayers() { //SOLO LOBBIES
        switch (getUserData().getHideMode()) {
            case 0:
                plugin.getServer().getOnlinePlayers().forEach(pl -> getPlayer().hidePlayer(pl));
                break;
            case 1:
                plugin.getServer().getOnlinePlayers().stream()
                        .filter(p -> getUserData().getAmigos().contains(p.getUniqueId()))
                        .forEach(p -> getPlayer().showPlayer(p));
                plugin.getServer().getOnlinePlayers().stream()
                        .filter(p -> !getUserData().getAmigos().contains(p.getUniqueId()))
                        .forEach(p -> getPlayer().hidePlayer(p));
                break;
            case 2:
                plugin.getServer().getOnlinePlayers().forEach(pl -> getPlayer().showPlayer(pl));
                break;
        }
    }
    
    public void sendToServer(String str) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(str);
        getPlayer().sendPluginMessage(plugin, "BungeeCord", out.toByteArray()); 
    }
    
    public void sendToLobby() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("bestLobby");
        out.writeUTF(getPlayer().getName());
        getPlayer().sendPluginMessage(plugin, "FEM", out.toByteArray()); 
    }
    
    /*
     * Reflection
     */
    public void sendActionBar(String msg) {
        if (getPlayer() == null) return;
        new Messages(FEMCore.getInstance(), "&cCore").sendActionBar(getPlayer(), msg);
    }

    public void repeatActionBar(String msg) {
        new LobbyMessageTask(this, msg).runTaskTimer(plugin, 2L, 20L);
    }
    
    public int getPing() {
        try {
            Method getHandleMethod = getPlayer().getClass().getDeclaredMethod("getHandle");
            getHandleMethod.setAccessible(true);
            
            Object entityPlayer = getHandleMethod.invoke(getPlayer());
            
            Field pingField = entityPlayer.getClass().getDeclaredField("ping");
            pingField.setAccessible(true);
            
            return pingField.getInt(entityPlayer);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {}
        return -1;
    }
    
    public void setUserLang() {
        switch (getUserData().getLang()) {
            case 2: userLang = FEMFileLoader.getItLang(); break;
            case 1: userLang = FEMFileLoader.getFrLang(); break;
            default: userLang = FEMFileLoader.getEsLang(); break;
        }
    }
    
    @Data
    public static class UserData {
        //Datos
        Grupo grupo = Grupo.Usuario;
        Location lastLocation = null;
        Boolean god = false;
        Long lastConnect = 0L;
        Long timeJoin = 0L;
        Long timePlayed = 0L;
        String nickname = null;
        Integer coins = 0;
        InetSocketAddress ip = null;
        
        Long parkourStartTime = -1L; //No guardar en MySQL
        Location parkourCheckpoint = FEMCore.getInstance().getServer().getWorlds().get(0).getSpawnLocation();
        
        //Stats
        HashMap<Integer, Integer> kills = new HashMap<>(); //id Juego, cantidad
        HashMap<Integer, Integer> deaths = new HashMap<>();
        HashMap<Integer, Integer> wins = new HashMap<>();
        HashMap<Integer, Integer> plays = new HashMap<>();

        Integer tntPuestas = 0;
        Integer tntQuitadas = 0;
        Integer tntExplotadas = 0;
        Integer genUpgraded = 0;
        Integer gemDestroyed = 0;
        Integer gemPlanted = 0;
        Integer record_dod = 0;
        Integer rondas_dod = 0;
        Integer picAcertadas = 0;
        Integer picDibujadas = 0; //Bien dibujadas, que alguien lo acertó
        Integer picPuntosTotales = 0;
        Integer brIntercambios = 0;
        Integer luckyRotos = 0;
        Integer teamTntPuestas = 0;
        Integer teamTntQuitadas = 0;
        Integer teamTntExplotadas = 0;
        Integer teamGenUpgraded = 0;
        HashMap<String, Integer> dropper = new HashMap<>();
        ArrayList<String> dropperInsignias = new ArrayList<>();
        
        //Settings
        Boolean friendRequest = false;
        Integer hideMode = 2; //0 nadie, 1 amigos, 2 todos
        Integer lang = 0; //0 castellano, 1 frances, 2 italiano
        Boolean enableTell = true;
        
        ArrayList<UUID> amigos = new ArrayList<>();
        
        //Establecer valores de 0 en los hashmap al crear instancia, evitar nulls
        public UserData() {
            for (int i = 1; i <= GameID.values().length; i++) {
                kills.put(i, 0);
                deaths.put(i, 0);
                wins.put(i, 0);
                plays.put(i, 0);
            }
        }
        
        public int getKills(GameID gameMode) {
            return kills.get(gameMode.getId());
        }
        
        public int getDeaths(GameID gameMode) {
            return deaths.get(gameMode.getId());
        }
        
        public int getWins(GameID gameMode) {
            return wins.get(gameMode.getId());
        }
        
        public int getPlays(GameID gameMode) {
            return plays.get(gameMode.getId());
        }
        
        public void addKill(GameID gameMode) {
            kills.replace(gameMode.getId(), getKills(gameMode) + 1);
        }
        
        public void addDeath(GameID gameMode) {
            deaths.replace(gameMode.getId(), getDeaths(gameMode) + 1);
        }
        
        public void addWin(GameID gameMode) {
            wins.replace(gameMode.getId(), getWins(gameMode) + 1);
        }
        
        public void addPlay(GameID gameMode) {
            plays.replace(gameMode.getId(), getPlays(gameMode) + 1);
        }
        
    }
    //-----
    
    @Override
    public String toString() {
        return "FEMUser{name: " + getName() + ", uuid: " + getUuid() + "}";
    }
 }
