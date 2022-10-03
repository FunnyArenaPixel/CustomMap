package pigeon.customMap.uilts;

import cn.nukkit.Player;
import cn.nukkit.block.BlockItemFrame;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.ItemMap;
import cn.nukkit.level.Location;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Config;
import pigeon.customMap.Main;
import pigeon.customMap.model.Map;
import pigeon.customMap.packet.ImagePacket;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class Build {

    public static void giveMap(Player p, String name, String index) throws IOException {
        ItemMap map = (ItemMap) Item.get(ItemID.MAP);
        map.setImage(new File(Main.getPlugin().getDataFolder() + "/images/" + name));
        map.setCustomName("§c魔法棒");
        String[] lores = new String[]{name, index};
        map.setLore(lores);
        p.getInventory().addItem(map);
    }

    public static ItemMap getMap(String name, int index) throws IOException {
        ItemMap map = (ItemMap) Item.get(ItemID.MAP);
        String suffix = Image.ext(name);
        File tempfile = new File(Main.getPlugin().getDataFolder() + "/images/" + name.replaceAll("." + Image.ext(name), "") + "/" + index + "." + suffix);
        map.setImage(tempfile);
        return map;
    }

    public static boolean buildImage(Player p, BlockFace face, String name, Location loc1, Location loc2, String index) throws IOException {
        double y1 = loc1.getY();
        double y2 = loc2.getY();
        if (y2 < y1) {
            Location temp = loc1;
            loc1 = loc2;
            loc2 = temp;
            y1 = loc1.getY();
            y2 = temp.getY();
        }

        double z1 = loc1.getZ();
        double z2 = loc2.getZ();
        double x1 = loc1.getX();
        double x2 = loc2.getX();
        double width = 0.0D;
        if (loc1.getX() != loc2.getX() && loc1.getZ() != loc2.getZ()) {
            return false;
        } else {
            if (loc1.getX() == loc2.getX()) {
                width = Math.abs(z1 - z2);
            }

            if (loc1.getZ() == loc2.getZ()) {
                width = Math.abs(x1 - x2);
            }

            final BufferedImage[] imgs = Image.splitImage(name, (int) (y2 - y1) + 1, (int) width + 1);
            int tempi = imgs.length - 1;
            double tempy = y1;

            for (int h = (int) (y2 - y1); h >= 0; --h) {
                double tempx = x1;
                double tempz = z1;

                for (int w = 0; (double) w <= width; ++w) {
                    Vector3 pos = new Vector3(tempx, tempy, tempz);
                    switch (face) {
                        case WEST:
                            pos = pos.west();
                            break;
                        case EAST:
                            pos = pos.east();
                            break;
                        case SOUTH:
                            pos = pos.south();
                            break;
                        case NORTH:
                            pos = pos.north();
                    }

                    loc1.getLevel().setBlock(pos, new BlockItemFrame(getMetaByFace(face)), true, false);
                    FullChunk chunk = loc1.getLevel().getChunk(pos.getFloorX() >> 4, pos.getFloorZ() >> 4, false);
                    CompoundTag nbt = (new CompoundTag()).putString("id", "ItemFrame").putInt("x", (int) pos.x).putInt("y", (int) pos.y).putInt("z", (int) pos.z);
                    final BlockEntityItemFrame frameEntity = new BlockEntityItemFrame(chunk, nbt);
                    addFile(name, frameEntity.getLocation(), index, p, loc1, loc2, face);
                    int finalTempi = tempi;
                    Main.getPlugin().getServer().getScheduler().scheduleDelayedTask(Main.getPlugin(), () -> {
                        ItemMap map = (ItemMap) Item.get(ItemID.MAP);
                        map.setImage(imgs[finalTempi]);
                        frameEntity.setItem(map);
                        if (finalTempi == 0) {
                            Map.reloadData();
                            ImagePacket.add(index);
                        }

                    }, 10);
                    --tempi;
                    if (loc1.getZ() == loc2.getZ()) {
                        if (x1 < x2) {
                            ++tempx;
                        } else {
                            --tempx;
                        }
                    }
                    if (loc1.getX() == loc2.getX()) {
                        if (z1 < z2) {
                            ++tempz;
                        } else {
                            --tempz;
                        }
                    }
                }
                ++tempy;
            }
            // 保存世界
            loc1.getLevel().save(true);
            return true;
        }
    }

    public static int getMetaByFace(BlockFace face) {
        switch (face) {
            case WEST:
                return 1;
            case EAST:
                return 0;
            case SOUTH:
                return 2;
            case NORTH:
                return 3;
            default:
                return -1;
        }
    }

    public static void addFile(String name, Location nowLoc, String index, Player p, Location loc1, Location loc2, BlockFace face) {
        File file = new File(Main.getPlugin().getDataFolder() + "/data.yml");
        Config data = new Config(file);
        Set<String> keys = data.getKeys(false);
        List<String> datalist = new ArrayList<>();
        Iterator<String> iterator = keys.iterator();

        String key;
        do {
            if (!iterator.hasNext()) {
                datalist.add(nowLoc.getX() + "," + nowLoc.getY() + "," + nowLoc.getZ());
                data.set(index + ".loclist", datalist);
                data.set(index + ".face", face.toString());
                data.set(index + ".loc1", loc1.getX() + "," + loc1.getY() + "," + loc1.getZ());
                data.set(index + ".loc2", loc2.getX() + "," + loc2.getY() + "," + loc2.getZ());
                data.set(index + ".world", p.getLevel().getName());
                data.set(index + ".img", name);
                data.set(index + ".cmds", new ArrayList<>());
                data.save();
                return;
            }
            key = iterator.next();
        } while (!key.equalsIgnoreCase(index));

        datalist = data.getStringList(index + ".loclist");
        datalist.add(nowLoc.getX() + "," + nowLoc.getY() + "," + nowLoc.getZ());
        data.set(index + ".loclist", datalist);
        data.save();
    }
}
