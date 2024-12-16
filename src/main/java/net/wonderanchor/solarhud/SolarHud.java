package net.wonderanchor.solarhud;

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

@Mod.EventBusSubscriber(modid = SolarHudMain.MODID, value = Dist.CLIENT)
public class SolarHud {

    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean isHudVisible = false;
    private static boolean hasLoggedRender = false;

    @SubscribeEvent
    public static void onKeyInput(TickEvent.ClientTickEvent event) {
        if (minecraft.screen != null) {
            return;
        }
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
            hasLoggedRender = true;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();

        double configScale = SolarHudConfig.HUD_SCALE.get();
        double configOpacity = SolarHudConfig.HUD_OPACITY.get();
        float op = (float) configOpacity;

        // Calculate text alpha with 0.32 threshold
        int originalAlpha = (int)(configOpacity * 255) & 0xFF;
        int textAlpha;
        if (originalAlpha < 82) {
            textAlpha = 0;
        } else {
            textAlpha = (int)(((originalAlpha - 82) / 173.0) * 255.0); // 173 = 255 - 82
        }
        int textColor = (textAlpha << 24) | 0xFFFFFF;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, op);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float)SolarHudConfig.HUD_X_POSITION.get(), (float)SolarHudConfig.HUD_Y_POSITION.get(), 0);
        guiGraphics.pose().scale((float)configScale, (float)configScale, 1.0F);

        String texturePath = getTexturePath(DataHandler.getTimestamp());
        RenderSystem.setShaderTexture(0, new ResourceLocation(SolarHudMain.MODID, texturePath));
        guiGraphics.blit(new ResourceLocation(SolarHudMain.MODID, texturePath),
                0, 0, 0, 0, 168, 108, 168, 108);

        String timestamp = DataHandler.getTimestamp();
        String[] timeParts = (timestamp != null && !timestamp.isEmpty()) ? timestamp.split(" ")[1].split(":") : new String[]{"--","--"};
        String timeString = timeParts[0] + ":" + timeParts[1];

        // Draw text with modified alpha scaling.
        guiGraphics.drawString(minecraft.font, "Montreal, QC " + timeString, 60, 19, textColor);
        guiGraphics.drawString(minecraft.font, "CPU: " + DataHandler.getCpuPowerDraw() + "w", 25, 40, textColor);
        guiGraphics.drawString(minecraft.font, "CONSUMPTION: " + DataHandler.getLoadPower() + "w", 25, 58, textColor);
        guiGraphics.drawString(minecraft.font, "GENERATION: " + DataHandler.getPvVoltage() + "v | " + DataHandler.getPvPower() + "w", 25, 76, textColor);
        guiGraphics.drawString(minecraft.font, "BATTERY: " + DataHandler.getBattVoltage() + "v | " + DataHandler.getBattPercentage() + "%", 25, 94, textColor);

        // Icons.
        RenderSystem.setShaderTexture(0, new ResourceLocation(SolarHudMain.MODID, getCpuIcon()));
        guiGraphics.blit(new ResourceLocation(SolarHudMain.MODID, getCpuIcon()), 3, 36, 0, 0, 16, 16, 16, 16);

        RenderSystem.setShaderTexture(0, new ResourceLocation(SolarHudMain.MODID, getLoadIcon()));
        guiGraphics.blit(new ResourceLocation(SolarHudMain.MODID, getLoadIcon()), 3, 54, 0, 0, 16, 16, 16, 16);

        RenderSystem.setShaderTexture(0, new ResourceLocation(SolarHudMain.MODID, getGenerationIcon()));
        guiGraphics.blit(new ResourceLocation(SolarHudMain.MODID, getGenerationIcon()), 3, 72, 0, 0, 16, 16, 16, 16);

        RenderSystem.setShaderTexture(0, new ResourceLocation(SolarHudMain.MODID, getBatteryIcon()));
        guiGraphics.blit(new ResourceLocation(SolarHudMain.MODID, getBatteryIcon()), 3, 90, 0, 0, 16, 16, 16, 16);

        RenderSystem.setShaderTexture(0, new ResourceLocation(SolarHudMain.MODID, getBatteryArrowIcon(DataHandler.getBattOverallCurrent())));
        guiGraphics.blit(new ResourceLocation(SolarHudMain.MODID, getBatteryArrowIcon(DataHandler.getBattOverallCurrent())),
                12, 90, 0, 0, 16, 16, 16, 16);

        guiGraphics.pose().popPose();

        // Reset shader color.
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static String getTexturePath(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "textures/gui/mc_sb_hud_day.png";
        }

        try {
            int hour = Integer.parseInt(timestamp.split(" ")[1].split(":")[0]);
            if (hour >= 6 && hour < 9) {
                return "textures/gui/mc_sb_hud_dawn.png";
            } else if (hour >= 9 && hour < 18) {
                return "textures/gui/mc_sb_hud_day.png";
            } else if (hour >= 18 && hour < 21) {
                return "textures/gui/mc_sb_hud_dusk.png";
            } else {
                return "textures/gui/mc_sb_hud_night.png";
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing timestamp '{}': {}", timestamp, e.getMessage());
            return "textures/gui/mc_sb_hud_day.png";
        }
    }

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

    private static String getGenerationIcon() {
        float pvPower = DataHandler.getPvPower();
        if (pvPower < 10) {
            return "textures/gui/mc_sb_icons_iso_sun_r.png";
        } else if (pvPower < 22) {
            return "textures/gui/mc_sb_icons_iso_sun_o.png";
        } else {
            return "textures/gui/mc_sb_icons_iso_sun.png";
        }
    }

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

    private static String getBatteryArrowIcon(float battOverallCurrent) {
        if (battOverallCurrent > 0) {
            return "textures/gui/mc_sb_icons_iso_arrow_up.png";
        } else {
            return "textures/gui/mc_sb_icons_iso_arrow_down.png";
        }
    }
}
