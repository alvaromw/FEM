package com.cadiducho.fem.lobby.utils;

import com.cadiducho.fem.core.util.ItemUtil;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.inventivetalent.particle.ParticleEffect;

import java.util.ArrayList;

public enum ParticleType {

    //NORMAL
    P(0, ItemUtil.createItem(Material.WOOL, 1, (short)14, "Corazones", new ArrayList<>()), ParticleEffect.HEART, ParticleID.NONE),
    P1(1, ItemUtil.createItem(Material.FURNACE, "Tren"), ParticleEffect.SMOKE_NORMAL, ParticleID.NONE),
    //HALO
    P2(2, ItemUtil.createItem(Material.LAVA_BUCKET, "Anillo de Fuego"), ParticleEffect.DRIP_LAVA, ParticleID.HALO),
    P3(3, ItemUtil.createItem(Material.WATER_BUCKET, "Anillo de Agua"), ParticleEffect.DRIP_WATER, ParticleID.HALO),
    //SPIRAL
    P4(4, ItemUtil.createItem(Material.FIREWORK, "Centellas"), ParticleEffect.FIREWORKS_SPARK, ParticleID.SPIRAL),
    P5(5, ItemUtil.createItem(Material.RECORD_11, "Música"), ParticleEffect.NOTE, ParticleID.SPIRAL, Color.fromBGR(50, 70, 100)),
    //SPHERE
    P6(6, ItemUtil.createItem(Material.ENDER_PEARL, "Area Venenosa"), ParticleEffect.REDSTONE, ParticleID.SPHERE, Color.fromBGR(255, 0, 255)),
    //POLYGON
    P7(7, ItemUtil.createItem(Material.ENCHANTMENT_TABLE, "Magia Arcana"), ParticleEffect.ENCHANTMENT_TABLE, ParticleID.POLYGON, 5),
    //WINGS
    P8(8, ItemUtil.createItem(Material.FEATHER, "Alas"), ParticleEffect.REDSTONE, ParticleID.WINGS, Color.fromBGR(6, 200, 100));


    @Getter private int id;
    @Getter private ItemStack item;
    @Getter private ParticleEffect pe;
    @Getter private ParticleID pid;
    @Getter private Color color;
    @Getter private int points;

    ParticleType(int id, ItemStack i, ParticleEffect pe, ParticleID pid){
        this(id, i, pe, pid, Color.fromBGR(0, 0, 0), 0);
    }

    ParticleType(int id, ItemStack i, ParticleEffect pe, ParticleID pid, Color color){
        this(id, i, pe, pid, color, 0);
    }

    ParticleType(int id, ItemStack i, ParticleEffect pe, ParticleID pid, int points){
        this(id, i, pe, pid, Color.fromBGR(0, 0, 0), points);
    }

    ParticleType(int id, ItemStack i, ParticleEffect pe, ParticleID pid, Color color, int points){
        this.id = id;
        this.item = i;
        this.pe = pe;
        this.pid = pid;
        this.color = color;
        this.points = points;
    }

    public enum ParticleID {
        //TODO: Más
        NONE, HALO, SPIRAL, SHIELD, WINGS, SPHERE, POLYGON, POLYGON_FULL, TORNADO;
    }
}
