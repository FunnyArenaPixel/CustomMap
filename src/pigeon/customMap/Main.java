package pigeon.customMap;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockItemFrame;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockEvent;
import cn.nukkit.event.block.BlockUpdateEvent;
import cn.nukkit.event.block.ItemFrameDropItemEvent;
import cn.nukkit.event.level.LevelLoadEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginBase;
import pigeon.customMap.files.Config;
import pigeon.customMap.model.Map;
import pigeon.customMap.packet.ImagePacket;
import pigeon.customMap.uilts.Build;

import java.io.File;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("DuplicatedCode")
public class Main extends PluginBase implements Listener {

    private static Main plugin;

    public static Main getPlugin() {
        return plugin;
    }

    public static HashMap<Player, Location> onClick = new HashMap<>();
    public List<Player> cooldown = new ArrayList<>();

    @Override
    public void onEnable() {
        plugin = this;
        Config.reload();
        Map.reloadData();

        this.getServer().getCommandMap().register("map", new Cmd());
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getLogger().info("加载成功...");
        ImagePacket.reload();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                ImagePacket.sendPacket();
            }
        }, 1L, Config.getConfig().getInt("Delay", 5) * 1000L);
    }

    @EventHandler
    public void onWorldLoad(LevelLoadEvent event) {
        String world = event.getLevel().getName();
        this.getLogger().info(world + "世界已经加载,正在载入世界内的地图");
        File file = new File(this.getDataFolder() + "/data.yml");
        cn.nukkit.utils.Config data = new cn.nukkit.utils.Config(file);
        Map.reloadData();
        for (String key : data.getKeys(false)) {
            if (data.getString(key + ".world").equalsIgnoreCase(world)) {
                ImagePacket.add(key);
                this.getLogger().info(world + "的地图已经加载完成");
            }
        }
    }

    public void cancelled(BlockEvent event) {
        Block block = event.getBlock();
        if (block instanceof BlockItemFrame) {
            Location loc = block.getLocation();
            BlockEntity be = loc.getLevel().getBlockEntity(new Vector3(loc.getX(), loc.getY(), loc.getZ()));
            if (be instanceof BlockEntityItemFrame) {
                BlockEntityItemFrame frame = (BlockEntityItemFrame) be;
                if (Map.containMap(frame.getLocation())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        this.cancelled(event);
    }

    @EventHandler
    public void onDrop(ItemFrameDropItemEvent event) {
        if (Map.containMap(event.getItemFrame().getLocation())) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void onUpdate(BlockUpdateEvent event) {
        this.cancelled(event);
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) throws IOException {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Item item = event.getItem();
        if (block instanceof BlockItemFrame) {
            Location loc = block.getLocation();
            BlockEntity be = loc.getLevel().getBlockEntity(new Vector3(loc.getX(), loc.getY(), loc.getZ()));
            if (be instanceof BlockEntityItemFrame) {
                BlockEntityItemFrame frame = (BlockEntityItemFrame) be;
                if (Map.containMap(frame.getLocation())) {
                    Map.runCmd(player, frame.getLocation());
                    event.setCancelled(true);
                }
            }
        }

        if (event.getAction().equals(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) && item.getCustomName().equalsIgnoreCase("§c魔法棒")) {
            if (this.cooldown.contains(player)) {
                return;
            }

            this.cooldown.add(player);
            this.getServer().getScheduler().scheduleDelayedTask(this, () -> cooldown.remove(player), 10);
            event.setCancelled();
            if (!onClick.containsKey(player)) {
                onClick.put(player, block.getLocation());
                player.sendMessage("§e你点击了右下角,请再点击左上角");
            } else {
                player.sendMessage("§e你点击了左上角,开始创建...");
                if (!Build.buildImage(player, event.getFace(), item.getLore()[0], onClick.get(player), block.getLocation(), item.getLore()[1])) {
                    player.sendMessage("§e当前两个点可能不在同一平面,清重新创建");
                } else {
                    player.getInventory().setItemInHand(new Item(0));
                }
                onClick.remove(player);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (BlockEntity blockEntity : player.getLevel().getBlockEntities().values()) {
            if (blockEntity instanceof BlockEntityItemFrame && blockEntity.distance(player) < 256) {
                ((BlockEntityItemFrame) blockEntity).spawnTo(player);
            }
        }
    }
}
