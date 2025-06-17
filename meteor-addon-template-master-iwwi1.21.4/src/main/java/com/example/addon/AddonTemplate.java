package com.example.addon;


import com.example.addon.modules.AutoMineDownRTP;
import com.example.addon.modules.BlockESP;
import com.example.addon.modules.StashFinder;
import com.example.addon.modules.MineToYMinus50;
import com.example.addon.modules.ElytraAutoFly;
import com.example.addon.modules.AutoSpawnerBreakerBaritone;
import com.example.addon.modules.AutoSpawnerChestClicker;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class AddonTemplate extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("IWWI");
    public static final HudGroup HUD_GROUP = new HudGroup("IWWI");

    @Override
public void onInitialize() {
    Modules.get().add(new BlockESP());
        Modules.get().add(new ElytraAutoFly());
    Modules.get().add(new StashFinder());
    Modules.get().add(new MineToYMinus50());
    Modules.get().add(new AutoMineDownRTP());
        Modules.get().add(new AutoSpawnerBreakerBaritone());
                Modules.get().add(new AutoSpawnerChestClicker());

    

    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.example.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("MeteorDevelopment", "meteor-addon-template");
    }
}
