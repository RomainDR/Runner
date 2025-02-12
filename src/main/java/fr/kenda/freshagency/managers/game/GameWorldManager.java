package fr.kenda.freshagency.managers.game;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.blocks.BlockSnap;
import fr.kenda.freshagency.database.Database;
import fr.kenda.freshagency.managers.AreaZone;
import fr.kenda.freshagency.managers.IManager;
import fr.kenda.freshagency.utils.Config;
import fr.kenda.freshagency.utils.Cuboid;
import fr.kenda.freshagency.utils.LocationTransform;
import fr.kenda.freshagency.utils.Messages;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class GameWorldManager implements IManager {

    private final Map<UUID, List<BlockSnap>> blockToRestore = new HashMap<>();
    private final Set<BlockSnap> originalMaps = new HashSet<>();
    private final Set<BlockSnap> protectedArea = new HashSet<>();
    private final List<AreaZone> protectedAreaZone = new ArrayList<>();
    private final Cuboid winArea;
    private final FreshAgencyRunner instance;
    private final Set<Material> blacklist;
    private GamePlayerManager gamePlayerManager;
    private double maxZ, minZ, maxZSpawn, minZSpawn;
    private Database database;

    public GameWorldManager(FreshAgencyRunner instance) {
        this.instance = instance;
        this.blacklist = Set.of(Material.BEDROCK, Material.BARREL, Config.getMaterial("pressure_plate"), Material.ITEM_FRAME);

        Location pos1 = LocationTransform.deserializeCoordinate("world", Config.getString("map.area_win_pos1"));
        Location pos2 = LocationTransform.deserializeCoordinate("world", Config.getString("map.area_win_pos2"));

        this.winArea = new Cuboid(pos1, pos2);
    }


    @Override
    public void register() {
        database = instance.getDatabases().getDatabase();
        saveMap("map.pos1", "map.pos2", originalMaps, true);
        saveMap("map.spawn.pos1", "map.spawn.pos2", protectedArea, false);
        saveMap("map.end.pos1", "map.end.pos2", protectedArea, false);
        createMaps();
        gamePlayerManager = instance.getManagers().getManager(GamePlayerManager.class);
        registerProtectedZone("map.spawn.pos1", "map.spawn.pos2");
        registerProtectedZone("map.end.pos1", "map.end.pos2");
        setBorderArea();
    }

    private void registerProtectedZone(String pos1, String pos2) {
        Location loc1 = LocationTransform.deserializeCoordinate("world", Config.getString(pos1));
        Location loc2 = LocationTransform.deserializeCoordinate("world", Config.getString(pos2));
        protectedAreaZone.add(new AreaZone(loc1, loc2));
    }

    private void saveMap(String pos1Key, String pos2Key, Set<BlockSnap> blockList, boolean showProgress) {
        World templateWorld = Bukkit.createWorld(new WorldCreator("template"));
        if (templateWorld == null) {
            Bukkit.getConsoleSender().sendMessage(Messages.transformColor(Messages.getPrefix() + "&cLe monde template n'a pas été trouvé."));
            return;
        }

        Location loc1 = LocationTransform.deserializeCoordinate(templateWorld.getName(), Config.getString(pos1Key));
        Location loc2 = LocationTransform.deserializeCoordinate(templateWorld.getName(), Config.getString(pos2Key));
        Cuboid cuboid = new Cuboid(loc1, loc2);

        List<Block> blocks = cuboid.getBlocksWithout(blacklist);
        int total = blocks.size();

        if (showProgress) {
            int progressInterval = total / 4;
            for (int i = 0; i < total; i++) {
                if (i % progressInterval == 0) {
                    Bukkit.getConsoleSender().sendMessage(Messages.transformColor(Messages.getPrefix() + "&a" + (i * 100 / total) + "% des blocs sauvegardés"));
                }
                Block block = blocks.get(i);
                if (block.getType() != Material.AIR && block.getType() != Material.BARRIER) {
                    blockList.add(new BlockSnap(block));
                }
            }
        } else {
            blocks.stream()
                    .filter(block -> block.getType() != Material.AIR && block.getType() != Material.BARRIER)
                    .forEach(block -> blockList.add(new BlockSnap(block)));
        }
        Bukkit.unloadWorld(templateWorld.getName(), false);
    }

    public void createMaps() {
        int numberMap = Config.getInt("map.number_map");
        instance.getServer().setMaxPlayers(numberMap);

        int totalMap = database.getTotalMap();
        for (int i = 1; i <= numberMap; i++) {
            String mapName = String.format(Config.getString("map.map_name").replace("%number%", "%02d"), i + totalMap);
            createWorld(mapName);
        }
    }

    private void createWorld(String worldName) {
        try {
            File source = new File(Bukkit.getWorldContainer(), "template");
            File target = new File(Bukkit.getWorldContainer(), worldName);
            copyWorld(source, target);
            World world = Bukkit.createWorld(new WorldCreator(worldName));
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.DO_FIRE_TICK, false);
            world.setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, false);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
            world.setAutoSave(false);

            winArea.getBlocksWithout(blacklist).forEach(block ->
                    world.getBlockAt(block.getLocation()).setType(Material.END_PORTAL)
            );

            blockToRestore.put(world.getUID(), new ArrayList<>());
            if (!database.isGameExist(worldName))
                database.createGame(worldName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyWorld(File source, File target) throws IOException {
        if (!target.exists()) {
            target.mkdirs();
        }
        for (String file : Objects.requireNonNull(source.list())) {
            if ("uid.dat".equalsIgnoreCase(file)) continue;
            File srcFile = new File(source, file);
            File destFile = new File(target, file);
            if (srcFile.isDirectory()) {
                copyWorld(srcFile, destFile);
            } else {
                Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    public void recreate(World baseWorld) {
        blockToRestore.get(baseWorld.getUID()).clear();
        blockToRestore.remove(baseWorld.getUID());
        String worldName = baseWorld.getName();
        File targetWorld = new File(Bukkit.getWorldContainer(), worldName);

        if (Bukkit.unloadWorld(baseWorld, false)) {
            Bukkit.getConsoleSender().sendMessage(Messages.transformColor("&aMonde déchargé avec succès : " + worldName));

            if (deleteWorld(targetWorld)) {
                Bukkit.getConsoleSender().sendMessage(Messages.transformColor("&aMonde supprimé avec succès : " + worldName));

                Bukkit.getScheduler().runTaskLater(instance, () -> createWorld(worldName), 20L);
            } else {
                Bukkit.getConsoleSender().sendMessage(Messages.transformColor("&cÉchec de la suppression du monde : " + worldName));
            }
        } else {
            Bukkit.getConsoleSender().sendMessage(Messages.transformColor("&cÉchec du déchargement du monde : " + worldName));
        }
    }

    private boolean deleteWorld(File target) {
        if (target.exists()) {
            for (File file : Objects.requireNonNull(target.listFiles())) {
                if (file.isDirectory()) {
                    deleteWorld(file);
                } else {
                    file.delete();
                }
            }
            return target.delete();
        } else {
            Bukkit.getConsoleSender().sendMessage(Messages.transformColor("&cLe dossier cible n'existe pas: " + target.getPath()));
        }
        return false;
    }

    public boolean isWorldContainsInGame(World world) {
        return blockToRestore.containsKey(world.getUID());
    }

    public void regenerateMap(World worldOfPlayer, int percent) {
        if (percent <= 0) percent = 0;
        if (percent >= 100) percent = 100;

        UUID worldId = worldOfPlayer.getUID();
        List<BlockSnap> blocksToRestore = blockToRestore.get(worldId);
        blocksToRestore.sort(Comparator.comparingInt(bs -> bs.getLocation().getBlockY()));

        int blockRestoreCount = (percent * originalMaps.size()) / 100;
        int numberPerSecond = (int) getNumberPerSecond(percent);

        new BukkitRunnable() {
            final int sqrt = (int) Math.sqrt(numberPerSecond);
            int updatedBlocks = 0;

            @Override
            public void run() {
                if (updatedBlocks >= blockRestoreCount || updatedBlocks >= originalMaps.size() || blocksToRestore.isEmpty()) {
                    this.cancel();
                    gamePlayerManager.getDataGameFromPlayer(worldOfPlayer.getPlayers().get(0)).updatePercent();
                    return;
                }

                for (int i = 0; i < sqrt && !blocksToRestore.isEmpty(); i++) {
                    for (int y = 0; y < sqrt && !blocksToRestore.isEmpty(); y++) {
                        BlockSnap bs = blocksToRestore.remove(0);
                        Location loc = bs.getLocation();
                        Material material = bs.getMaterial();
                        Bukkit.getScheduler().runTask(FreshAgencyRunner.getInstance(), () -> Objects.requireNonNull(Bukkit.getWorld(worldId)).getBlockAt(loc).setType(material));
                    }
                    updatedBlocks++;
                    gamePlayerManager.getDataGameFromPlayer(worldOfPlayer.getPlayers().get(0)).updatePercent();
                }
            }
        }.runTaskTimer(instance, 0L, 2);
    }

    private double getNumberPerSecond(int percent) {
        NavigableMap<Integer, String> speedMap = new TreeMap<>();
        speedMap.put(75, "map.repair_speed.75");
        speedMap.put(50, "map.repair_speed.50");
        speedMap.put(25, "map.repair_speed.25");
        speedMap.put(0, "map.repair_speed.default");

        return Config.getInt(speedMap.floorEntry(percent).getValue());
    }

    private void setBorderArea() {
        Location pos1 = LocationTransform.deserializeCoordinate("world", Config.getString("map.spawn.pos1"));
        Location pos2 = LocationTransform.deserializeCoordinate("world", Config.getString("map.spawn.pos2"));
        minZSpawn = Math.min(pos1.getZ(), pos2.getZ());
        maxZSpawn = Math.max(pos1.getZ(), pos2.getZ());

        minZ = Config.getInt("map.min_z");
        maxZ = Config.getInt("map.max_z");
    }

    public double getMaxZ() {
        return maxZ;
    }

    public double getMinZSpawn() {
        return minZSpawn;
    }

    public double getMaxZSpawn() {
        return maxZSpawn;
    }

    public double getMinZ() {
        return minZ;
    }

    public Cuboid getWinArea() {
        return winArea;
    }

    public boolean isInArea(Location newLocation) {
        final double z = newLocation.getZ();
        if (newLocation.getX() > 15) {
            return z >= minZ && z <= maxZ;
        } else {
            return z >= minZSpawn && z <= maxZSpawn;
        }
    }

    public float getPercentOfMap(UUID world) {
        final float total = originalMaps.size();
        final float breaked = blockToRestore.get(world).size();

        return ((total - breaked) * 100) / total;
    }

    public boolean isBlockContainsInProtectedArea(BlockSnap block) {

        return protectedArea.contains(block)/* || isContainsInAreaZone(block.getLocation())*/;
    }

    public boolean isBlockContainsInOriginalMap(BlockSnap block) {
        return originalMaps.contains(block);
    }

    public void addBlockToRestore(BlockSnap block) {
        blockToRestore.computeIfAbsent(block.getWorldId(), k -> new ArrayList<>());
        blockToRestore.get(block.getWorldId()).add(block);
    }

    public void deleteWorldsGame() {
        database.clearAllGames();
        List<File> worldsFolder = new ArrayList<>();
        for (UUID worldId : blockToRestore.keySet()) {
            World world = Bukkit.getWorld(worldId);
            if (world == null) {
                Bukkit.getConsoleSender().sendMessage(Messages.transformColor("&cLe monde avec l'ID `" + worldId + "` est null"));
                continue;
            }
            worldsFolder.add(world.getWorldFolder());
            Bukkit.unloadWorld(world, false); // Décharge le monde sans sauvegarder les modifications
        }
        worldsFolder.forEach(this::deleteWorld);
    }

    public World getEmptyWorld() {
        return Bukkit.getWorlds().stream()
                .filter(world -> world.getPlayers().isEmpty() // Aucun joueur dans le monde
                        && world.getName().startsWith(Config.getString("map.map_name").substring(0, Config.getString("map.map_name").indexOf("%") - 1))) // Le nom n'est pas "world
                .findFirst() // Trouve le premier monde correspondant aux critères
                .orElse(null); // Retourne null si aucun monde n'est trouvé
    }
}
