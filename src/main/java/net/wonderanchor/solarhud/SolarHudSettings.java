package net.wonderanchor.solarhud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SolarHudSettings extends Screen {

    private final double hudScale;
    private int hudX;
    private int hudY;
    private final double hudOpacity;

    private boolean dragging = false;
    private int dragOffsetX;
    private int dragOffsetY;

    private ScaleSlider scaleSlider;
    private OpacitySlider opacitySlider;

    public SolarHudSettings() {
        super(Component.literal("Solar HUD Settings"));

        double initialScale = SolarHudConfig.HUD_SCALE.get();
        int initialXPos = SolarHudConfig.HUD_X_POSITION.get();
        int initialYPos = SolarHudConfig.HUD_Y_POSITION.get();
        double initialOpacity = SolarHudConfig.HUD_OPACITY.get();

        this.hudScale = initialScale;
        this.hudX = initialXPos;
        this.hudY = initialYPos;
        this.hudOpacity = initialOpacity;
    }

    @Override
    protected void init() {
        super.init();

        this.scaleSlider = new ScaleSlider(this.width / 2 - 100, this.height / 2 - 50,
                200, 20, 0.3, 1.5, this.hudScale);
        this.addRenderableWidget(this.scaleSlider);

        this.opacitySlider = new OpacitySlider(this.width / 2 - 100, this.height / 2 - 20,
                200, 20, 0.0, 1.0, this.hudOpacity);
        this.addRenderableWidget(this.opacitySlider);

        Button saveButton = Button.builder(Component.literal("Save"), button -> {
            SolarHudConfig.HUD_SCALE.set(this.scaleSlider.getValue());
            SolarHudConfig.HUD_X_POSITION.set(this.hudX);
            SolarHudConfig.HUD_Y_POSITION.set(this.hudY);
            SolarHudConfig.HUD_OPACITY.set(this.opacitySlider.getValue());

            if (SolarHudMain.CLIENT_MOD_CONFIG != null) {
                SolarHudMain.CLIENT_MOD_CONFIG.save();
            }

            Minecraft.getInstance().setScreen(null);
        }).bounds(this.width / 2 - 105, this.height - 30, 100, 20).build();

        Button cancelButton = Button.builder(Component.literal("Cancel"), button -> Minecraft.getInstance().setScreen(null)).bounds(this.width / 2 + 5, this.height - 30, 100, 20).build();

        this.addRenderableWidget(saveButton);
        this.addRenderableWidget(cancelButton);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        if (isMouseOverHud(mouseX, mouseY)) {
            this.dragging = true;
            this.dragOffsetX = (int)mouseX - this.hudX;
            this.dragOffsetY = (int)mouseY - this.hudY;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        this.dragging = false;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.dragging) {
            this.hudX = (int)mouseX - this.dragOffsetX;
            this.hudY = (int)mouseY - this.dragOffsetY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        // Center the title.
        String title = "SolarHUD Settings";
        int titleWidth = this.font.width(title);
        int titleX = (this.width - titleWidth) / 2; // Center horizontally
        int titleY = 40; // fixed Y position
        guiGraphics.drawString(this.font, title, titleX, titleY, 0xFFFFFF, true);

        float op = (float)this.opacitySlider.getValue();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // Apply opacity to the HUD texture only
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, op);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.hudX, this.hudY, 0);
        guiGraphics.pose().scale((float)this.scaleSlider.getValue(), (float)this.scaleSlider.getValue(), 1.0F);

        RenderSystem.setShaderTexture(0, new net.minecraft.resources.ResourceLocation("solarhud", "textures/gui/mc_sb_hud_day.png"));
        guiGraphics.blit(new net.minecraft.resources.ResourceLocation("solarhud", "textures/gui/mc_sb_hud_day.png"),
                0, 0, 0, 0, 168, 108, 168, 108);

        // Reset shader color back to full white for "Drag to Reposition" text.
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Always fully visible (no alpha fade).
        int textX = (168 / 2) - (this.font.width("Drag to Reposition") / 2);
        int textY = (108 / 2) - (this.font.lineHeight / 2);
        guiGraphics.drawString(this.font, "Drag to Reposition", textX, textY, 0xFFFFFFFF, false);

        guiGraphics.pose().popPose();
    }

    private boolean isMouseOverHud(double mouseX, double mouseY) {
        int hudW = (int)(168 * this.scaleSlider.getValue());
        int hudH = (int)(108 * this.scaleSlider.getValue());

        return mouseX >= this.hudX && mouseX <= this.hudX + hudW && mouseY >= this.hudY && mouseY <= this.hudY + hudH;
    }

    private static class ScaleSlider extends AbstractSliderButton {
        private final double min;
        private final double max;
        public ScaleSlider(int x, int y, int width, int height, double min, double max, double initialValue) {
            super(x, y, width, height, Component.literal(""), (initialValue - min) / (max - min));
            this.min = min;
            this.max = max;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal("Scale: " + String.format("%.2f", getValue())));
        }

        @Override
        protected void applyValue() {
        }

        public double getValue() {
            return this.value * (this.max - this.min) + this.min;
        }
    }

    private static class OpacitySlider extends AbstractSliderButton {
        private final double min;
        private final double max;
        public OpacitySlider(int x, int y, int width, int height, double min, double max, double initialValue) {
            super(x, y, width, height, Component.literal(""), (initialValue - min) / (max - min));
            this.min = min;
            this.max = max;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal("Opacity: " + String.format("%.2f", getValue())));
        }

        @Override
        protected void applyValue() {
        }

        public double getValue() {
            return this.value * (this.max - this.min) + this.min;
        }
    }
}
