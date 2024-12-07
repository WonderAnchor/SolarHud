package net.wonderanchor.solarhud;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = "solarhud", value = Dist.CLIENT)
public class DataHandler {
    //The URL of the rest API for the Solar Server
    private static final String API_URL = "https://photon.sunblockone.milieux.ca/";
    private static final Logger LOGGER = LogManager.getLogger();

    private static String timestamp;
    private static float pvVoltage;
    private static float pvCurrent;
    private static float pvPower;
    private static float battVoltage;
    private static float battChargeCurrent;
    private static float battChargePower;
    private static float loadPower;
    private static float battPercentage;
    private static float battOverallCurrent;
    private static float cpuPowerDraw;
    private static String powerProfile;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().level != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime % 10000 < 50) { // Update every 10 seconds
                fetchAndStoreApiData();
            }
        }
    }
    //Gets all the variables as floats except timestamp and power profile.
    public static void fetchAndStoreApiData() {
        String responseData = fetchApiData();
        if (responseData != null) {
            try {
                JsonObject dataObject = JsonParser.parseString(responseData).getAsJsonObject();
                timestamp = dataObject.get("Timestamp").getAsString();
                pvVoltage = dataObject.get("PVVoltage").getAsFloat();
                pvCurrent = dataObject.get("PVCurrent").getAsFloat();
                pvPower = dataObject.get("PVPower").getAsFloat();
                battVoltage = dataObject.get("BattVoltage").getAsFloat();
                battChargeCurrent = dataObject.get("BattChargeCurrent").getAsFloat();
                battChargePower = dataObject.get("BattChargePower").getAsFloat();
                loadPower = dataObject.get("LoadPower").getAsFloat();
                battPercentage = dataObject.get("BattPercentage").getAsFloat();
                battOverallCurrent = dataObject.get("BattOverallCurrent").getAsFloat();
                cpuPowerDraw = dataObject.get("CPUPowerDraw").getAsFloat();
                powerProfile = dataObject.get("PowerProfile").getAsString();
            } catch (Exception e) {
                LOGGER.error("Failed to parse API data", e);
            }
        }
    }
    //Tries to connect to the URL, if connection fails log message is written.
    public static String fetchApiData() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(API_URL);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to fetch API data", e);
        }
        return null;
    }

    //Getters for the data
    public static String getTimestamp() {
        return timestamp;
    }

    public static float getPvVoltage() {
        return pvVoltage;
    }

    public static float getPvCurrent() {
        return pvCurrent;
    }

    public static float getPvPower() {
        return pvPower;
    }

    public static float getBattVoltage() {
        return battVoltage;
    }

    public static float getBattChargeCurrent() {
        return battChargeCurrent;
    }

    public static float getBattChargePower() {
        return battChargePower;
    }

    public static float getLoadPower() {
        return loadPower;
    }

    public static float getBattPercentage() {
        return battPercentage;
    }

    public static float getBattOverallCurrent() {
        return battOverallCurrent;
    }

    public static float getCpuPowerDraw() {
        return cpuPowerDraw;
    }

    public static String getPowerProfile() {
        return powerProfile;
    }
}
