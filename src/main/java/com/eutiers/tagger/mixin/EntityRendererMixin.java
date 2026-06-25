package com.eutiers.tagger.mixin;

import com.eutiers.tagger.EuTiersClient;
import com.eutiers.tagger.PlayerTiers;
import com.eutiers.tagger.TierFormat;
import com.eutiers.tagger.TierManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * In 1.21.11 the nametag label text is produced by EntityRenderer#getDisplayName
 * and stored in the entity render state. We add the EU logo (left) and tier tag
 * (right) here so they flow through to the floating nameplate.
 */
@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void eutiers$appendTier(Entity entity, CallbackInfoReturnable<Text> cir) {
        if (EuTiersClient.CONFIG == null || !EuTiersClient.CONFIG.showOnNametags) return;
        if (!(entity instanceof PlayerEntity player)) return;

        Text original = cir.getReturnValue();
        if (original == null) return;

        String name = player.getGameProfile().name();
        if (name == null) return;

        PlayerTiers p = TierManager.INSTANCE.get(name);
        if (p == null) return;

        MutableText prefix = TierFormat.buildPrefix(p, EuTiersClient.CONFIG);
        MutableText tag = TierFormat.buildTag(p, EuTiersClient.CONFIG);
        if (prefix == null && tag == null) return;

        MutableText result = Text.empty();
        if (prefix != null) result.append(prefix);
        result.append(original);
        if (tag != null) result.append(tag);
        cir.setReturnValue(result);
    }
}
