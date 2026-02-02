package me.shivam.godspear;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class GodSpear extends JavaPlugin implements Listener {

    private NamespacedKey godKey;

    @Override
    public void onEnable() {
        godKey = new NamespacedKey(this, "god_spear");
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("GodSpear Enabled");
    }

    /* ---------------- CREATE GOD SPEAR ---------------- */
    private ItemStack createGodSpear(UUID owner) {
        ItemStack spear = new ItemStack(Material.TRIDENT);
        ItemMeta meta = spear.getItemMeta();

        meta.setDisplayName("§4§lGOD SPEAR");
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(godKey, PersistentDataType.STRING, owner.toString());

        spear.setItemMeta(meta);
        return spear;
    }

    private boolean isGodSpear(ItemStack item) {
        if (item == null || item.getType() != Material.TRIDENT) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null &&
                meta.getPersistentDataContainer().has(godKey, PersistentDataType.STRING);
    }

    /* ---------------- COMMAND (ONLY ONE SPEAR) ---------------- */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        if (getConfig().getBoolean("given", false)) {
            player.sendMessage("§cGod Spear already exists on this server!");
            return true;
        }

        ItemStack spear = createGodSpear(player.getUniqueId());
        player.getInventory().addItem(spear);

        getConfig().set("given", true);
        getConfig().set("owner", player.getUniqueId().toString());
        saveConfig();

        Bukkit.broadcastMessage("§6⚔ §c§lGOD SPEAR §6has been claimed!");
        return true;
    }

    /* ---------------- IMMORTAL MODE ---------------- */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;

        if (!isGodSpear(p.getInventory().getItemInMainHand())) return;

        e.setCancelled(true);
        p.setHealth(p.getMaxHealth());
        p.setFireTicks(0);
    }

    /* ---------------- ONE SHOT DAMAGE ---------------- */
    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {

        Player attacker = null;

        if (e.getDamager() instanceof Player p) attacker = p;
        else if (e.getDamager() instanceof Trident t && t.getShooter() instanceof Player p) attacker = p;

        if (attacker == null) return;
        if (!isGodSpear(attacker.getInventory().getItemInMainHand())) return;

        if (e.getEntity() instanceof LivingEntity target) {
            target.getEquipment().setArmorContents(null);
            target.setHealth(0.0);
            e.setCancelled(true);
        }
    }
    }
