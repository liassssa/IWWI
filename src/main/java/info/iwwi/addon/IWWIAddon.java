package info.iwwi.addon;


import info.iwwi.addon.modules.AutoMineDownRTP;
import info.iwwi.addon.modules.BlockESP;
import info.iwwi.addon.modules.StashFinder;
import info.iwwi.addon.modules.MineToYMinus50;
import info.iwwi.addon.modules.ElytraAutoFly;
import info.iwwi.addon.modules.AutoSpawnerBreakerBaritone;
import info.iwwi.addon.modules.AutoSpawnerChestClicker;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class IWWIAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("IWWI");

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
        return "info.iwwi.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("liassssa", "IWWI");
    }
}
