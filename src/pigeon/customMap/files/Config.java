package pigeon.customMap.files;

import pigeon.customMap.Main;

import java.io.File;

@SuppressWarnings({"ResultOfMethodCallIgnored"})
public class Config {

    private static cn.nukkit.utils.Config config;

    public static void reload() {
        if (!(new File(Main.getPlugin().getDataFolder() + "/Config.yml")).exists()) {
            Main.getPlugin().saveResource("Config.yml");
        }
        if (!(new File(Main.getPlugin().getDataFolder() + "/images")).exists()) {
            (new File(Main.getPlugin().getDataFolder() + "/images")).mkdirs();
        }
        config = new cn.nukkit.utils.Config(Main.getPlugin().getDataFolder() + "/Config.yml");
        if (!config.exists("Delay")) {
            config.set("Delay", 5);
            config.save();
        }
    }

    public static cn.nukkit.utils.Config getConfig() {
        return config;
    }
}
