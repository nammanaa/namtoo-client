// lang: Java  file: Module.java  target: Fabric 1.21.1
package dev.namtoo.module;

import dev.namtoo.NamtooClient;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {

    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String         name;
    private final String         category;
    private final int            defaultKey;
    private       int            key;
    private       boolean        enabled;
    private final List<Setting>  settings = new ArrayList<>();

    public Module(String name, String category, int defaultKey) {
        this.name       = name;
        this.category   = category;
        this.defaultKey = defaultKey;
        this.key        = defaultKey;
    }

    // ── lifecycle ──────────────────────────────────────────────────────────
    public void onEnable()  {}
    public void onDisable() {}

    public void toggle() {
        enabled = !enabled;
        if (enabled) {
            NamtooClient.INSTANCE.getEventBus().subscribe(this);
            onEnable();
        } else {
            onDisable();
            NamtooClient.INSTANCE.getEventBus().unsubscribe(this);
        }
    }

    // ── settings ──────────────────────────────────────────────────────────
    protected Setting addSetting(Setting s) { settings.add(s); return s; }
    public List<Setting> getSettings()      { return settings;           }

    // ── accessors ─────────────────────────────────────────────────────────
    public String  getName()     { return name;    }
    public String  getCategory() { return category; }
    public boolean isEnabled()   { return enabled;  }
    public int     getKey()      { return key;      }
    public void    setKey(int k) { key = k;         }

    // ── inner setting types ────────────────────────────────────────────────
    public static class Setting {
        public enum Type { TOGGLE, SLIDER, ENUM }
        public final String name;
        public final Type   type;
        // TOGGLE
        public boolean boolValue;
        // SLIDER
        public double  value, min, max, step;
        // ENUM
        public String[] options;
        public int      enumIndex;

        public Setting(String name, boolean def) {
            this.name = name; this.type = Type.TOGGLE; this.boolValue = def;
        }
        public Setting(String name, double def, double min, double max, double step) {
            this.name = name; this.type = Type.SLIDER;
            this.value = def; this.min = min; this.max = max; this.step = step;
        }
        public Setting(String name, String[] options, int def) {
            this.name = name; this.type = Type.ENUM;
            this.options = options; this.enumIndex = def;
        }
        public String currentEnum() { return options[enumIndex]; }
        public void   cycleEnum()   { enumIndex = (enumIndex + 1) % options.length; }
    }
}
