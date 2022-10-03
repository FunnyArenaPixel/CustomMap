package pigeon.customMap.libs;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.response.FormResponse;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseModal;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.utils.ConfigSection;
import com.google.gson.Gson;

@SuppressWarnings("all")
public abstract class MyForm extends FormWindow {

    private boolean closed = false;

    public Player player;
    public String playerName;
    private final FormWindow window;
    private ConfigSection section;

    public MyForm(Player player, FormWindow window) {
        this.player = player;
        this.playerName = player.getName();
        this.window = window;
        this.section = new ConfigSection();
    }

    public void setSection(ConfigSection section) {
        this.section = section;
    }

    public Number getSectionNumber(int key) {
        return this.getSectionNumber(key, 0);
    }

    public Number getSectionNumber(int key, Number defaultValue) {
        return (Number) this.section.get(String.valueOf(key), defaultValue);
    }

    public String getSectionString(int key) {
        return this.getSectionString(key, "null");
    }

    public String getSectionString(int key, String defaultValue) {
        return this.section.getString(String.valueOf(key), defaultValue);
    }

    public boolean getSectionBoolean(int key) {
        return this.getSectionBoolean(key, false);
    }

    public boolean getSectionBoolean(int key, boolean defaultValue) {
        return this.section.getBoolean(String.valueOf(key), defaultValue);
    }

    public int getButtonId() {
        return this.section.getInt("id");
    }

    public String getButtonText() {
        return this.section.getString("text");
    }

    public void sendToPlayer() {
        this.player.showFormWindow(this);
    }

    /**
     * 处理数据
     */
    public void setResponse(String data) {
        if (data.equals("null")) {
            this.closed = true;
        } else {
            this.window.setResponse(data);
            FormResponse response = this.window.getResponse();

            if (response instanceof FormResponseCustom) {
                /*
                 * ElementSlider Float
                 * ElementToggle Boolean
                 * 其他Element都是: String
                 */
                ((FormResponseCustom) response).getResponses().forEach((key, obj) -> {
                    this.section.set(String.valueOf(key), obj);
                });
            } else if (response instanceof FormResponseModal) {
                this.section.set("id", ((FormResponseModal) response).getClickedButtonId());
                this.section.set("text", ((FormResponseModal) response).getClickedButtonText());
            } else if (response instanceof FormResponseSimple) {
                ElementButton elementButton = ((FormResponseSimple) response).getClickedButton();
                this.section.put("id", ((FormResponseSimple) response).getClickedButtonId());
                this.section.put("text", elementButton.getText());
            }

            try {
                this.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void call();

    public FormResponse getResponse() {
        return this.window.getResponse();
    }

    @Override
    public String getJSONData() {
        return (new Gson()).toJson(this.window);
    }

    @Override
    public boolean wasClosed() {
        return this.closed;
    }
}
