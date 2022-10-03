package pigeon.customMap;

import cn.nukkit.Player;
import cn.nukkit.form.element.*;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import pigeon.customMap.files.Config;
import pigeon.customMap.libs.MyForm;
import pigeon.customMap.model.Map;
import pigeon.customMap.uilts.Build;
import pigeon.customMap.uilts.Image;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Gui {

    public static void sendGui(Player player) {
        List<ElementButton> buttons = new ArrayList<>();
        buttons.add(new ElementButton("创建图片"));
        buttons.add(new ElementButton("删除图片"));
        buttons.add(new ElementButton("Url下载图片"));
        buttons.add(new ElementButton("重载插件"));
        FormWindowSimple simple = new FormWindowSimple("自定义图片", "", buttons);
        MyForm form = new MyForm(player, simple) {
            @Override
            public void call() {
                switch (getButtonText()) {
                    case "创建图片":
                        sendCreateGui(player);
                        break;
                    case "删除图片":
                        sendRemoveGui(player);
                        break;
                    case "Url下载图片":
                        sendDownloadGui(player);
                        break;
                    case "重载插件":
                        Config.reload();
                        Map.reloadData();
                        player.sendMessage("§e重载成功");
                        break;
                    default:
                        break;
                }
            }
        };
        form.sendToPlayer();
    }

    public static void sendCreateGui(Player player) {
        String[] files = (new File(Main.getPlugin().getDataFolder() + "/images")).list();
        if (files != null) {
            List<ElementButton> buttons = new ArrayList<>();
            for (String filename : files) {
                buttons.add(new ElementButton(filename));
            }
            FormWindowSimple simple = new FormWindowSimple("选择图片", "图片列表", buttons);
            MyForm form = new MyForm(player, simple) {
                @Override
                public void call() {
                    sendInputIndexGui(player, getButtonText());
                }
            };
            form.sendToPlayer();
        }
    }

    public static void sendInputIndexGui(Player player, String name) {
        List<Element> el = new ArrayList<>();
        el.add(new ElementLabel("输入编号(随意不重复即可)"));
        el.add(new ElementLabel(name));
        el.add(new ElementInput(" "));
        FormWindowCustom custom = new FormWindowCustom("输入编号", el);
        MyForm form = new MyForm(player, custom) {
            @Override
            public void call() {
                String index = this.getSectionString(2);
                File file = new File(Main.getPlugin().getDataFolder() + "/data.yml");
                cn.nukkit.utils.Config data = new cn.nukkit.utils.Config(file);
                if (index.equalsIgnoreCase("")) {
                    player.sendMessage("§e未填写编号");
                } else {
                    if (data.exists(index)) {
                        player.sendMessage("§e此编号已存在");
                    } else {
                        try {
                            Build.giveMap(player, this.getSectionString(1), index);
                            List<Element> el = new ArrayList<>();
                            el.add(new ElementLabel("§c图片已经发放到了你的背包!"));
                            el.add(new ElementLabel("§c请先确定你要摆放的平面"));
                            el.add(new ElementLabel("§c然后首先点击§e右下角"));
                            el.add(new ElementLabel("§c然后点击§e左上角"));
                            el.add(new ElementLabel("§b恭喜你创建成功!"));
                            player.showFormWindow(new FormWindowCustom("图片创建提示", el));
                        } catch (IOException error) {
                            error.printStackTrace();
                        }
                    }
                }
            }
        };
        form.sendToPlayer();
    }

    public static void sendRemoveGui(Player player) {
        cn.nukkit.utils.Config data = new cn.nukkit.utils.Config(new File(Main.getPlugin().getDataFolder() + "/data.yml"));
        List<ElementButton> buttons = new ArrayList<>();
        for (String key : data.getKeys(false)) {
            buttons.add(new ElementButton(key));
        }
        FormWindowSimple simple = new FormWindowSimple("选择你要删除的图片", "", buttons);
        MyForm form = new MyForm(player, simple) {
            @Override
            public void call() {
                String index = getButtonText();
                Map.clearMap(player, index);
            }
        };
        form.sendToPlayer();
    }

    public static void sendDownloadGui(Player player) {
        List<Element> el = new ArrayList<>();
        el.add(new ElementInput("图片链接:"));
        el.add(new ElementInput("保存文件名:"));
        ElementDropdown ed = new ElementDropdown("扩展名");
        ed.addOption("jpg", true);
        ed.addOption("png", false);
        ed.addOption("jpeg", false);
        el.add(ed);
        FormWindowCustom custom = new FormWindowCustom("下载图片", el);
        MyForm form = new MyForm(player, custom) {
            @Override
            public void call() {
                String url = getSectionString(0);
                String name = getSectionString(1);
                if (!url.equals("") && !name.equals("")) {
                    String suffix = getSectionString(2);

                    Main.getPlugin().getLogger().info(suffix);

                    if ((new File(Main.getPlugin().getDataFolder() + "/images/" + name + "." + suffix)).exists()) {
                        player.sendMessage("§e图片名已存在");
                    } else {
                        try {
                            Image.download(url, name + "." + suffix, Main.getPlugin().getDataFolder() + "/images/");
                            player.sendMessage("§e图片下载中,你可以创建下载到的图片了!");
                        } catch (Exception error) {
                            error.printStackTrace();
                        }
                    }
                } else {
                    player.sendMessage("§e内容未填写");
                }
            }
        };
        form.sendToPlayer();
    }
}
