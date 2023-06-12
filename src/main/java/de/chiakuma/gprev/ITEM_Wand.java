package de.chiakuma.gprev;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;
import java.util.UUID;

public class ITEM_Wand extends ItemStack implements Listener {

    Component name = Component.text("[").color(TextColor.color(255, 182, 0)).
            append(Component.text("IClaim").color(TextColor.color(255, 136, 43))).
            append(Component.text("Wand").color(TextColor.color(255, 84, 0))).
            append(Component.text("]").color(TextColor.color(255, 182, 0)));
    AttributeModifier data = new AttributeModifier(UUID.randomUUID(), "IClaimWand_372475328027476700378", 1,  AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);

    public ITEM_Wand()
    {
        //Definition of the Item
        super(Material.GOLDEN_SHOVEL, 1);
        this.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        ItemMeta meta = this.getItemMeta();
        meta.displayName(name);
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, data);
        this.setItemMeta(meta);
        //this.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        this.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 1);
    }

    /***
     * Checks if the Item is an IClaimWand
     * @param item
     * @return
     */
    public boolean checkItem(ItemStack item)
    {
        //Checks if the Item is really the IClaimWand
        try {
            if (item != null) {
                if (item.containsEnchantment(Enchantment.VANISHING_CURSE)
                        && item.hasItemMeta()
                        && Objects.equals(item.getItemMeta().displayName(), name)
                        && item.getItemMeta().hasAttributeModifiers()
                        && !Objects.requireNonNull(item.getItemMeta().getAttributeModifiers()).isEmpty()
                )
                {
                    for (AttributeModifier cache : Objects.requireNonNull(item.getItemMeta().getAttributeModifiers(Attribute.GENERIC_ATTACK_DAMAGE))) {
                        if (cache.getName().equals("IClaimWand_372475328027476700378")) {
                            return true;
                        }
                    }
                }
            }
        }
        catch (Exception e) {}
        return false;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e)
    {
        boolean isCancelled = false;
        e.setCancelled(true);
        isCancelled = this.checkItem(e.getItem());

        Player p = e.getPlayer();
        switch (e.getAction())
        {
            case LEFT_CLICK_AIR -> p.sendMessage("LEFT_CLICK_AIR");
            case LEFT_CLICK_BLOCK -> p.sendMessage("LEFT_CLICK_BLOCK");
            case RIGHT_CLICK_AIR -> p.sendMessage("RIGHT_CLICK_AIR");
            case RIGHT_CLICK_BLOCK -> p.sendMessage("RIGHT_CLICK_BLOCK");
            case PHYSICAL -> p.sendMessage("PHYSICAL");
        }

        e.setCancelled(isCancelled);
    }

    @EventHandler
    public void onPlayerBlockBreakEvent(BlockBreakEvent e)
    {
        boolean isCancelled = false;
        e.setCancelled(true);
        isCancelled = this.checkItem(e.getPlayer().getInventory().getItemInMainHand());
        e.setCancelled(isCancelled);
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(EntityDamageByEntityEvent e)
    {
        boolean isCancelled = false;
        e.setCancelled(true);
        if (e.getDamager() instanceof Player)
        {
            Player p = (Player) e.getDamager();
            isCancelled = this.checkItem(p.getInventory().getItemInMainHand());
        }
        e.setCancelled(isCancelled);
    }

    @EventHandler
    public void onPlayerItemDropEvent(PlayerDropItemEvent e)
    {
        if (this.checkItem(e.getItemDrop().getItemStack())) e.getItemDrop().remove();
    }
}
