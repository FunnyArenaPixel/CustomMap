package pigeon.customMap.packet;

import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import pigeon.customMap.Main;
import pigeon.customMap.model.Map;

@SuppressWarnings({"DuplicatedCode", "unused"})
public class ImagePacket {

    public static void sendPacket() {
        (new Thread(() -> {
            for (Level level : Main.getPlugin().getServer().getLevels().values()) {
                for (BlockEntity be : level.getBlockEntities().values()) {
                    if (be instanceof BlockEntityItemFrame) {
                        ((BlockEntityItemFrame) be).spawnToAll();
                    }
                }
            }
        })).start();
    }

    public static void reload() {
        for (String s : Map.framesid.keySet()) {
            for (Location location : Map.framesid.get(s)) {
                spawnToAllByLocation(location);
            }
        }
    }

    public static void add(String index) {
        for (Location location : Map.framesid.get(index)) {
            spawnToAllByLocation(location);
        }
    }

    public static void spawnToAllByLocation(Location location){
        BlockEntity be = location.getLevel().getBlockEntity(location);
        if (be instanceof BlockEntityItemFrame) {
            ((BlockEntityItemFrame) be).spawnToAll();
        }
    }

    public static void remove(long mapId) {
    }
}
