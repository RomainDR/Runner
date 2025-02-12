package fr.kenda.freshagency.managers;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.commands.*;

import java.util.Objects;

public class CommandManager implements IManager {

    private final FreshAgencyRunner instance;

    public CommandManager(FreshAgencyRunner instance) {
        this.instance = instance;
    }

    @Override
    public void register() {

        Objects.requireNonNull(instance.getCommand("back")).setExecutor(new BackCommand());
        Objects.requireNonNull(instance.getCommand("backglitch")).setExecutor(new BackGlitchCommand());
        Objects.requireNonNull(instance.getCommand("tnt")).setExecutor(new TntCommand());
        //Objects.requireNonNull(instance.getCommand("roll")).setExecutor(new RollCommand());
        Objects.requireNonNull(instance.getCommand("jump")).setExecutor(new JumpCommand());
        Objects.requireNonNull(instance.getCommand("debug")).setExecutor(new DebugCommand());
        Objects.requireNonNull(instance.getCommand("repair")).setExecutor(new RepairCommand());
        Objects.requireNonNull(instance.getCommand("cobweb")).setExecutor(new CobWebCommand());
        Objects.requireNonNull(instance.getCommand("win")).setExecutor(new WinCommand());
        Objects.requireNonNull(instance.getCommand("sendtitle")).setExecutor(new TitleCommand());
        Objects.requireNonNull(instance.getCommand("startlive")).setExecutor(new StartLiveCommand());
        Objects.requireNonNull(instance.getCommand("disconnectlive")).setExecutor(new DisconnectLiveCommand());
        Objects.requireNonNull(instance.getCommand("reset")).setExecutor(new ResetCommand());
        Objects.requireNonNull(instance.getCommand("spawnmonster")).setExecutor(new SpawnMonsterCommand());
        Objects.requireNonNull(instance.getCommand("fuse")).setExecutor(new FuseCommand());
        Objects.requireNonNull(instance.getCommand("tppercent")).setExecutor(new TpCommand());
        Objects.requireNonNull(instance.getCommand("give")).setExecutor(new GiveCommand());
    }
}
