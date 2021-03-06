package com.cadiducho.fem.core.taks;

import com.cadiducho.fem.core.api.FEMUser;
import org.bukkit.scheduler.BukkitRunnable;

public class LobbyMessageTask extends BukkitRunnable {
    
    private final FEMUser player;
    private final String mensaje;
    
    public LobbyMessageTask(FEMUser instance, String msg) {
        player = instance;
        mensaje = msg;
    }

    @Override
    public void run() {
        if (player.getPlayer() == null) cancel();
        
        player.sendActionBar(mensaje);
    }
    
}
