package me.alexliudev.bukkitPlugins.spectator;

import me.alexliudev.bukkitPlugins.SMPHelper;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class SwitchGameMode implements Listener {

    @EventHandler
    public void onChatMessage(AsyncPlayerChatEvent event) {
        if (event.getMessage().equalsIgnoreCase("!s")) {
            File dataFile = new File(new File(SMPHelper.getHelper().getDataFolder(), "playerData"),
                    event.getPlayer().getUniqueId() +".json");
            if (dataFile.isDirectory()) {
                try {
                    Files.delete(dataFile.toPath());
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                    Bukkit.broadcastMessage("操作失败！无法读取/写入坐标");
                    Bukkit.broadcastMessage("出现错误："+e);
                    return;
                }
            }
            if (!dataFile.exists()) {
                try {
                    if (!dataFile.createNewFile()) {
                        Bukkit.broadcastMessage("操作失败！无法读取/写入坐标");
                        Bukkit.broadcastMessage("出现未知错误");
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                    Bukkit.broadcastMessage("操作失败！无法读取/写入坐标");
                    Bukkit.broadcastMessage("出现错误："+e);
                    return;
                }
            }
            if (event.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
                // 已经在旁观
                PlayerSpectatorData data;
                try {
                    data = SMPHelper.getGson().fromJson(
                            FileUtils.readFileToString(dataFile, StandardCharsets.UTF_8),
                            PlayerSpectatorData.class
                    );
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                    Bukkit.broadcastMessage("操作失败！无法读取/写入坐标");
                    Bukkit.broadcastMessage("出现错误："+e);
                    return;
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        event.getPlayer().teleport(new Location(Bukkit.getWorld(data.getWorld()), data.getX(), data.getY(), data.getZ()));
                        event.getPlayer().setGameMode(GameMode.SURVIVAL);
                        SMPHelper.getPerm().playerAddGroup(null, event.getPlayer(), data.getOriginalPermissionGroup());
                        SMPHelper.getPerm().playerRemoveGroup(null, event.getPlayer(), SMPHelper.getHelper().getConfig().getString("Spectator_Module_Switch_Permission_Group"));
                    }
                }.runTask(SMPHelper.getHelper());
            } else {
                // 不在旁观
                PlayerSpectatorData data = new PlayerSpectatorData();
                data.setWorld(event.getPlayer().getWorld().getName());
                data.setX(event.getPlayer().getLocation().getX());
                data.setY(event.getPlayer().getLocation().getY());
                data.setZ(event.getPlayer().getLocation().getZ());
                data.setOriginalPermissionGroup(SMPHelper.getPerm().getPrimaryGroup(event.getPlayer()));
                try {
                    FileUtils.writeStringToFile(dataFile, SMPHelper.getGson().toJson(data), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                    Bukkit.broadcastMessage("操作失败！无法读取/写入坐标");
                    Bukkit.broadcastMessage("出现错误："+e);
                    return;
                }

                SMPHelper.getPerm().playerAddGroup(null, event.getPlayer(), SMPHelper.getHelper().getConfig().getString("Spectator_Module_Switch_Permission_Group"));
                SMPHelper.getPerm().playerRemoveGroup(null, event.getPlayer(), SMPHelper.getPerm().getPrimaryGroup(event.getPlayer()));

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        event.getPlayer().setGameMode(GameMode.SPECTATOR);
                    }
                }.runTask(SMPHelper.getHelper());
            }
        }
    }
}
