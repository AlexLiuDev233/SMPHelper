package me.alexliudev.bukkitPlugins.spectator;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class PlayerSpectatorData {
    private double X;
    private double Y;
    private double Z;
    private String world;
    private String originalPermissionGroup;
}
