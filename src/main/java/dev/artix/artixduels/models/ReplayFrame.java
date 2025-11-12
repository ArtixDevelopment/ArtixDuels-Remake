package dev.artix.artixduels.models;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Representa um frame (frame) de um replay.
 */
public class ReplayFrame {
    private long timestamp;
    private Map<UUID, EntitySnapshot> entitySnapshots;
    private Map<UUID, LocationSnapshot> locationSnapshots;
    private Map<UUID, InventorySnapshot> inventorySnapshots;

    public ReplayFrame(long timestamp) {
        this.timestamp = timestamp;
        this.entitySnapshots = new HashMap<>();
        this.locationSnapshots = new HashMap<>();
        this.inventorySnapshots = new HashMap<>();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<UUID, EntitySnapshot> getEntitySnapshots() {
        return entitySnapshots;
    }

    public Map<UUID, LocationSnapshot> getLocationSnapshots() {
        return locationSnapshots;
    }

    public Map<UUID, InventorySnapshot> getInventorySnapshots() {
        return inventorySnapshots;
    }

    public void addEntitySnapshot(UUID entityId, EntitySnapshot snapshot) {
        entitySnapshots.put(entityId, snapshot);
    }

    public void addLocationSnapshot(UUID entityId, LocationSnapshot snapshot) {
        locationSnapshots.put(entityId, snapshot);
    }

    public void addInventorySnapshot(UUID entityId, InventorySnapshot snapshot) {
        inventorySnapshots.put(entityId, snapshot);
    }

    public static class EntitySnapshot {
        private double health;
        private double maxHealth;
        private int foodLevel;
        private float saturation;

        public EntitySnapshot(double health, double maxHealth, int foodLevel, float saturation) {
            this.health = health;
            this.maxHealth = maxHealth;
            this.foodLevel = foodLevel;
            this.saturation = saturation;
        }

        public double getHealth() {
            return health;
        }

        public double getMaxHealth() {
            return maxHealth;
        }

        public int getFoodLevel() {
            return foodLevel;
        }

        public float getSaturation() {
            return saturation;
        }
    }

    public static class LocationSnapshot {
        private double x;
        private double y;
        private double z;
        private float yaw;
        private float pitch;

        public LocationSnapshot(double x, double y, double z, float yaw, float pitch) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public LocationSnapshot(org.bukkit.Location location) {
            this.x = location.getX();
            this.y = location.getY();
            this.z = location.getZ();
            this.yaw = location.getYaw();
            this.pitch = location.getPitch();
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        public float getYaw() {
            return yaw;
        }

        public float getPitch() {
            return pitch;
        }

        public org.bukkit.Location toLocation(org.bukkit.World world) {
            return new org.bukkit.Location(world, x, y, z, yaw, pitch);
        }
    }

    public static class InventorySnapshot {
        private ItemStack[] contents;
        private ItemStack[] armor;

        public InventorySnapshot(ItemStack[] contents, ItemStack[] armor) {
            this.contents = contents;
            this.armor = armor;
        }

        public ItemStack[] getContents() {
            return contents;
        }

        public ItemStack[] getArmor() {
            return armor;
        }
    }
}

