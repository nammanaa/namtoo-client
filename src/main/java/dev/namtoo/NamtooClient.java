// lang: Java  file: NamtooClient.java  target: Fabric 1.21.1  MSVC/Gradle
package dev.namtoo;

import dev.namtoo.event.EventBus;
import dev.namtoo.gui.ClickGUI;
import dev.namtoo.gui.HUD;
import dev.namtoo.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class NamtooClient implements ClientModInitializer {

    public static final String NAME    = "Namtoo Client";
    public static final String VERSION = "1.0.0";

    public static NamtooClient     INSTANCE;
    public static MinecraftClient  mc = MinecraftClient.getInstance();

    private EventBus      eventBus;
    private ModuleManager moduleManager;
    private ClickGUI      clickGUI;
    private HUD           hud;

    @Override
    public void onInitializeClient() {
        INSTANCE      = this;
        eventBus      = new EventBus();
        moduleManager = new ModuleManager();
        clickGUI      = new ClickGUI();
        hud           = new HUD();

        moduleManager.init();
        System.out.println("[Namtoo] " + VERSION + " booted");
    }

    public EventBus      getEventBus()      { return eventBus;      }
    public ModuleManager getModuleManager() { return moduleManager; }
    public ClickGUI      getClickGUI()      { return clickGUI;      }
    public HUD           getHUD()           { return hud;           }
}
