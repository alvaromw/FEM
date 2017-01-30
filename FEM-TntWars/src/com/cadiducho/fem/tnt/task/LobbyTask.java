package com.cadiducho.fem.tnt.task;

import com.cadiducho.fem.tnt.TntWars;
import com.cadiducho.fem.tnt.manager.GameState;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class LobbyTask extends BukkitRunnable {

    private final TntWars plugin;

    public LobbyTask(TntWars instance) {
        plugin = instance;
    }

    private int count = 36;

    @Override
    public void run() {
        //Comprobar si sigue habiendo suficientes jugadores o cancelar
        if (plugin.getGm().getPlayersInGame().size() < plugin.getAm().getMinPlayers()) {
            plugin.getGm().setCheckStart(true);
            plugin.getServer().getOnlinePlayers().forEach(pl ->  pl.setLevel(0));
            GameState.state = GameState.LOBBY;
            cancel();
            return;
        }
        
        plugin.getGm().getPlayersInGame().stream().forEach(players -> {
            plugin.getMsg().sendActionBar(players, "&a&lEl juego empieza en: " + (count + 4));
        });
        if (count == 30) {
            plugin.getMsg().sendBroadcast("&7El juego empezará en &c30 &7segundos");
        } else if (count > 0 && count <= 2) {
            plugin.getMsg().sendBroadcast("&7El juego empezará en &c" + (count + 3) + " &7segundos");
            plugin.getGm().getPlayersInGame().forEach(p -> p.playSound(p.getLocation(), Sound.NOTE_PLING, 1F, 1F));
        } else if (count == 0) {
            GameState.state = GameState.GAME;
            plugin.getMsg().sendBroadcast("&7El juego empezará en &c3 &7segundos");
            plugin.getGm().getPlayersInGame().forEach(p -> p.playSound(p.getLocation(), Sound.NOTE_PLING, 1F, 1F));
            plugin.getGm().getPlayersInGame().forEach(p -> plugin.getAm().teleport(p)); 
            plugin.getGm().setDañoEnCaida(false);
            
            //Iniciar hilo de la fase de esconder
            new GameTask(plugin).runTaskTimer(plugin, 20l, 20l);
            cancel();
        }

        --count;      
    }

}
