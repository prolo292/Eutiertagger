package com.eutiers.tagger.mixin;

import com.eutiers.tagger.EuTiersClient;
import com.eutiers.tagger.PlayerTiers;
import com.eutiers.tagger.TierFormat;
import com.eutiers.tagger.TierManager;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void eutiers$appendTier(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        if (EuTiersClient.CONFIG == null || !EuTiersClient.CONFIG.showInTabList) return;
        if (entry == null || entry.getProfile() == null) return;

        String name = entry.getProfile().name();
        if (name == null) return;

        PlayerTiers p = TierManager.INSTANCE.get(name);
        if (p == null) return;

        MutableText prefix = TierFormat.buildPrefix(p, EuTiersClient.CONFIG);
        MutableText tag = TierFormat.buildTag(p, EuTiersClient.CONFIG);
        if (prefix == null && tag == null) return;

        MutableText result = Text.empty();
        if (prefix != null) result.append(prefix);
        result.append(cir.getReturnValue());
        if (tag != null) result.append(tag);
        cir.setReturnValue(result);
    }
}
