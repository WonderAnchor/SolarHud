package net.wonderanchor.solarhud;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;

@Mod.EventBusSubscriber(modid = "solarhud", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeybindHandler {

    //Create a keybinding instance
    public static final KeyMapping SUNBLOCK_SOLAR_HUD_KEY = new KeyMapping(
            "key.solarhud.solar_hud", // Translation key
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G, //Default key is 'G'
            "key.categories.solarhud" //Category
    );

    //Register the keybinding
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(SUNBLOCK_SOLAR_HUD_KEY);
    }

    //What happens if the key is pressed (log message for testing)
    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        if (SUNBLOCK_SOLAR_HUD_KEY.consumeClick()) {
            System.out.println("Sunblock Solar HUD key pressed!");
        }
    }
}
