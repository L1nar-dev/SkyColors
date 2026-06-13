package dev.skycolors;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.*;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public class SkyColorsPlugin extends JavaPlugin {

    public static final Map<String, int[]> BIOMES = new LinkedHashMap<>();

    static {
        BIOMES.put("purple_sky", new int[]{0x8B008B, 0x8B008B});
        BIOMES.put("red_sky",    new int[]{0xFF0000, 0xFF0000});
        BIOMES.put("green_sky",  new int[]{0x00AA00, 0x00AA00});
        BIOMES.put("cyan_sky",   new int[]{0x00FFFF, 0x00FFFF});
        BIOMES.put("white_sky",  new int[]{0xFFFFFF, 0xFFFFFF});
    }

    @Override
    public void onLoad() {
        try {
            registerBiomes();
            getLogger().info("Registered " + BIOMES.size() + " custom sky biomes!");
        } catch (Exception e) {
            getLogger().severe("Failed to register biomes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void registerBiomes() throws Exception {
        var nmsServer = ((CraftServer) getServer()).getServer();
        RegistryAccess registryAccess = (RegistryAccess) nmsServer.registryAccess();
        var biomeRegistry = (MappedRegistry<Biome>) registryAccess.registryOrThrow(Registries.BIOME);

        unfreeze(biomeRegistry);

        var plainsKey = ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("plains"));
        var plains = biomeRegistry.get(plainsKey).orElseThrow().value();
        if (plains == null) throw new IllegalStateException("Plains biome not found!");

        int waterColor = plains.getSpecialEffects().getWaterColor();
        int waterFogColor = plains.getSpecialEffects().getWaterFogColor();

        for (var entry : BIOMES.entrySet()) {
            String name = entry.getKey();
            int skyColor = entry.getValue()[0];
            int fogColor = entry.getValue()[1];

            var effects = new BiomeSpecialEffects.Builder()
                    .skyColor(skyColor)
                    .fogColor(fogColor)
                    .waterColor(waterColor)
                    .waterFogColor(waterFogColor)
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build();

            var biome = new Biome.BiomeBuilder()
                    .hasPrecipitation(true)
                    .temperature(0.8f)
                    .downfall(0.4f)
                    .specialEffects(effects)
                    .mobSpawnSettings(MobSpawnSettings.EMPTY)
                    .generationSettings(BiomeGenerationSettings.EMPTY)
                    .build();

            var key = ResourceKey.create(
                    Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath("sky_colors", name)
            );

            Registry.register(biomeRegistry, key, biome);
            getLogger().info("Registered biome: sky_colors:" + name);
        }

        refreeze(biomeRegistry);
    }

    private void unfreeze(Registry<?> registry) throws Exception {
        if (registry instanceof MappedRegistry<?> mapped) {
            Field frozenField = MappedRegistry.class.getDeclaredField("frozen");
            frozenField.setAccessible(true);
            frozenField.set(mapped, false);
        }
    }

    private void refreeze(Registry<?> registry) throws Exception {
        if (registry instanceof MappedRegistry<?> mapped) {
            Field frozenField = MappedRegistry.class.getDeclaredField("frozen");
            frozenField.setAccessible(true);
            frozenField.set(mapped, true);
        }
    }
}
