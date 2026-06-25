package com.eutiers.tagger;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Formatting helpers: tier ordering, mode labels/icons, colors and Text builders. */
public final class TierFormat {

    private TierFormat() {}

    /** Custom bitmap font bundled in the mod (assets/eutiers/font/icons.json). */
    public static final Identifier ICON_FONT = Identifier.of("eutiers", "icons");

    /** EU Tierlist logo glyph (shown to the left of the name). */
    public static final String EU_ICON = "\uE000";

    /** Points used to decide which tier is "highest" (mirrors RANK_POINTS in bot.py). */
    public static final Map<String, Integer> RANK_POINTS = new LinkedHashMap<>();
    static {
        RANK_POINTS.put("HT1", 60); RANK_POINTS.put("LT1", 45);
        RANK_POINTS.put("HT2", 30); RANK_POINTS.put("LT2", 20);
        RANK_POINTS.put("HT3", 15); RANK_POINTS.put("LT3", 10);
        RANK_POINTS.put("HT4", 5);  RANK_POINTS.put("LT4", 3);
        RANK_POINTS.put("HT5", 2);  RANK_POINTS.put("LT5", 1);
    }

    /** Display names per mode (used when icons are disabled). */
    public static final Map<String, String> MODE_LABELS = new LinkedHashMap<>();
    static {
        MODE_LABELS.put("SWORD", "Sword");
        MODE_LABELS.put("AXE", "Axe");
        MODE_LABELS.put("UHC", "UHC");
        MODE_LABELS.put("NETHPOT", "NethPot");
        MODE_LABELS.put("POT", "Pot");
        MODE_LABELS.put("MACE", "Mace");
        MODE_LABELS.put("CRYSTAL", "Crystal");
        MODE_LABELS.put("CPVP", "Crystal");
        MODE_LABELS.put("SMP", "SMP");
    }

    /** Private-use codepoints matching assets/eutiers/font/icons.json. */
    public static final Map<String, String> MODE_ICON = new LinkedHashMap<>();
    static {
        MODE_ICON.put("SWORD", "\uE001");
        MODE_ICON.put("AXE", "\uE002");
        MODE_ICON.put("MACE", "\uE003");
        MODE_ICON.put("UHC", "\uE004");
        MODE_ICON.put("POT", "\uE005");
        MODE_ICON.put("NETHPOT", "\uE006");
        MODE_ICON.put("SMP", "\uE007");
        MODE_ICON.put("CRYSTAL", "\uE008");
        MODE_ICON.put("CPVP", "\uE008");
    }

    public static String modeLabel(String mode) {
        if (mode == null) return "";
        return MODE_LABELS.getOrDefault(mode.toUpperCase(Locale.ROOT), capitalize(mode));
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    public static int points(String code) {
        if (code == null) return -1;
        return RANK_POINTS.getOrDefault(code.toUpperCase(Locale.ROOT), -1);
    }

    /** Color for a rank code. Tier 1 = warm/bright, tier 5 = cool/gray. */
    public static int tierColor(String code) {
        if (code == null) return 0xAAAAAA;
        switch (code.toUpperCase(Locale.ROOT)) {
            case "HT1": return 0xFF4D4D;
            case "LT1": return 0xFFA033;
            case "HT2": return 0xFFD93B;
            case "LT2": return 0xE6E64D;
            case "HT3": return 0x66E066;
            case "LT3": return 0x4DD0C4;
            case "HT4": return 0x5AA9E6;
            case "LT4": return 0x8C8CF0;
            case "HT5": return 0xC0A6E0;
            case "LT5": return 0xB0B0B0;
            default:    return 0xAAAAAA;
        }
    }

    /** Returns {mode, code} of the highest-ranked tier the player has, or null. */
    public static String[] highest(PlayerTiers p) {
        if (p == null || p.tiers == null || p.tiers.isEmpty()) return null;
        String bestMode = null, bestCode = null;
        int best = -1;
        for (Map.Entry<String, String> e : p.tiers.entrySet()) {
            int pts = points(e.getValue());
            if (pts > best) {
                best = pts;
                bestMode = e.getKey();
                bestCode = e.getValue();
            }
        }
        return bestMode == null ? null : new String[]{bestMode, bestCode};
    }

    /** Builds a colored mode icon (or text label fallback) for a given mode. */
    private static MutableText modePart(String mode, TierConfig cfg) {
        String up = mode.toUpperCase(Locale.ROOT);
        if (cfg.useModeIcons && MODE_ICON.containsKey(up)) {
            // White color = show the icon's own colors (font glyphs are tinted by text color).
            return Text.literal(MODE_ICON.get(up))
                    .setStyle(Style.EMPTY.withFont(ICON_FONT).withColor(TextColor.fromRgb(0xFFFFFF)));
        }
        if (cfg.showModeName) {
            return Text.literal(modeLabel(up)).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xC0C0C0)));
        }
        return null;
    }

    /** EU logo shown to the left of the name (for tracked players). Null if disabled. */
    public static MutableText buildPrefix(PlayerTiers p, TierConfig cfg) {
        if (p == null || !cfg.showEuIcon) return null;
        MutableText pre = Text.literal(EU_ICON)
                .setStyle(Style.EMPTY.withFont(ICON_FONT).withColor(TextColor.fromRgb(0xFFFFFF)));
        pre.append(Text.literal(" "));
        return pre;
    }

    /** Small colored tag appended to a name, e.g. " [HT4 <icon>]". Null if nothing to show. */
    public static MutableText buildTag(PlayerTiers p, TierConfig cfg) {
        if (p == null) return null;

        String mode, code;
        if ("HIGHEST".equalsIgnoreCase(cfg.displayMode)) {
            String[] hi = highest(p);
            if (hi == null) return null;
            mode = hi[0];
            code = hi[1];
        } else {
            mode = cfg.displayMode.toUpperCase(Locale.ROOT);
            code = (p.tiers == null) ? null : p.tiers.get(mode);
            if (code == null) return null;
        }

        Style gray = Style.EMPTY.withColor(TextColor.fromRgb(0x808080));
        MutableText tag = Text.literal(" [").setStyle(gray);
        tag.append(Text.literal(code).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(tierColor(code)))));

        MutableText modePart = modePart(mode, cfg);
        if (modePart != null) {
            tag.append(Text.literal(" "));
            tag.append(modePart);
        }
        tag.append(Text.literal("]").setStyle(gray));
        return tag;
    }

    /** Full multi-line breakdown for the /eutiers command. */
    public static MutableText buildFull(PlayerTiers p) {
        MutableText out = Text.literal("");
        out.append(Text.literal("\n§7──────── §bEU Tiers §7────────\n"));
        out.append(Text.literal("§7Player: §f" + p.ign + "\n"));
        if (p.discord != null && !p.discord.isEmpty()) {
            out.append(Text.literal("§7Discord: §f" + p.discord + "\n"));
        }

        if (p.tiers == null || p.tiers.isEmpty()) {
            out.append(Text.literal("§cNo tiers recorded.\n"));
        } else {
            p.tiers.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(points(b.getValue()), points(a.getValue())))
                    .forEach(e -> {
                        String mode = e.getKey();
                        String code = e.getValue();
                        boolean retired = p.retired_modes != null && p.retired_modes.contains(mode);
                        String peak = (p.peak_tiers != null) ? p.peak_tiers.get(mode) : null;

                        MutableText line = Text.literal("§7• §f" + modeLabel(mode) + ": ");
                        line.append(Text.literal(code)
                                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(tierColor(code)))));
                        if (peak != null && !peak.equalsIgnoreCase(code)) {
                            line.append(Text.literal(" §8(peak " + peak + ")"));
                        }
                        if (retired) {
                            line.append(Text.literal(" §8[retired]"));
                        }
                        line.append(Text.literal("\n"));
                        out.append(line);
                    });
        }

        out.append(Text.literal("§7Total points: §f" + p.total_pts + "\n"));
        out.append(Text.literal("§7────────────────────────"));
        return out;
    }
}
