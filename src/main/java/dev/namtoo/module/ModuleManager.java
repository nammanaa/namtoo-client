// lang: Java  file: ModuleManager.java  target: Fabric 1.21.1
package dev.namtoo.module;

import dev.namtoo.module.combat.*;
import dev.namtoo.module.movement.*;
import dev.namtoo.module.player.*;
import dev.namtoo.module.visual.*;
import dev.namtoo.module.world.*;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void init() {
        // Combat
        register(new KillAura());
        register(new AimAssist());
        register(new Velocity());
        // Visual
        register(new ESP());
        register(new FullBright());
        // Movement
        register(new Scaffold());
        register(new Sprint());
        // Player
        register(new FastPlace());
        register(new Freecam());
        // World
        register(new SusChunkFinder());
    }

    private void register(Module m) { modules.add(m); }

    public List<Module> getAll()              { return modules; }

    public Module getByName(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public List<Module> getByCategory(String cat) {
        return modules.stream()
                .filter(m -> m.getCategory().equalsIgnoreCase(cat))
                .toList();
    }

    public List<String> getCategories() {
        return modules.stream()
                .map(Module::getCategory)
                .distinct()
                .toList();
    }

    public void onKey(int keyCode) {
        for (Module m : modules)
            if (m.getKey() == keyCode)
                m.toggle();
    }
}
