package adris.altoclef.altomenu;

import adris.altoclef.altomenu.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

//todo:
// allow other mods to hook into our Cheat menu (Cheats, Baritone Settings, AnarchyClef Settings, Command System, etc)
// Support RusherHack AddonSystem by adding our Hacks to their ClickGUI using their API
public class Mod {


    private String name;
    private String displayName;
    private String description;
    private Category Category;
    private int key;
    private boolean enabled;
    public static int intset = 0;

    private List<Setting> settings = new ArrayList<Setting>();


    protected static MinecraftClient mc = MinecraftClient.getInstance();

    public boolean isNull() {
        return Objects.isNull(mc.world) || Objects.isNull(mc.player) || Objects.isNull(mc.player);
    }

    public void onWorldRender(MatrixStack matrices) {
    }

    public Mod(String name, String description, Category category) {
        this.name = name;
        this.displayName = name;
        this.description = description;
        this.Category = category;
    }

    protected void sendMsg(String message) {
        if (mc.player == null) return;
        mc.player.sendMessage(Text.of(message.replace(".", "§")));
    }


    public List<Setting> getSettings() {
        return settings;
    }

    public void addSetting(Setting setting) {
        settings.add(setting);
    }

    public void addSettings(Setting... settings) {
        for (Setting setting : settings) addSetting(setting);
    }
    public void toggle() {
        this.enabled = !this.enabled;

        if (enabled) onEnable();
        else onDisable();
    }

    public void onEnable() {

    }
    public boolean onBoolEnable() {
        return false;
    }
    public void onDisable() {

    }

    public Mod.Category getCategory() {
        return Category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (enabled) onEnable();
        else onDisable();
    }

    public String getDisplayName() {
        return displayName;
    }

    public String setDisplayname(String displayName) {
        this.displayName = displayName;
        return displayName;
    }

    public boolean onShitTick() {
        return false;
    }
    public boolean onCockAndBallTorture() {
       // if (mc.player.CockandBallTorture()) return true;
        return false;
    }


    public void onTick() {
    }
    public static void onStaticTick() {
    }
    public static boolean nullCheck(){
        return MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null;
    }
    public int onIntTick() {

        return intset;
    }
    
    public void registerSettings() {
        settings = List.of(Stream.of(this.getClass().getDeclaredFields(), Module.class.getDeclaredFields()).flatMap(Stream::of)
            .filter(field -> Setting.class.isAssignableFrom(field.getType()))
            .map(field -> {
                field.setAccessible(true);
                try {
                    return (Setting) field.get(this);
                } catch (IllegalAccessException ignore) {
                    return null;
                }
            }).toArray(Setting[]::new));
    }

    public boolean onActivate() {
        return false;
    }
    public boolean onDeactivate(){
        return true;
    }

    public enum Category {
        COMBAT("Combat"),
        MOVEMENT("Movement"),
        PLAYER("Player"),
        BARITONE("Baritone"),
        WORLD("World"),
        RENDER("Render"),
        EXPLOIT("Exploit"),
        DEVELOPMENT("Development");

        public final String name;

        private Category(String name) {
            this.name = name;
        }
    }
}
