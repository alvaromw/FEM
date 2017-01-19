package com.cadiducho.fem.core.util;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class Messages {
    private static Class<?> packetClass = null;
    private static Class<?> componentClass = null;
    private static Class<?> packetTabClass = null;
    private static Class<?> serializerClass = null;
    private static Constructor<?> packetConstructor = null;
    private static Constructor<?> packetTabConstructor = null;
    @SuppressWarnings("rawtypes")
    private static Class<Enum> enumTitleAction = null;

    private static String nmsver;

    private final Plugin plugin;
    private final String prefix;

    public Messages(Plugin instance, String prefix) {
        this.plugin = instance;
        this.prefix = Metodos.colorizar(prefix);

        nmsver = plugin.getServer().getClass().getPackage().getName();
        nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);
    }

    public void sendMessage(Player player, String msg) {
        player.sendMessage(prefix + " " + Metodos.colorizar(msg));
    }

    public void sendColorMessage(Player player, String msg) {
        player.sendMessage(Metodos.colorizar(msg));
    }

    public void sendBroadcast(String msg) {
        plugin.getServer().broadcastMessage(prefix + " " + Metodos.colorizar(msg));
    }

    public void sendEmptyLine() {
        plugin.getServer().broadcastMessage("");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void sendTitle(Player p, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
        packetClass = ReflectionAPI.getNmsClass("PacketPlayOutTitle");
        componentClass = ReflectionAPI.getNmsClass("IChatBaseComponent");
        serializerClass = ReflectionAPI.getNmsClass("IChatBaseComponent$ChatSerializer");
        enumTitleAction = (Class<Enum>) ReflectionAPI.getNmsClass("PacketPlayOutTitle$EnumTitleAction");
        try {
            packetConstructor = packetClass.getConstructor(enumTitleAction, componentClass, int.class, int.class, int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (subtitle != null) {
            Object subTitleSer;
            Object subTitlePacket;
            try {
                subTitleSer = serializerClass.getMethod("a", String.class).invoke(null, "{\"text\": \"" + Metodos.colorizar(subtitle) + "\"}");
                subTitlePacket = packetConstructor.newInstance(enumTitleAction.getEnumConstants()[1], subTitleSer, fadeIn.intValue(), stay.intValue(), fadeOut.intValue());
                ReflectionAPI.sendPacket(p, subTitlePacket);
            } catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
                System.out.println(enumTitleAction.getEnumConstants());
            }
        }
        if (title != null) {
            Object titleSer;
            Object titlePacket;
            try {
                titleSer = serializerClass.getMethod("a", String.class).invoke(null, "{\"text\": \"" + Metodos.colorizar(title) + "\"}");
                titlePacket = packetConstructor.newInstance(enumTitleAction.getEnumConstants()[0], titleSer, fadeIn.intValue(), stay.intValue(), fadeOut.intValue());
                ReflectionAPI.sendPacket(p, titlePacket);
            } catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendActionBar(Player p, String msg) {
        try {
            packetClass = ReflectionAPI.getNmsClass("PacketPlayOutChat");
            componentClass = ReflectionAPI.getNmsClass("IChatBaseComponent");
            serializerClass = ReflectionAPI.getNmsClass("IChatBaseComponent$ChatSerializer");
            packetConstructor = packetClass.getConstructor(componentClass, byte.class);
            Object BaseComponent = serializerClass.getMethod("a", String.class).invoke(null, "{\"text\": \"" + Metodos.colorizar(msg) + "\"}");
            Object packet = packetConstructor.newInstance(BaseComponent, (byte) 2);
            ReflectionAPI.sendPacket(p, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendHeaderAndFooter(Player p, String headerText, String footerText) {
        try {
            Class chatSerializer = ReflectionAPI.getNmsClass("ChatSerializer");

            Object tabHeader = chatSerializer.getMethod("a", String.class).invoke(chatSerializer, "{'text': '" + headerText + "'}");
            Object tabFooter = chatSerializer.getMethod("a", String.class).invoke(chatSerializer, "{'text': '" + footerText + "'}");

            Object ppoplhf = ReflectionAPI.getNmsClass("PacketPlayOutPlayerListHeaderFooter").getConstructor(new Class[]{ReflectionAPI.getNmsClass("IChatBaseComponent")}).newInstance(new Object[]{tabHeader});

            Field f = ppoplhf.getClass().getDeclaredField("b");
            f.setAccessible(true);
            f.set(ppoplhf, tabFooter);

            Object nmsp = p.getClass().getMethod("getHandle", new Class[0]).invoke(p, new Object[0]);
            Object pcon = nmsp.getClass().getField("playerConnection").get(nmsp);

            pcon.getClass().getMethod("sendPacket", new Class[]{ReflectionAPI.getNmsClass("Packet")}).invoke(pcon, new Object[]{ppoplhf});
        } catch (IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public String getPrefix() {
        return prefix;
    }
}
