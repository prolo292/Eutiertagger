package com.eutiers.tagger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * User-editable configuration, stored at:
 *   .minecraft/config/eutiers-tagger.json
 */
public class TierConfig {

    /** Public base URL of your Discord bot's web server (no trailing slash). */
    public String baseUrl = "http://eutierlist.hatenna.com:25332";

    /** Show the tier tag on the floating name above players' heads. */
    public boolean showOnNametags = true;

    /** Show the tier tag in the Tab player list. */
    public boolean showInTabList = true;

    /**
     * Which tier to show next to the name:
     *   "HIGHEST"  -> the player's best tier across all modes
     *   or a specific mode: SWORD, AXE, UHC, NETHPOT, POT, MACE, CRYSTAL, SMP
     */
    public String displayMode = "HIGHEST";

    /** Append the mode to the tag. */
    public boolean showModeName = true;

    /** Use the bundled mode icons (true) instead of the text label (false). */
    public boolean useModeIcons = true;

    /** Show the EU Tierlist logo to the left of tracked players' names. */
    public boolean showEuIcon = true;

    /** How often (seconds) to re-download the tier data. Minimum 20. */
    public int refreshIntervalSeconds = 120;

    // ----------------------------------------------------------------

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("eutiers-tagger.json");
    }

    public static TierConfig load() {
        Path p = path();
        try {
            if (Files.exists(p)) {
                TierConfig cfg = GSON.fromJson(Files.readString(p), TierConfig.class);
                if (cfg != null) {
                    cfg.save(); // re-write to add any newly introduced fields
                    return cfg;
                }
            }
        } catch (Exception e) {
            EuTiersClient.LOGGER.warn("Could not read config, using defaults: {}", e.toString());
        }
        TierConfig cfg = new TierConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        try {
            Files.writeString(path(), GSON.toJson(this));
        } catch (IOException e) {
            EuTiersClient.LOGGER.warn("Could not save config: {}", e.toString());
        }
    }
}
