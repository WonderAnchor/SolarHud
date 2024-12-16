package net.wonderanchor.solarhud;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SolarHudMain.MODID)
public class SolarHudMain {
    public static final String MODID = "solarhud";
    public static final Logger LOGGER = LogUtils.getLogger();

    // We'll store the ModConfig reference once the config is loaded
    public static ModConfig CLIENT_MOD_CONFIG;

    public SolarHudMain() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the client config
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SolarHudConfig.CLIENT_CONFIG, "solarhud-client.toml");

        // Register event listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Nothing special needed here for the config now
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Add items/blocks if needed
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Server start logic if needed
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Client-side initialization

        }
    }
}
