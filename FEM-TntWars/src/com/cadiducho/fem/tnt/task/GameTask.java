package com.cadiducho.fem.tnt.task;

import com.cadiducho.fem.core.api.FEMServer;
import com.cadiducho.fem.tnt.TntWars;
import com.cadiducho.fem.tnt.manager.GameState;
import java.util.HashMap;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GameTask extends BukkitRunnable {

    private final TntWars plugin;
    public static GameTask instance;
    
    public GameTask(TntWars plugin) {
        this.plugin = plugin;
    }

    private static int count = 0;

    @Override
    public void run() {
        instance = this;
        checkWinner();
        if (count >= 0 && count < 2) {
            plugin.getMsg().sendBroadcast("&7El juego empezará en " + (count == 0 ? "2" : "1") + " segundos");
            plugin.getGm().getPlayersInGame().forEach(p -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1F, 1F));  
        } else if (count == 2) {
            for (Player players : plugin.getGm().getPlayersInGame()) {
                players.playSound(players.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F);
                TntWars.getPlayer(players).setCleanPlayer(GameMode.SURVIVAL);
                players.setScoreboard(plugin.getServer().getScoreboardManager().getNewScoreboard());
                TntWars.getPlayer(players).setGameScoreboard();
                HashMap<Integer, Integer> plays = TntWars.getPlayer(players).getBase().getUserData().getPlays();
                plays.replace(1, plays.get(1) + 1);
                TntWars.getPlayer(players).getBase().getUserData().setPlays(plays);
                FEMServer.getUser(players).save();
            }
            plugin.getAm().getIslas().forEach(i -> i.destroyCapsule());
        }
        if (count == 7) { //Desactivar a los 5 segundos la inmunidad por caidas
            plugin.getGm().setDañoEnCaida(true);
            plugin.getAm().getGeneradores().forEach(gen -> gen.init());
        }

        ++count;
        plugin.getGm().getPlayersInGame().forEach(pl -> pl.setLevel(count - 3));
    }
    
    public void checkWinner() {
        if (plugin.getGm().getPlayersInGame().size() <= 1) {
            Player winner = plugin.getGm().getPlayersInGame().get(0);
            plugin.getMsg().sendBroadcast(winner.getDisplayName() + " ha ganado la partida!");
            HashMap<Integer, Integer> wins = TntWars.getPlayer(winner).getBase().getUserData().getWins();
            wins.replace(1, wins.get(1) + 1);
            TntWars.getPlayer(winner).getBase().getUserData().setWins(wins);
            TntWars.getPlayer(winner).getBase().save();
            end();
            cancel();
        }
    }
    
    public void end() {
        GameState.state = GameState.ENDING;

        //Cuenta atrás para envio a los lobbies y cierre del server
        //Iniciar hilo del juego
        new ShutdownTask(plugin).runTaskTimer(plugin, 20l, 20l);
    }

    public static int getTimeLeft() {
        return count;
    }
}
