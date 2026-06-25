package com.eutiers.tagger;

import java.util.List;
import java.util.Map;

/**
 * One entry from the bot's /player_rankings.json endpoint.
 * Field names match the JSON keys produced by bot.py (save_player_rankings).
 */
public class PlayerTiers {
    public String ign;
    public String discord;
    public String discord_id;
    public Map<String, String> tiers;        // mode (e.g. "SWORD") -> rank code (e.g. "HT1")
    public List<String> retired_modes;       // modes where the player is retired
    public Map<String, String> peak_tiers;   // mode -> peak rank code
    public int total_pts;
}
