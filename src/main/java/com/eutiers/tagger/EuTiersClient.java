package com.eutiers.tagger;

import com.eutiers.tagger.client.EuTiersScreen;
import com.eutiers.tagger.command.EuTiersCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EuTiersClient implements ClientModInitializer {

    public static final String MOD_ID = "eutiers";
    public static final Logger LOGGER = LoggerFactory.getLogger("EuTiersTagger");

    /** Loaded once at startup; read by the mixins, command and screen. */
    public static TierConfig CONFIG;

    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("eutiers", "main"));

    private static KeyBinding openPanelKey;

    @Override
    public void onInitializeClient() {
        CONFIG = TierConfig.load();
        TierManager.INSTANCE.start(CONFIG);

        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> EuTiersCommand.register(dispatcher));

        openPanelKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.eutiers.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openPanelKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new EuTiersScreen());
                }
            }
        });

        LOGGER.info("EU Tiers Tagger ready. Tier endpoint base = {}", CONFIG.baseUrl);
    }
}
