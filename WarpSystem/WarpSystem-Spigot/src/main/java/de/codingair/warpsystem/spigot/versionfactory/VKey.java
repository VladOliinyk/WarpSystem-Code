package de.codingair.warpsystem.spigot.versionfactory;

public enum VKey {
    CooldownButton(VFac.GUI),
    CommandButton(VFac.GUI),
    PermissionButton(VFac.GUI),
    CostsButton(VFac.GUI),
    MessageButton(VFac.GUI),

    RotationItemComponent(VFac.OBJECTS),
    AnimationPartColor(VFac.OBJECTS),
    AnimationPartSpeed(VFac.OBJECTS),
    ParticlesHandler(VFac.OBJECTS),
    ParticleOptionsSpeed(VFac.OBJECTS),
    ParticleOptionsColor(VFac.OBJECTS),
    PortalBlockEditorHandler(VFac.OBJECTS),
    RTP_Go_Command_Handler(VFac.OBJECTS),
    WarpGUIChoosePage(VFac.OBJECTS),
    CWarpSystem(VFac.OBJECTS),

    DestinationPageHandler(VFac.HANDLERS),
    TeleportCommandHandler(VFac.HANDLERS),
    WarpGUI(VFac.HANDLERS),
    PlayerWarpHandler(VFac.HANDLERS),
    RandomTeleportHandler(VFac.HANDLERS),
    TeleportCommandManager(VFac.HANDLERS),
    DimensionalCommandHandler(VFac.HANDLERS),

    Portal(VFac.FEATURE_OBJECTS),
    Shortcut(VFac.FEATURE_OBJECTS),
    WarpSign(VFac.FEATURE_OBJECTS),
    DimensionalPortal(VFac.FEATURE_OBJECTS + "dimensional."),
    ;

    private final String path;

    VKey(String path) {
        this.path = path;
    }

    VKey() {
        this(null);
    }

    public String getPath() {
        return path + name();
    }
}
