package com.cadiducho.fem.pro;

import com.cadiducho.fem.core.util.Metodos;
import com.cadiducho.fem.pro.utils.CuboidRegion;
import com.cadiducho.fem.pro.utils.ProType;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.*;

public class ProArea {

    @Getter private int id;
    @Getter private ProType proType;
    @Getter private CuboidRegion cuboidRegion;
    @Getter private ProPlayer proPlayer;
    @Getter private Location location;

    private static final Protections pro = Protections.getInstance();

    public ProArea(Location l, ProType proType, ProPlayer proPlayer){
        this(l, proType, proPlayer,0); //Para crear una nueva parcela
    }

    public ProArea(Location l, ProType proType, ProPlayer proPlayer, int id){
        this.location = l;
        this.proType = proType;
        this.proPlayer = proPlayer;
        this.id = id;
        //Dejemos el método aquí...
    }

    public ProArea(int id){
        this.id = id;
    }

    public ProArea(ProPlayer proPlayer){
        this.proPlayer = proPlayer;
    }

    public ProArea(){}

    //Area Methods
    public void generateArea(Material m){
        this.id = pro.getFiles().getID("areas"); //Siguiente ID
        pro.getFiles().getAreas().set("area_" + id + ".block", Metodos.locationToStringNormal(location));
        pro.getFiles().getAreas().set("area_" + id + ".loc", cuboidRegion.toString());
        pro.getFiles().getAreas().set("area_" + id + ".dueño", proPlayer.getUuid());
        pro.getFiles().getAreas().set("area_" + id + ".admins", Arrays.asList(proPlayer.getUuid()));
        pro.getFiles().getAreas().set("area_" + id + ".users", Arrays.asList(""));
        pro.getFiles().getAreas().set("area_" + id + ".tipo", proType.toString());
        setDefaultFlags();
        pro.getFiles().saveFiles();

        setBorderMaterial(m);
    }

    public void generateCuboidRegion(){
        Block b1 = location.getWorld().getBlockAt((int) location.getX() + (proType.getArea() / 2), (int)(location.getY() - 1), (int) location.getZ() + (proType.getArea() / 2));
        Block b2 = location.getWorld().getBlockAt((int) location.getX() -(proType.getArea() / 2), (int)(location.getY() - 1), (int) location.getZ() - (proType.getArea() / 2));

        this.cuboidRegion = new CuboidRegion(b1, b2, proType.getArea());
    }

    public void showArea(){
        //getSquareLocations().forEach(l -> ParticleEffect.REDSTONE.display(new ParticleEffect.OrdinaryColor(255, 0, 255), l, proPlayer.getPlayer()));
    }

    public void removeArena(Material m){
        pro.getFiles().getAreas().set("area_" + id, null);
        pro.getFiles().saveFiles();
        setBorderMaterial(m);
    }

    private void setBorderMaterial(Material m){
        getSquareLocations().forEach(l -> l.getWorld().getBlockAt(l.subtract(0, 5, 0)).getRelative(BlockFace.UP).setType(m));
    }

    //Area Getters
    public List<ProPlayer> getAreaUsers(){
        List<ProPlayer> players = new ArrayList<>();

        pro.getFiles().getAreas().getStringList("area_" + id + ".users").forEach(ad -> players.add(new ProPlayer(UUID.fromString(ad))));

        return players;
    }

    public List<ProPlayer> getAreaOwners(){
        List<ProPlayer> players = new ArrayList<>();

        pro.getFiles().getAreas().getStringList("area_" + id + ".admins").forEach(ad -> players.add(new ProPlayer(UUID.fromString(ad))));

        return players;
    }

    public ProPlayer getDueño(){
        return new ProPlayer(UUID.fromString(pro.getFiles().getAreas().getString("area_" + id + ".dueño")));
    }

    public ProType getProType(){
        return ProType.valueOf(pro.getFiles().getAreas().getString("area_" + id + ".tipo"));
    }

    public CuboidRegion getCuboidRegion(){
        Block b1 = stringCuboidBlockToLocation(pro.getFiles().getAreas().getString("area_" + id + ".loc"), 0).getBlock();
        Block b2 = stringCuboidBlockToLocation(pro.getFiles().getAreas().getString("area_" + id + ".loc"), 1).getBlock();
        return new CuboidRegion(b1, b2, ProType.valueOf(pro.getFiles().getAreas().getString("area_" + id + ".tipo")).getArea());
    }

    public Location getLocation(){
        return Metodos.stringToLocationNormal(pro.getFiles().getAreas().getString("area_" + id + ".block"));
    }

    //Utils
    public Location stringCuboidBlockToLocation(String string, int block){
        if (string == null) return null;
        return Metodos.stringToLocationNormal(string.split(";")[block]);
    }

    private boolean hit;
    public boolean hitOtherArena(){
        hit = false;

        if (getAllRegions().size() == 0) return false;

        getAllRegions().forEach(r -> r.toArray().forEach(b -> {
            if (cuboidRegion.toArray().contains(b)) {
                hit = true;
            }
        }));
        return hit;
    }

    public boolean exist(){
        return pro.getFiles().getAreas().contains("area_" + id);
    }

    public boolean isInsideArea(){
        return getCuboidRegion().contains(proPlayer.getPlayer().getLocation().getBlock());
    }

    //Getters
    private List<Location> getSquareLocations(){
        List<Location> locations = new ArrayList<>();

        for (int g = 0; g < proType.getArea(); g++){
            locations.add(new Location(cuboidRegion.getCorner1().getWorld(), cuboidRegion.getCorner1().getX() - g, cuboidRegion.getCorner1().getY() + (g / 2), cuboidRegion.getCorner1().getZ()));
        }
        for (int g = 0; g < proType.getArea(); g++){
            locations.add(new Location(cuboidRegion.getCorner1().getWorld(), cuboidRegion.getCorner1().getX(), cuboidRegion.getCorner1().getY() + (g / 2), cuboidRegion.getCorner1().getZ() - g));
        }
        for (int g = 0; g < proType.getArea(); g++){
            locations.add(new Location(cuboidRegion.getCorner2().getWorld(), cuboidRegion.getCorner2().getX() + g, cuboidRegion.getCorner2().getY() + (g / 2), cuboidRegion.getCorner2().getZ()));
        }
        for (int g = 0; g < proType.getArea(); g++){
            locations.add(new Location(cuboidRegion.getCorner2().getWorld(), cuboidRegion.getCorner2().getX(), cuboidRegion.getCorner2().getY() + (g / 2), cuboidRegion.getCorner2().getZ() + g));
        }
        return locations;
    }

    public List<CuboidRegion> getAllRegions(){
        List<CuboidRegion> regions = new ArrayList<>();
        getAllAreas().forEach(a -> regions.add(a.getCuboidRegion()));
        return regions;
    }

    public List<ProArea> getAllAreas(){
        List<ProArea> areas = new ArrayList<>();
        for (int x = 0; x < pro.getFiles().getCurrentID("areas"); x++){
            Location l = Metodos.stringToLocationNormal(pro.getFiles().getAreas().getString("area_" + x + ".block"));
            ProPlayer player = new ProPlayer(UUID.fromString(pro.getFiles().getAreas().getString("area_" + id + ".dueño")));

            areas.add(new ProArea(l, ProType.valueOf(pro.getFiles().getAreas().getString("area_" + id + ".tipo")), player, x));
        }
        return areas;
    }

    public List<Integer> getPlayerAreas(ProPlayer player){
        List<Integer> areas = new ArrayList<>();

        getAllAreas().forEach(a ->{
            if (a.getDueño().equals(player)){
                areas.add(a.getId());
            }
        });
        return areas;
    }

    //Area Settings
    public HashMap<String, Boolean> getAllFlags(){
        HashMap<String, Boolean> settings = new HashMap<>();

        pro.getFiles().getAreas().getStringList("area_" + id + "flags").forEach(s -> settings.put(s, getFlags(s)));

        return settings;
    }

    public void setFlags(String s, boolean value){
        String path = "area_" + id + "flags.";

        pro.getFiles().getAreas().set(path + s, value);
        pro.getFiles().saveFiles();
    }

    public boolean getFlags(String s){
        return pro.getFiles().getAreas().getBoolean("area_" + id + "flags." + s);
    }

    private void setDefaultFlags(){
        setFlags("join", true);
        setFlags("pvp", false);
        setFlags("pve", false);
        setFlags("explosion", false);
    }
}
