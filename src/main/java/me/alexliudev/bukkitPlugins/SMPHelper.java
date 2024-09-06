package me.alexliudev.bukkitPlugins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import me.alexliudev.bukkitPlugins.spectator.SwitchGameMode;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class SMPHelper extends JavaPlugin {
    private FileConfiguration newConfig;

    @NotNull
    @Override
    public FileConfiguration getConfig() {
        if (newConfig == null)
            reloadConfig();
        return newConfig;
    }

    @Override
    public void reloadConfig() {
        newConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        boolean edited = false;
        if (!newConfig.isSet("Spectator_Module_Switch_Permission_Group")) {
            newConfig.set("Spectator_Module_Switch_Permission_Group", "specPlayers");
            edited = true;
        }
        if (!newConfig.isBoolean("Recipe.embeddedRecipes")) {
            newConfig.set("Recipe.embeddedRecipes", true);
            edited = true;
        }
        if (edited) {
            saveConfig();
            getLogger().warning("配置文件中的某些值类型非法!");
            getLogger().warning("已经自动校正了这些值!");
        }
    }

    @Getter
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @Getter
    private static SMPHelper helper;
    @Getter
    private static Permission perm;
    @Getter
    private RecipeManager recipeManager;

    private void createDirectory(String name) {
        File directory = new File(SMPHelper.getHelper().getDataFolder(), name);
        if (!directory.exists() && !directory.mkdirs())
            Bukkit.getPluginManager().disablePlugin(this);
    }

    @Override
    public void onEnable() {
        try {
            // Plugin startup logic
            helper = this;

            saveDefaultConfig();

            if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
                getLogger().severe("由于程序代码不完整，缺失前置插件");
                getLogger().severe("插件无法继续运行");
                getLogger().severe("请安装 Vault 插件");
                throw new RuntimeException("Need Depend: Vault API");
            }
            RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
            if (rsp == null) {
                getLogger().severe("由于程序代码不完整，缺失前置插件");
                getLogger().severe("插件无法继续运行");
                getLogger().severe("请安装 **任何** 兼容 Vault 的权限管理插件");
                throw new RuntimeException("Need Depend: Permission Manager Provider");
            }
            perm = rsp.getProvider();
            createDirectory("playerData");
            recipeManager = new RecipeManager();
            if (getConfig().getBoolean("Recipe.embeddedRecipes")) {
                recipeManager.embeddedRecipesInit();
            }

            recipeManager.configCraftingRecipesInit();
            getServer().getPluginManager().registerEvents(new SwitchGameMode(), this);
        } catch (Throwable t) {
            Bukkit.getPluginManager().disablePlugin(this);
            throw t;
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
    }
}
