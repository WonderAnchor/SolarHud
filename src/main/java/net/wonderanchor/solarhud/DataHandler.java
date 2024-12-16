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
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mod.EventBusSubscriber(modid = "solarhud", value = Dist.CLIENT)
public class DataHandler {
    private static final String API_URL = "https://photon.sunblockone.milieux.ca/";
    private static final Logger LOGGER = LogManager.getLogger();

    private static final long UPDATE_INTERVAL = 10000; // 10 seconds
    private static long lastUpdateTime = 0;

    // Data variables with defaults
    private static String timestamp = "";
    private static float pvVoltage = 0.0f;
    private static float pvCurrent = 0.0f;
    private static float pvPower = 0.0f;
    private static float battVoltage = 0.0f;
    private static float battChargeCurrent = 0.0f;
    private static float battChargePower = 0.0f;
    private static float loadPower = 0.0f;
    private static float battPercentage = 0.0f;
    private static float battOverallCurrent = 0.0f;
    private static float cpuPowerDraw = 0.0f;
    private static String powerProfile = "";

    // Use a single-threaded executor for async network calls
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().level != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdateTime >= UPDATE_INTERVAL) {
                lastUpdateTime = currentTime;
                // Run the fetch on a separate thread
                EXECUTOR.submit(DataHandler::fetchAndStoreApiDataAsync);
            }
        }
    }

    private static void fetchAndStoreApiDataAsync() {
        String responseData = fetchApiData();
        if (responseData != null) {
            try {
                JsonObject dataObject = JsonParser.parseString(responseData).getAsJsonObject();

                String newTimestamp = safeGetString(dataObject, "Timestamp");
                float newPvVoltage = safeGetFloat(dataObject, "PVVoltage");
                float newPvCurrent = safeGetFloat(dataObject, "PVCurrent");
                float newPvPower = safeGetFloat(dataObject, "PVPower");
                float newBattVoltage = safeGetFloat(dataObject, "BattVoltage");
                float newBattChargeCurrent = safeGetFloat(dataObject, "BattChargeCurrent");
                float newBattChargePower = safeGetFloat(dataObject, "BattChargePower");
                float newLoadPower = safeGetFloat(dataObject, "LoadPower");
                float newBattPercentage = safeGetFloat(dataObject, "BattPercentage");
                float newBattOverallCurrent = safeGetFloat(dataObject, "BattOverallCurrent");
                float newCpuPowerDraw = safeGetFloat(dataObject, "CPUPowerDraw");
                String newPowerProfile = safeGetString(dataObject, "PowerProfile");

                // Update fields atomically to avoid partial updates
                synchronized (DataHandler.class) {
                    timestamp = newTimestamp;
                    pvVoltage = newPvVoltage;
                    pvCurrent = newPvCurrent;
                    pvPower = newPvPower;
                    battVoltage = newBattVoltage;
                    battChargeCurrent = newBattChargeCurrent;
                    battChargePower = newBattChargePower;
                    loadPower = newLoadPower;
                    battPercentage = newBattPercentage;
                    battOverallCurrent = newBattOverallCurrent;
                    cpuPowerDraw = newCpuPowerDraw;
                    powerProfile = newPowerProfile;
                }

                LOGGER.debug("Successfully updated data: Timestamp: {}, PV Power: {}, Battery: {}%",
                        timestamp, pvPower, battPercentage);

            } catch (Exception e) {
                LOGGER.error("Failed to parse API data", e);
            }
        } else {
            LOGGER.warn("No data fetched from the API this cycle. Keeping previous values.");
        }
    }

    public static String fetchApiData() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(2000)     // 2-second connection timeout
                .setSocketTimeout(2000)      // 2-second read timeout
                .build();

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
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

    private static String safeGetString(JsonObject jsonObject, String key) {
        try {
            if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull()) {
                return jsonObject.get(key).getAsString();
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing string for key '{}': {}", key, e.getMessage());
        }
        return "";
    }

    private static float safeGetFloat(JsonObject jsonObject, String key) {
        try {
            if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull()) {
                return jsonObject.get(key).getAsFloat();
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing float for key '{}': {}", key, e.getMessage());
        }
        return (float) 0.0;
    }

    // Getters are synchronized to ensure thread-safe reads.
    public static synchronized String getTimestamp() { return timestamp; }
    public static synchronized float getPvVoltage() { return pvVoltage; }
    public static synchronized float getPvCurrent() { return pvCurrent; }
    public static synchronized float getPvPower() { return pvPower; }
    public static synchronized float getBattVoltage() { return battVoltage; }
    public static synchronized float getBattChargeCurrent() { return battChargeCurrent; }
    public static synchronized float getBattChargePower() { return battChargePower; }
    public static synchronized float getLoadPower() { return loadPower; }
    public static synchronized float getBattPercentage() { return battPercentage; }
    public static synchronized float getBattOverallCurrent() { return battOverallCurrent; }
    public static synchronized float getCpuPowerDraw() { return cpuPowerDraw; }
    public static synchronized String getPowerProfile() { return powerProfile; }
}
