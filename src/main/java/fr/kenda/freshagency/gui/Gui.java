package fr.kenda.freshagency.gui;

import fr.kenda.freshagency.FreshAgencyRunner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class Gui implements Listener {

    protected Inventory inventory;
    protected String title;
    protected Player owner;
    protected int size;


    /**
     * Create gui with title and number of row
     *
     * @param title Title of inventory
     * @param row   Row need in inventory
     */
    public Gui(String title, int row) {
        this(title, null, row);
    }

    /**
     * Create gui with title and number of row
     *
     * @param title Title of inventory
     * @param owner owner of inventory
     * @param row   Row need in inventory
     */
    public Gui(String title, Player owner, int row) {
        this.owner = owner;
        this.title = title;
        this.size = row * 9;

        Bukkit.getPluginManager().registerEvents(this, FreshAgencyRunner.getInstance());
    }

    /**
     * Create inventory with row
     *
     * @param row number of row
     */
    public Gui(int row) {
        this(null, null, row);
    }

    /**
     * Create inventory with row
     *
     * @param row number of row
     */
    public Gui(Player player, int row) {
        this(null, player, row);
    }

    /**
     * create inventory for player
     *
     * @param player Owner of inventory
     */
    public void create(Player player) {
        if (owner == null) owner = player;
        inventory = Bukkit.createInventory(owner, size, title);
        owner.openInventory(inventory);

        updateContent(mainMenu());
    }

    /**
     * create inventory for player
     */
    public void create() {
        inventory = Bukkit.createInventory(owner, size, title);
        owner.openInventory(inventory);

        updateContent(mainMenu());
    }

    /**
     * Update inventory with a content given
     *
     * @param content ItemStack[] given
     */
    public void updateContent(ItemStack[] content) {
        inventory.setContents(content);
    }

    /**
     * Set the title of inventory
     *
     * @param title title of inventory
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Main menu of inventory
     */
    public abstract ItemStack[] mainMenu();

    /**
     * Close inventory
     */
    public void close() {
        owner.closeInventory();
    }

    /**
     * Set the size of inventory
     *
     * @param row number of row
     */
    protected void setSize(int row) {
        this.size = row * 9;
        create();
    }

    @EventHandler
    public abstract void onClick(InventoryClickEvent e);

}
