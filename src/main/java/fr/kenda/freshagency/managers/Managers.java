package fr.kenda.freshagency.managers;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.managers.back.BackGlitchManager;
import fr.kenda.freshagency.managers.back.BackManager;
import fr.kenda.freshagency.managers.bossbar.BossbarManager;
import fr.kenda.freshagency.managers.game.GamePlayerManager;
import fr.kenda.freshagency.managers.game.GameWorldManager;
import fr.kenda.freshagency.managers.jump.JumpManager;

import java.util.HashMap;
import java.util.Map;

public class Managers {

    private final Map<Class<? extends IManager>, IManager> managers = new HashMap<>();

    public Managers(FreshAgencyRunner instance) {
        registerManager(new FileManager());
        registerManager(new CommandManager(instance));
        registerManager(new EventManager(instance));
        registerManager(new GamePlayerManager(instance));
        //registerManager(new RollEventManager(instance));
        //registerManager(new RollWinManager(instance));
        registerManager(new JumpManager());
        registerManager(new GameWorldManager(instance));
        registerManager(new BackGlitchManager(instance));
        registerManager(new BackManager(instance));
        registerManager(new BossbarManager());
    }

    <T extends IManager> void registerManager(T managerInstance) {
        managers.putIfAbsent(managerInstance.getClass(), managerInstance);
    }

    public <T extends IManager> T getManager(Class<T> managerClass) {
        return managerClass.cast(managers.get(managerClass));
    }

    public void registerAll() {
        managers.forEach((aClass, iManager) -> iManager.register());
    }
}
