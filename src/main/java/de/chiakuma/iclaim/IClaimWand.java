package de.chiakuma.iclaim;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;
import java.util.UUID;

public class IClaimWand extends ItemStack implements Listener {

    Component displayName =
            Component.text("[", Style.style().decoration(TextDecoration.ITALIC, false).build())
                    .append(Component.text("IClaimWand", Style.style().color(TextColor.color(255, 165, 50)).build()))
                    .append(Component.text("]", Style.style().decoration(TextDecoration.ITALIC, false).build()));
    AttributeModifier data = new AttributeModifier(UUID.randomUUID(), "IClaimWand_372475328027476700378", 1,  AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);

    /***
     * Defines, Initializes the ItemStack of the IClaimWand to get it back or use it in other places
     * it also adds all the important Events for this tool itself.
     */
    public IClaimWand()
    {
        //Define IClaim Tool
        super(Material.GOLDEN_SHOVEL, 1);
        ItemMeta meta = this.getItemMeta();
        meta.displayName(displayName);
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
                        && Objects.equals(item.getItemMeta().displayName(), displayName)
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

    //All the events this Item triggers and uses
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        ClaimSelect cs = IClaim.perPlayerInstances.get(e.getPlayer().getUniqueId());
        boolean isCancelled = false;
        e.setCancelled(true);
        isCancelled = this.checkItem(e.getItem());
        if (isCancelled)
        {
            //Debug
            e.getPlayer().sendMessage(e.getAction().toString());
            switch (e.getAction())
            {
                case LEFT_CLICK_AIR:
                    //TODO
                    cs.setLocation(1, cs.rayCastPos());
                    break;
                case RIGHT_CLICK_AIR:
                    //TODO
                    cs.setLocation(2, cs.rayCastPos());
                    break;
                case LEFT_CLICK_BLOCK:
                    //TODO
                    e.getPlayer().sendMessage("Hallo");
                    cs.setLocation(1, e.getClickedBlock().getLocation());
                    break;
                case RIGHT_CLICK_BLOCK:
                    //TODO
                    e.getPlayer().sendMessage("Tschüss");
                    cs.setLocation(2, e.getClickedBlock().getLocation());
                    break;
            }
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
