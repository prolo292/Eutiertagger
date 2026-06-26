package com.eutiers.tagger.client;

import com.eutiers.tagger.EuTiersClient;
import com.eutiers.tagger.TierConfig;
import com.eutiers.tagger.TierFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Panel opened with the Y key: choose what shows next to player names. */
public class EuTiersScreen extends Screen {

    private static final String[] MODES = {
            "HIGHEST", "SWORD", "AXE", "MACE", "UHC", "POT", "NETHPOT", "CRYSTAL", "SMP"
    };

    public EuTiersScreen() {
        super(Text.literal("EU Tiers Settings"));
    }

    @Override
    protected void init() {
        TierConfig cfg = EuTiersClient.CONFIG;

        int colW = 100, gap = 4, rowH = 20;
        int perRow = 3;
        int rows = (MODES.length + perRow - 1) / perRow;
        int totalW = perRow * colW + (perRow - 1) * gap;
        int startX = (this.width - totalW) / 2;
        int startY = 50;

        for (int i = 0; i < MODES.length; i++) {
            final String mode = MODES[i];
            int r = i / perRow, c = i % perRow;
            int x = startX + c * (colW + gap);
            int y = startY + r * (rowH + gap);

            boolean selected = cfg.displayMode.equalsIgnoreCase(mode);
            String name = mode.equals("HIGHEST") ? "Highest" : TierFormat.modeLabel(mode);
            String label = selected ? ("\u00A7a\u00BB " + name + " \u00AB") : name;

            this.addDrawableChild(ButtonWidget.builder(Text.literal(label), btn -> {
                cfg.displayMode = mode;
                cfg.save();
                this.clearAndInit();
            }).dimensions(x, y, colW, rowH).build());
        }

        int ty = startY + rows * (rowH + gap) + 12;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Nametags: " + onOff(cfg.showOnNametags)), btn -> {
                    cfg.showOnNametags = !cfg.showOnNametags;
                    cfg.save();
                    this.clearAndInit();
                }).dimensions(this.width / 2 - 154, ty, 100, rowH).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Tab list: " + onOff(cfg.showInTabList)), btn -> {
                    cfg.showInTabList = !cfg.showInTabList;
                    cfg.save();
                    this.clearAndInit();
                }).dimensions(this.width / 2 - 50, ty, 100, rowH).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Icons: " + onOff(cfg.useModeIcons)), btn -> {
                    cfg.useModeIcons = !cfg.useModeIcons;
                    cfg.save();
                    this.clearAndInit();
                }).dimensions(this.width / 2 + 54, ty, 100, rowH).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("EU logo: " + onOff(cfg.showEuIcon)), btn -> {
                    cfg.showEuIcon = !cfg.showEuIcon;
                    cfg.save();
                    this.clearAndInit();
                }).dimensions(this.width / 2 - 50, ty + rowH + gap, 100, rowH).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> this.close())
                .dimensions(this.width / 2 - 50, ty + (rowH + gap) * 2 + 6, 100, rowH).build());
    }

    private static String onOff(boolean b) {
        return b ? "\u00A7aON" : "\u00A7cOFF";
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        net.minecraft.client.font.TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        context.drawCenteredTextWithShadow(tr, this.title, this.width / 2, 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(tr,
                Text.literal("\u00A77Choose what shows next to player names"),
                this.width / 2, 34, 0xAAAAAA);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
