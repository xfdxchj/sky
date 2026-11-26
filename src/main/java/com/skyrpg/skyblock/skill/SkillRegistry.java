package com.skyrpg.skyblock.skill;

import net.minecraft.util.Identifier;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Runtime registry for built-in and extension-provided skills. */
public final class SkillRegistry {
    private static final Map<Identifier, SkillRegistration> SKILLS = new LinkedHashMap<>();
    private static boolean builtinsRegistered;
    private SkillRegistry() {}

    public static synchronized void registerBuiltins() {
        if (builtinsRegistered) return;
        for (SkillType type : SkillType.values()) {
            String lower = type.name().toLowerCase(Locale.ROOT);
            String name = Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
            register(SkillRegistration.linear(type.identifier(), name, 60, 100.0));
        }
        builtinsRegistered = true;
    }

    public static synchronized boolean register(SkillRegistration definition) {
        if (definition == null || SKILLS.containsKey(definition.id())) return false;
        SKILLS.put(definition.id(), definition);
        return true;
    }

    public static Optional<SkillRegistration> get(Identifier id) {
        return Optional.ofNullable(id == null ? null : SKILLS.get(id));
    }

    public static Optional<SkillRegistration> resolve(String rawId) {
        if (rawId == null || rawId.isBlank()) return Optional.empty();
        String normalized = rawId.trim().toLowerCase(Locale.ROOT);
        Identifier id = Identifier.tryParse(normalized.contains(":") ? normalized : "skyblockrpg:" + normalized);
        return get(id);
    }

    public static Collection<SkillRegistration> all() {
        return Collections.unmodifiableCollection(SKILLS.values());
    }
}
