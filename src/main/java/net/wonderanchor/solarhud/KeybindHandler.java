package net.wonderanchor.solarhud;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "solarhud", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeybindHandler {

    public static final KeyMapping SUNBLOCK_SOLAR_HUD_KEY = new KeyMapping(
            "key.solarhud.solar_hud",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.solarhud"
    );

    public static final KeyMapping SOLAR_HUD_SETTINGS_KEY = new KeyMapping(
            "key.solarhud.solar_hud_settings",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.solarhud"
    );

    public KeybindHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(SUNBLOCK_SOLAR_HUD_KEY);
        event.register(SOLAR_HUD_SETTINGS_KEY);
    }

    @Mod.EventBusSubscriber(modid = "solarhud", value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent event) {
            if (event.phase == ClientTickEvent.Phase.END) {
                Minecraft mc = Minecraft.getInstance();

                // Check if the Solar HUD settings key is pressed.
                if (SOLAR_HUD_SETTINGS_KEY.consumeClick()) {
                    // If no other screen is open, open the settings screen.
                    if (mc.screen == null) {
                        mc.setScreen(new SolarHudSettings());
                    }
                }
            }
        }
    }
}
