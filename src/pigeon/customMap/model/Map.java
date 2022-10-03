package pigeon.customMap.model;

import cn.nukkit.Player;
import cn.nukkit.block.BlockAir;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.item.ItemMap;
import cn.nukkit.level.Location;
import cn.nukkit.utils.Config;
import pigeon.customMap.Main;
import pigeon.customMap.packet.ImagePacket;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class Map {

    public static HashMap<String, List<Location>> framesid = new HashMap<>();
    public static HashMap<String, List<String>> framescmd = new HashMap<>();

    public static void reloadData() {
        framesid.clear();
        framescmd.clear();
        File file = new File(Main.getPlugin().getDataFolder() + "/data.yml");
        Config data = new Config(file);
        Set<String> keys = data.getKeys(false);

        for (String key : keys) {
            List<Location> loclist = new ArrayList<>();
            data.getStringList(key + ".loclist").forEach((value) -> {
                String[] temploc = value.split(",");
                Location loc = new Location(Double.parseDouble(temploc[0]), Double.parseDouble(temploc[1]), Double.parseDouble(temploc[2]), Main.getPlugin().getServer().getLevelByName(data.getString(key + ".world")));
                loclist.add(loc);
                framescmd.put(key, data.getStringList(key + ".cmds"));
            });
            framesid.put(key, loclist);
        }

    }

    public static boolean containMap(Location maploc) {
        for (List<Location> locations : framesid.values()) {
            if (locations.contains(maploc)) {
                return true;
            }
        }
        return false;
    }

    public static void runCmd(Player p, Location maploc) {
        Object name = getIndex(maploc);
        List<String> cmds = framescmd.get(name);
        cmds.forEach((cmd) -> {
            cmd = cmd.replaceAll("%player%", p.getName());
            String[] tempcmd = cmd.split(":");
            if (tempcmd.length == 1) {
                Main.getPlugin().getServer().dispatchCommand(p, tempcmd[0]);
            } else if (tempcmd[0].equalsIgnoreCase("op")) {
                if (p.isOp()) {
                    Main.getPlugin().getServer().dispatchCommand(p, tempcmd[1]);
                } else {
                    p.setOp(true);
                    Main.getPlugin().getServer().dispatchCommand(p, tempcmd[1]);
                    p.setOp(false);
                }
            } else if (tempcmd[0].equalsIgnoreCase("console")) {
                Main.getPlugin().getServer().dispatchCommand(Main.getPlugin().getServer().getConsoleSender(), tempcmd[1]);
            }
        });
    }

    public static void clearMap(Player p, String index) {
        for (Location location : framesid.get(index)) {
            location.getLevel().setBlock(location, new BlockAir(), true, false);
            BlockEntity be = location.getLevel().getBlockEntity(location);
            if (be instanceof BlockEntityItemFrame) {
                BlockEntityItemFrame frame = (BlockEntityItemFrame) be;
                if (frame.getItem() instanceof ItemMap) {
                    ImagePacket.remove(((ItemMap) frame.getItem()).getMapId());
                }
                location.getLevel().removeBlockEntity(be);
            }
        }
        Config data = new Config(new File(Main.getPlugin().getDataFolder() + "/data.yml"));
        data.remove(index);
        data.save();
        reloadData();
        p.sendMessage("§e删除成功");
    }

    public static String getIndex(Location maploc) {
        for(String key : framesid.keySet()){
            List<Location> locations = framesid.get(key);
            if(locations.contains(maploc)){
                return key;
            }
        }
        return "";
    }
}
