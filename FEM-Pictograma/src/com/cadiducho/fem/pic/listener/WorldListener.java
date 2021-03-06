package com.cadiducho.fem.pic.listener;

import com.cadiducho.fem.pic.Pictograma;
import com.cadiducho.fem.pic.manager.GameState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class WorldListener implements Listener {

    private final Pictograma plugin;

    public WorldListener(Pictograma instnace) {
        plugin = instnace;
    }
    
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        if (e instanceof LivingEntity) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        e.setCancelled(true);
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        e.setCancelled(true);
    }
    
    @EventHandler
    public void onMotdChange(ServerListPingEvent e){
        e.setMotd(GameState.getParsedStatus() + "#" + plugin.getAm().getBuildZone().getWorld().getName());
    }
}
