package net.wonderanchor.sunblockcore;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = "sunblockcore", value = Dist.CLIENT)
public class SolarHud {

    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean isHudVisible = false;
    private static boolean hasLoggedRender = false;

    @SubscribeEvent
    public static void onKeyInput(TickEvent.ClientTickEvent event) {
        if (minecraft.options == null || minecraft.screen != null) {
            return;
        }
        //Detects key press and changes isHudVisible value. Also logs message for testing.
        if (KeybindHandler.SUNBLOCK_SOLAR_HUD_KEY.consumeClick()) {
            isHudVisible = !isHudVisible;
            LOGGER.info("HUD visibility toggled: {}", isHudVisible);
            if (isHudVisible) {
                hasLoggedRender = false;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (!isHudVisible) {
            return;
        }

        if (!hasLoggedRender) {
            LOGGER.info("Rendering Solar HUD");
            hasLoggedRender = true;//Boolean value here just makes sure message is printed once.
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        String texturePath = getTexturePath(DataHandler.getTimestamp());

        try {
            RenderSystem.setShaderTexture(0, new ResourceLocation("sunblockcore", texturePath.toLowerCase()));
            //Renders the background image.
            guiGraphics.blit(new ResourceLocation("sunblockcore", texturePath.toLowerCase()), 60, event.getWindow().getGuiScaledHeight() - 108, 0, 0, 168, 108, 168, 108);

            String[] timeParts = DataHandler.getTimestamp().split(" ")[1].split(":" );
            String timeString = timeParts[0] + ":" + timeParts[1];
            //Renders the Text components.
            guiGraphics.drawString(minecraft.font, "Montreal, QC " + timeString, 120, event.getWindow().getGuiScaledHeight() - 89, 0xFFFFFF);
            guiGraphics.drawString(minecraft.font, "CPU: " + DataHandler.getCpuPowerDraw() + "w", 85, event.getWindow().getGuiScaledHeight() - 68, 0xFFFFFF);
            guiGraphics.drawString(minecraft.font, "CONSUMPTION: " + DataHandler.getLoadPower() + "w", 85, event.getWindow().getGuiScaledHeight() - 50, 0xFFFFFF);
            guiGraphics.drawString(minecraft.font, "GENERATION: " + DataHandler.getPvVoltage() + "v | " + DataHandler.getPvPower() + "w", 85, event.getWindow().getGuiScaledHeight() - 32, 0xFFFFFF);
            guiGraphics.drawString(minecraft.font, "BATTERY: " + DataHandler.getBattVoltage() + "v | " + DataHandler.getBattPercentage() + "%", 85, event.getWindow().getGuiScaledHeight() - 14, 0xFFFFFF);

            //Renders the icons.
            drawIcon(guiGraphics, getCpuIcon(), 63, event.getWindow().getGuiScaledHeight() - 72);
            drawIcon(guiGraphics, getLoadIcon(), 63, event.getWindow().getGuiScaledHeight() - 54);
            drawIcon(guiGraphics, getGenerationIcon(), 63, event.getWindow().getGuiScaledHeight() - 36);
            drawIcon(guiGraphics, getBatteryIcon(), 63, event.getWindow().getGuiScaledHeight() - 18);

            //Renders arrow icon for battery charge/discharge
            String arrowIcon = getBatteryArrowIcon(DataHandler.getBattOverallCurrent());
            drawIcon(guiGraphics, arrowIcon, 72, event.getWindow().getGuiScaledHeight() - 18);
        } catch (Exception e) {
            LOGGER.error("Error rendering Solar HUD: {}", e.getMessage(), e);
        }
    }

    //Setup drawIcon (icon sizes).
    private static void drawIcon(GuiGraphics guiGraphics, String iconPath, int x, int y) {
        RenderSystem.setShaderTexture(0, new ResourceLocation("sunblockcore", iconPath.toLowerCase()));
        guiGraphics.blit(new ResourceLocation("sunblockcore", iconPath.toLowerCase()), x, y, 0, 0, 16, 16, 16, 16);
    }

    //Selects the background depending on real world hour.
    private static String getTexturePath(String timestamp) {
        int hour = Integer.parseInt(timestamp.split(" ")[1].split(":" )[0]);
        if (hour >= 6 && hour < 9) {
            return "textures/gui/mc_sb_hud_dawn.png";
        } else if (hour >= 9 && hour < 18) {
            return "textures/gui/mc_sb_hud_day.png";
        } else if (hour >= 18 && hour < 21) {
            return "textures/gui/mc_sb_hud_dusk.png";
        } else {
            return "textures/gui/mc_sb_hud_night.png";
        }
    }

    //Selects CPU icon colour depending on power draw.
    private static String getCpuIcon() {
        float cpuPowerDraw = DataHandler.getCpuPowerDraw();
        if (cpuPowerDraw < 5) {
            return "textures/gui/mc_sb_icons_iso_cpu_g.png";
        } else if (cpuPowerDraw < 20) {
            return "textures/gui/mc_sb_icons_iso_cpu_y.png";
        } else {
            return "textures/gui/mc_sb_icons_iso_cpu_r.png";
        }
    }

    //Selects Globe icon depending on consumption (watts).
    private static String getLoadIcon() {
        float loadPower = DataHandler.getLoadPower();
        if (loadPower < 10) {
            return "textures/gui/mc_sb_icons_iso_globe_g.png";
        } else if (loadPower < 22) {
            return "textures/gui/mc_sb_icons_iso_globe_y.png";
        } else {
            return "textures/gui/mc_sb_icons_iso_globe_r.png";
        }
    }

    //Selects Sun icon depending on Power generation.
    private static String getGenerationIcon() {
        float pvPower = DataHandler.getPvPower();
        if (pvPower < 20) {
            return "textures/gui/mc_sb_icons_iso_sun.png";
        } else if (pvPower < 50) {
            return "textures/gui/mc_sb_icons_iso_sun_o.png";
        } else {
            return "textures/gui/mc_sb_icons_iso_sun_r.png";
        }
    }

    //Selects battery icon depending on charge.
    private static String getBatteryIcon() {
        float battPercentage = DataHandler.getBattPercentage();
        if (battPercentage > 75) {
            return "textures/gui/mc_sb_icons_iso_battery_g.png";
        } else if (battPercentage > 50) {
            return "textures/gui/mc_sb_icons_iso_battery_y.png";
        } else if (battPercentage > 25) {
            return "textures/gui/mc_sb_icons_iso_battery_o.png";
        } else {
            return "textures/gui/mc_sb_icons_iso_battery_r.png";
        }
    }

    //Selects arrow icon (up or down) depending on positive or negative current.
    private static String getBatteryArrowIcon(float battOverallCurrent) {
        if (battOverallCurrent > 0) {
            return "textures/gui/mc_sb_icons_iso_arrow_up.png";
        } else {
            return "textures/gui/mc_sb_icons_iso_arrow_down.png";
        }
    }
}
