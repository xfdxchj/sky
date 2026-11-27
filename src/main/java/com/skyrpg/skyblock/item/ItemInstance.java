package com.skyrpg.skyblock.item;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Immutable, player-owned item stack state.
 *
 * <p>ItemDefinition remains the shared static definition. This class contains
 * only per-instance state and deliberately does not mutate a PlayerInventory.</p>
 */
public final class ItemInstance {
    public static final int MAX_ENCHANTMENTS = 64;
    public static final int MAX_ENCHANTMENT_LEVEL = 255;
    public static final int MAX_STARS = 10;
    public static final int MAX_CUSTOM_DATA_DEPTH = 16;
    public static final int MAX_CUSTOM_DATA_NODES = 1024;
    public static final int MAX_CUSTOM_DATA_KEY_LENGTH = 128;
    public static final int MAX_CUSTOM_DATA_STRING_LENGTH = 4096;

    private final UUID instanceId;
    private final Identifier definitionId;
    private final int count;
    private final String reforgeId;
    private final Map<Identifier, Integer> enchantments;
    private final int stars;
    private final int upgradeLevel;
    private final NbtCompound customData;

    private ItemInstance(
            UUID instanceId,
            Identifier definitionId,
            int count,
            String reforgeId,
            Map<Identifier, Integer> enchantments,
            int stars,
            int upgradeLevel,
            NbtCompound customData) {
        this.instanceId = Objects.requireNonNull(instanceId, "instanceId");
        this.definitionId = Objects.requireNonNull(definitionId, "definitionId");
        if (definitionId.getPath().isBlank()) {
            throw new IllegalArgumentException("definitionId must not be blank");
        }
        if (count < 1 || count > 64) {
            throw new IllegalArgumentException("count must be between 1 and 64 before definition validation");
        }
        if (stars < 0 || stars > MAX_STARS) {
            throw new IllegalArgumentException("stars must be between 0 and " + MAX_STARS);
        }
        if (upgradeLevel < 0) {
            throw new IllegalArgumentException("upgradeLevel must not be negative");
        }

        String normalizedReforge = reforgeId == null ? "" : reforgeId.trim().toLowerCase(java.util.Locale.ROOT);
        if (!normalizedReforge.isEmpty() && Identifier.tryParse(normalizedReforge) == null) {
            throw new IllegalArgumentException("Invalid reforge ID: " + reforgeId);
        }
        this.reforgeId = normalizedReforge;

        if (enchantments == null || enchantments.size() > MAX_ENCHANTMENTS) {
            throw new IllegalArgumentException("Invalid enchantment collection");
        }
        Map<Identifier, Integer> copiedEnchantments = new LinkedHashMap<>();
        enchantments.forEach((id, level) -> {
            if (id == null || id.getPath().isBlank() || level == null
                    || level < 1 || level > MAX_ENCHANTMENT_LEVEL) {
                throw new IllegalArgumentException("Enchantment IDs and levels must be valid");
            }
            if (copiedEnchantments.putIfAbsent(id, level) != null) {
                throw new IllegalArgumentException("Duplicate enchantment: " + id);
            }
        });
        this.enchantments = Map.copyOf(copiedEnchantments);
        this.stars = stars;
        this.upgradeLevel = upgradeLevel;
        this.customData = customData == null ? new NbtCompound() : customData.copy();
        validateCustomData(this.customData, 0, new int[]{0});

        if (hasInstanceState() && count != 1) {
            throw new IllegalArgumentException("Stateful item instances must have count 1");
        }
        this.count = count;
    }

    /** Creates a fresh, unmodified stack with a new stable identity. */
    public static ItemInstance create(ItemDefinition definition, int count) {
        Objects.requireNonNull(definition, "definition");
        Identifier id = parseDefinitionId(definition.id());
        ItemInstance instance = new ItemInstance(
                UUID.randomUUID(), id, count, "", Map.of(), 0, 0, new NbtCompound());
        instance.validateAgainst(definition);
        return instance;
    }

    /** Reconstitutes an instance while retaining its persisted identity. */
    public static ItemInstance restore(
            UUID instanceId, Identifier definitionId, int count, String reforgeId,
            Map<Identifier, Integer> enchantments, int stars, int upgradeLevel, NbtCompound customData,
            ItemDefinition definition) {
        ItemInstance instance = new ItemInstance(instanceId, definitionId, count, reforgeId,
                enchantments, stars, upgradeLevel, customData);
        instance.validateAgainst(definition);
        return instance;
    }

    public UUID instanceId() { return instanceId; }
    public Identifier definitionId() { return definitionId; }
    public int count() { return count; }
    public String reforgeId() { return reforgeId; }
    public Map<Identifier, Integer> enchantments() { return enchantments; }
    public int stars() { return stars; }
    public int upgradeLevel() { return upgradeLevel; }
    public NbtCompound customData() { return customData.copy(); }

    public boolean hasInstanceState() {
        return !reforgeId.isEmpty() || !enchantments.isEmpty() || stars > 0
                || upgradeLevel > 0 || !customData.isEmpty();
    }

    /** Validates definition-specific constraints after the data pack registry is available. */
    public void validateAgainst(ItemDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        if (!definitionId.equals(parseDefinitionId(definition.id()))) {
            throw new IllegalArgumentException("Instance definition does not match supplied ItemDefinition");
        }
        if (count > definition.maxStackSize()) {
            throw new IllegalArgumentException("Instance count exceeds item max stack size");
        }
        if (hasInstanceState() && count != 1) {
            throw new IllegalArgumentException("Stateful item instances cannot stack");
        }
        if (!reforgeId.isEmpty() && !definition.reforgeable()) {
            throw new IllegalArgumentException("Item definition does not allow reforging");
        }
    }

    public boolean canStackWith(ItemInstance other, ItemDefinition definition) {
        if (other == null || definition == null || instanceId.equals(other.instanceId)) return false;
        if (hasInstanceState() || other.hasInstanceState()) return false;
        if (!definitionId.equals(other.definitionId)
                || !definitionId.equals(parseDefinitionId(definition.id()))) return false;
        try {
            validateAgainst(definition);
            other.validateAgainst(definition);
        } catch (IllegalArgumentException invalidInstance) {
            return false;
        }
        return (long) count + other.count <= definition.maxStackSize();
    }

    /**
     * Merges another compatible stack into this stack. The target identity is retained.
     * The source instance is not mutated; callers must remove it from their container.
     */
    public Optional<ItemInstance> mergeFrom(ItemInstance other, ItemDefinition definition) {
        if (!canStackWith(other, definition)) return Optional.empty();
        return Optional.of(copyWithCount(count + other.count));
    }

    /**
     * Splits amount items into a new stack. The original identity remains with the remainder;
     * the split stack receives a new UUID. Stateful stacks cannot be split.
     */
    public SplitResult split(int amount) {
        if (hasInstanceState()) throw new IllegalStateException("Stateful item instances cannot be split");
        if (amount < 1 || amount >= count) {
            throw new IllegalArgumentException("Split amount must be between 1 and count - 1");
        }
        return new SplitResult(copyWithCount(count - amount),
                new ItemInstance(UUID.randomUUID(), definitionId, amount, "", Map.of(), 0, 0, new NbtCompound()));
    }

    public ItemInstance withReforge(String value, ItemDefinition definition) {
        return updated(value, enchantments, stars, upgradeLevel, customData, definition);
    }

    public ItemInstance withEnchantment(Identifier enchantmentId, int level, ItemDefinition definition) {
        Map<Identifier, Integer> next = new LinkedHashMap<>(enchantments);
        if (enchantmentId == null || level < 1 || level > MAX_ENCHANTMENT_LEVEL) {
            throw new IllegalArgumentException("Invalid enchantment ID or level");
        }
        next.put(enchantmentId, level);
        return updated(reforgeId, next, stars, upgradeLevel, customData, definition);
    }

    public ItemInstance withoutEnchantment(Identifier enchantmentId, ItemDefinition definition) {
        Map<Identifier, Integer> next = new LinkedHashMap<>(enchantments);
        next.remove(enchantmentId);
        return updated(reforgeId, next, stars, upgradeLevel, customData, definition);
    }

    public ItemInstance withStars(int value, ItemDefinition definition) {
        return updated(reforgeId, enchantments, value, upgradeLevel, customData, definition);
    }

    public ItemInstance withUpgradeLevel(int value, ItemDefinition definition) {
        return updated(reforgeId, enchantments, stars, value, customData, definition);
    }

    public ItemInstance withCustomData(NbtCompound value, ItemDefinition definition) {
        return updated(reforgeId, enchantments, stars, upgradeLevel, value, definition);
    }

    private ItemInstance updated(String reforge, Map<Identifier, Integer> enchants,
                                 int nextStars, int nextUpgrade, NbtCompound data,
                                 ItemDefinition definition) {
        ItemInstance updated = new ItemInstance(instanceId, definitionId, count, reforge,
                enchants, nextStars, nextUpgrade, data);
        updated.validateAgainst(definition);
        return updated;
    }

    private ItemInstance copyWithCount(int value) {
        return new ItemInstance(instanceId, definitionId, value, reforgeId,
                enchantments, stars, upgradeLevel, customData);
    }

    private static Identifier parseDefinitionId(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Item definition ID is required");
        Identifier parsed = value.contains(":")
                ? Identifier.tryParse(value)
                : Identifier.tryParse("skyblockrpg:" + value);
        if (parsed == null) throw new IllegalArgumentException("Invalid item definition ID: " + value);
        return parsed;
    }

    private static void validateCustomData(NbtElement element, int depth, int[] nodes) {
        if (depth > MAX_CUSTOM_DATA_DEPTH || ++nodes[0] > MAX_CUSTOM_DATA_NODES) {
            throw new IllegalArgumentException("Custom item data exceeds structural limits");
        }
        if (element instanceof NbtCompound compound) {
            if (compound.getKeys().size() > MAX_CUSTOM_DATA_NODES) {
                throw new IllegalArgumentException("Custom item data contains too many keys");
            }
            for (String key : compound.getKeys()) {
                if (key.length() > MAX_CUSTOM_DATA_KEY_LENGTH) {
                    throw new IllegalArgumentException("Custom item data key is too long");
                }
                NbtElement child = compound.get(key);
                if (child != null) validateCustomData(child, depth + 1, nodes);
            }
        } else if (element instanceof NbtList list) {
            for (int i = 0; i < list.size(); i++) {
                validateCustomData(list.get(i), depth + 1, nodes);
            }
        } else if (element instanceof NbtString string
                && string.asString().length() > MAX_CUSTOM_DATA_STRING_LENGTH) {
            throw new IllegalArgumentException("Custom item data string is too long");
        }
    }

    public record SplitResult(ItemInstance remainder, ItemInstance split) {
        public SplitResult {
            Objects.requireNonNull(remainder, "remainder");
            Objects.requireNonNull(split, "split");
            if (remainder.instanceId().equals(split.instanceId())) {
                throw new IllegalArgumentException("Split stacks must have distinct identities");
            }
        }
    }
}
