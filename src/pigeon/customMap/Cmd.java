package pigeon.customMap;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import pigeon.customMap.files.Config;
import pigeon.customMap.model.Map;
import pigeon.customMap.packet.ImagePacket;

public class Cmd extends Command {

    public Cmd() {
        super("map", "map", "/map");
    }

    public boolean execute(CommandSender commandSender, String s, String[] args) {
        if (commandSender instanceof ConsoleCommandSender) {
            Config.reload();
            Map.reloadData();
            ImagePacket.reload();
            commandSender.sendMessage("§e重载成功");
        } else {
            if (commandSender.isOp()) {
                Gui.sendGui((Player) commandSender);
            } else {
                commandSender.sendMessage("§e你不是管理员.");
            }
        }
        return true;
    }
}
