package me.robertlit.hostilegolems;

import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class HostileGolems extends JavaPlugin implements Listener {

    private int chance;
    private Collection<EntityType> MOB_TYPES;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        chance = getConfig().getInt("chance", 10);
        MOB_TYPES = getConfig().getStringList("types").stream().map(EntityType::valueOf).collect(Collectors.toSet());
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.IRON_GOLEM || !MOB_TYPES.contains(event.getEntityType())) {
            return;
        }
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }
        if (!event.getLocation().getWorld().getName().equals("world")) {
            return;
        }
        Random random = ThreadLocalRandom.current();
        if (!(random.nextInt(100) < chance)) {
            return;
        }
        event.setCancelled(true);
        IronGolem golem = (IronGolem) event.getLocation().getWorld().spawnEntity(event.getLocation(), EntityType.IRON_GOLEM);
        golem.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false));
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof IronGolem)) {
            Entity target = event.getTarget();
            if (!(target instanceof IronGolem)) {
                return;
            }
            IronGolem golemTarget = (IronGolem) target;
            if (golemTarget.hasPotionEffect(PotionEffectType.GLOWING)) {
                event.setCancelled(true);
            }
            return;
        }
        IronGolem golem = (IronGolem) entity;
        if (!golem.hasPotionEffect(PotionEffectType.GLOWING)) {
            return;
        }
        if (!(event.getTarget() instanceof Player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        for (Entity entity : player.getNearbyEntities(16, 16, 16)) {
            if (!(entity instanceof IronGolem)) {
                continue;
            }
            IronGolem golem = (IronGolem) entity;
            if (!golem.hasPotionEffect(PotionEffectType.GLOWING)) {
                continue;
            }
            if (golem.getTarget() != null) {
                continue;
            }
            golem.setTarget(player);
        }
    }
}
