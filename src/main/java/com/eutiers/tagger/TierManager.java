package com.eutiers.tagger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically downloads /player_rankings.json from the bot and caches it,
 * keyed by lowercase IGN. All network work happens on a daemon thread, never
 * on the render thread.
 */
public class TierManager {

    public static final TierManager INSTANCE = new TierManager();

    private final Gson gson = new Gson();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private volatile Map<String, PlayerTiers> byIgn = new HashMap<>();
    private ScheduledExecutorService scheduler;
    private volatile TierConfig config;

    private TierManager() {}

    public void start(TierConfig cfg) {
        this.config = cfg;
        if (scheduler != null) scheduler.shutdownNow();
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "eutiers-fetch");
            t.setDaemon(true);
            return t;
        });
        int interval = Math.max(20, cfg.refreshIntervalSeconds);
        scheduler.scheduleWithFixedDelay(this::refreshSafe, 0, interval, TimeUnit.SECONDS);
    }

    private void refreshSafe() {
        try {
            refresh();
        } catch (Exception e) {
            EuTiersClient.LOGGER.warn("Tier refresh failed: {}", e.toString());
        }
    }

    /** Blocking fetch — call from a background thread only. */
    public void refresh() throws Exception {
        TierConfig cfg = this.config;
        if (cfg == null) return;

        String base = cfg.baseUrl == null ? "" : cfg.baseUrl.trim();
        while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        if (base.isEmpty()) {
            EuTiersClient.LOGGER.warn("baseUrl is empty — set it in config/eutiers-tagger.json");
            return;
        }

        String url = base + "/player_rankings.json";
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "EuTiersTagger/1.0")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            EuTiersClient.LOGGER.warn("Tier endpoint {} returned HTTP {}", url, resp.statusCode());
            return;
        }

        List<PlayerTiers> list = gson.fromJson(resp.body(),
                new TypeToken<List<PlayerTiers>>() {}.getType());

        Map<String, PlayerTiers> map = new HashMap<>();
        if (list != null) {
            for (PlayerTiers p : list) {
                if (p != null && p.ign != null) {
                    map.put(p.ign.toLowerCase(Locale.ROOT), p);
                }
            }
        }
        byIgn = map;
        EuTiersClient.LOGGER.info("Loaded {} player tiers from {}", map.size(), url);
    }

    public PlayerTiers get(String ign) {
        if (ign == null) return null;
        return byIgn.get(ign.toLowerCase(Locale.ROOT));
    }

    public int size() {
        return byIgn.size();
    }
}
