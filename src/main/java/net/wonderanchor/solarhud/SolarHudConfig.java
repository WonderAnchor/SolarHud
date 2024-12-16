package net.wonderanchor.solarhud;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SolarHudMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SolarHudConfig {
    public static final ForgeConfigSpec CLIENT_CONFIG;
    public static final ForgeConfigSpec.DoubleValue HUD_SCALE;
    public static final ForgeConfigSpec.IntValue HUD_X_POSITION;
    public static final ForgeConfigSpec.IntValue HUD_Y_POSITION;
    public static final ForgeConfigSpec.DoubleValue HUD_OPACITY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Solar HUD Settings").push("hud");
        HUD_SCALE = builder.comment("HUD scale factor")
                .defineInRange("scale", 0.85, 0.3, 1.5);
        HUD_X_POSITION = builder.comment("HUD X position")
                .defineInRange("xPos", 2, 0, Integer.MAX_VALUE);
        HUD_Y_POSITION = builder.comment("HUD Y position")
                .defineInRange("yPos", 2, 0, Integer.MAX_VALUE);
        HUD_OPACITY = builder.comment("HUD opacity")
                .defineInRange("opacity", 1.0, 0.0, 1.0);
        builder.pop();

        CLIENT_CONFIG = builder.build();
    }

    @SubscribeEvent
    public static void onLoadConfig(final ModConfigEvent.Loading event) {
        ModConfig config = event.getConfig();
        if (config.getModId().equals(SolarHudMain.MODID) && config.getType() == ModConfig.Type.CLIENT) {
            SolarHudMain.CLIENT_MOD_CONFIG = config;
        }
    }

    @SubscribeEvent
    public static void onReloadConfig(final ModConfigEvent.Reloading event) {
        ModConfig config = event.getConfig();
        if (config.getModId().equals(SolarHudMain.MODID) && config.getType() == ModConfig.Type.CLIENT) {
            SolarHudMain.CLIENT_MOD_CONFIG = config;
        }
    }
}
