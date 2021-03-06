package com.cadiducho.fem.chat;

import java.io.*;
import java.util.logging.Level;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.UUID;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;

public class BungeeListener implements Listener {
    
    private final FEMChat plugin;
    
    public BungeeListener(FEMChat instance) {
        plugin = instance;
    }

    public void processChat(ProxiedPlayer player, String mensaje) {
        if (plugin.chatsActivados.containsKey(player.getUniqueId())) {
            ProxiedPlayer chat = plugin.getProxy().getPlayer(plugin.chatsActivados.get(player.getUniqueId()));
            sendPrivateMessage(chat, player, mensaje.split(FEMChat.CHAR)[1]);
        } else {
            sendChat(player, mensaje.replace(FEMChat.CHAR, ""));
        }
    }
    
    public void sendChat(ProxiedPlayer from, String mensaje) {
        try {
            //Filtro antispam
            if(checkSpam(from)) return;
            
            //Filtro antimayúsculas
            if (!plugin.isStaff(from) && mensaje.length() > 3) {
                int mayusculas = 0;
                for (char c : mensaje.toCharArray()) {
                    if (c <= 'Z' && c >= 'A') {
                        mayusculas++;
                    }
                }

                if (mayusculas > mensaje.length() / 2) {
                    mensaje = mensaje.substring(0, 1).toUpperCase().concat(mensaje.toLowerCase().substring(1));
                }
            }
            
          
            BaseComponent[] msg = Parser.parse(mensaje);
            for (ProxiedPlayer target : plugin.getProxy().getPlayers()) {
                // Enviar a todos los jugadores que estén en un servidor lobby y no estén ignorados
                
                if (target.getServer() == null) continue;
                if (!target.getServer().getInfo().getName().contains("lobby")) continue;
                if (plugin.ignoredPlayers.get(target.getUniqueId()) != null && plugin.ignoredPlayers.get(target.getUniqueId()).contains(from.getUniqueId())) continue;
                
                target.sendMessage(msg);
            }
        } catch (Throwable th) {
            from.sendMessage(Parser.parse("&cError interno procesando el mensaje."));
            plugin.getLogger().log(Level.SEVERE, "Error procensando el mensaje", th);
        }
    }
    
    public void processPrivateMsg(String targetS, String fromS, String mensaje) {
        ProxiedPlayer from = plugin.getProxy().getPlayer(fromS);
        if (from == null) return;
        
        if (targetS.equals("")) { //terminar chat privado
            if (!checkEndChat(from)) {
                from.sendMessage(Parser.parse(c(plugin.getTag()+ "&eUsa &a/tell <Usuario> <Mensaje>!")));
            }
            return;
        }
        
        ProxiedPlayer target = plugin.getProxy().getPlayer(targetS); 
        if (targetS.equals(FEMChat.CHAR + "reply")) {
            UUID targetId = plugin.replyTarget.get(from.getUniqueId());
            if(targetId == null){
                from.sendMessage(Parser.parse(c(plugin.getTag() + "¡Nadie te ha enviado un mensaje! :(")));
                return;
            }
            target = plugin.getProxy().getPlayer(targetId);
        }
        
        if (target == null) {
            from.sendMessage(Parser.parse(c(plugin.getTag() + "&c¡Jugador no encontrado!")));
            return;
        }
        
        if (plugin.ignoredPlayers.get(from.getUniqueId()) != null && plugin.ignoredPlayers.get(from.getUniqueId()).contains(target.getUniqueId())) {
            from.sendMessage(Parser.parse(c(plugin.getTag() + "&c¡No puedes hablar a un usuario al que has ignorado!")));
            return;
        }
        
        if (mensaje.equals("")) { //Iniciar chat con target   
            if (checkEndChat(from)) return;
            plugin.chatsActivados.put(from.getUniqueId(), target.getUniqueId());
            from.sendMessage(Parser.parse(plugin.getTag() + "&aHas iniciado una conversación privada con &e" + target.getName()));
            return;
        }
        sendPrivateMessage(target, from, mensaje);   
    }
    
    public boolean checkEndChat(ProxiedPlayer p) {
        if (plugin.chatsActivados.containsKey(p.getUniqueId())) {
            ProxiedPlayer oldChat = plugin.getProxy().getPlayer(plugin.chatsActivados.get(p.getUniqueId()));
            plugin.chatsActivados.remove(p.getUniqueId());
            p.sendMessage(Parser.parse(c(plugin.getTag() + "&cHas terminado tu chat con " + oldChat.getName())));
            return true;
        }
        return false;
    }
    
    public void sendPrivateMessage(ProxiedPlayer target, ProxiedPlayer from, String mensaje) {
        //Filtro antispam
        if(checkSpam(from)) return;
            
        from.sendMessage(Parser.parse(c("&6A " + target.getName() + ": &d" + mensaje)));
        
        //Si el target tiene activados los mensajes privados
        
        if (plugin.getDisableTell().contains(target.getUniqueId())) return;
        
        //Solo mostrar el mensaje si target no ignora a from
        if (plugin.ignoredPlayers.get(target.getUniqueId()) != null && plugin.ignoredPlayers.get(target.getUniqueId()).contains(from.getUniqueId())) return;
        
        target.sendMessage(Parser.parse(c("&6De " + from.getName() + ": &d" + mensaje)));
        plugin.setReply(target.getUniqueId(), from.getUniqueId());
    }
    
    public void processIgnore(String targetS, String fromS) {
        ProxiedPlayer from = plugin.getProxy().getPlayer(fromS);
        if (from == null) return;
        
        ProxiedPlayer target = plugin.getProxy().getPlayer(targetS);
        if (target == null) {
            from.sendMessage(Parser.parse(c(plugin.getTag() + "&c¡Jugador no encontrado!")));
            return;
        }
        
        if (plugin.isStaff(target)) {
            from.sendMessage(Parser.parse(c(plugin.getTag() + "&c¡No puedes ignorar a alguien del staff!")));
            return;
        }
        
        ArrayList<UUID> ignorados = plugin.ignoredPlayers.get(from.getUniqueId());
        if (ignorados == null) {
            ignorados = new ArrayList<>(1);
        }
        
        if (ignorados.contains(target.getUniqueId())) { //Eliminar ignore
            ignorados.remove(target.getUniqueId());
            from.sendMessage(Parser.parse(c(plugin.getTag() + "&aYa no ignoras a " + target.getName())));
            plugin.getMysql().removeIgnore(from.getUniqueId(), target.getUniqueId());
        } else {
            ignorados.add(target.getUniqueId());
            from.sendMessage(Parser.parse(c(plugin.getTag() + "&aAhora ignoras a " + target.getName())));
            plugin.getMysql().addIgnore(from.getUniqueId(), target.getUniqueId());
        }
        plugin.ignoredPlayers.put(from.getUniqueId(), ignorados);
    }
    
    public void processIgnoreList(String pS) {
        ProxiedPlayer p = plugin.getProxy().getPlayer(pS);
        if (p == null) return;
        
        ArrayList<UUID> ignorados = plugin.ignoredPlayers.get(p.getUniqueId());
        if (ignorados == null || ignorados.isEmpty()) {
            p.sendMessage(Parser.parse(c(plugin.getTag() + "&aNo has ignorado a nadie")));
            p.sendMessage(Parser.parse(c(plugin.getTag() + "&aUsa &e/ignore <usuario> &apara hacerlo")));
            return;
        }
        
        String usuarios = "";
        usuarios = ignorados.stream()
            .map(u -> plugin.getProxy().getPlayer(u).getName() + ", ")
            .reduce(usuarios, String::concat);
        
        if (!"".equals(usuarios)) usuarios = usuarios.substring(0, usuarios.length() - 2);
        
        p.sendMessage(Parser.parse(c(plugin.getTag() + "&aUsuarios que están actualmente ignorados:")));
        p.sendMessage(Parser.parse(c("&e" + usuarios)));
    }
    /*
     * Recibir los packets de Bukkit con rangos y demás datos 
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getTag().equals(FEMChat.MAIN_CHANNEL)) {
            event.setCancelled(true);
            if (event.getReceiver() instanceof ProxiedPlayer && event.getSender() instanceof Server) {
                try {
                    ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();

                    DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));
                    String subchannel = in.readUTF();
                    switch (subchannel) {
                        case FEMChat.MAIN_SUBCHANNEL:
                            processChat(player, in.readUTF());
                            break;
                        case FEMChat.PRIVATE_SUBCHANNEL:
                            processPrivateMsg(in.readUTF(), in.readUTF(), in.readUTF());
                            break;
                        case FEMChat.IGNORE_SUBCHANNEL:
                            processIgnore(in.readUTF(), in.readUTF());
                            break;
                        case FEMChat.IGNLIST_SUBCHANNEL:
                            processIgnoreList(in.readUTF());
                            break;
                        case FEMChat.UPDATE_SUBCHANNEL:
                            plugin.getMysql().updateDisableTellList();
                            break;
                    }
                } catch (IOException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Error obtieniendo datos de Bukkit", ex);
                }
            }
        }
    }
    
    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer p = event.getPlayer();
        if (plugin.chatsActivados.containsKey(p.getUniqueId())) plugin.chatsActivados.remove(p.getUniqueId());
        if (plugin.replyTarget.containsKey(p.getUniqueId())) plugin.replyTarget.remove(p.getUniqueId());
    }
    
    public String c(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
    
    public boolean checkSpam(ProxiedPlayer p) {
        if (!plugin.spamDataMap.containsKey(p.getUniqueId())) {
            plugin.spamDataMap.put(p.getUniqueId(), new AntiSpamData());
        }
        
        AntiSpamData antiSpamData = plugin.spamDataMap.get(p.getUniqueId());
        if (antiSpamData.isSpamming()) {
            p.sendMessage(Parser.parse(c(plugin.getTag() + "&cHas enviado demasiados mensajes. Vuelve a intentarlo en un minuto")));
            return true;
        }
        return false;
    }
}
