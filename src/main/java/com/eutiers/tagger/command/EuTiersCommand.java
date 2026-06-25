package com.eutiers.tagger.command;

import com.eutiers.tagger.PlayerTiers;
import com.eutiers.tagger.TierFormat;
import com.eutiers.tagger.TierManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/** Client-side command: /eutiers &lt;username&gt; */
public final class EuTiersCommand {

    private EuTiersCommand() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("eutiers")
                .then(argument("username", StringArgumentType.word())
                        .executes(EuTiersCommand::run))
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(Text.literal("§eUsage: /eutiers <username>"));
                    return 1;
                }));
    }

    private static int run(CommandContext<FabricClientCommandSource> ctx) {
        String username = StringArgumentType.getString(ctx, "username");
        FabricClientCommandSource src = ctx.getSource();
        PlayerTiers p = TierManager.INSTANCE.get(username);

        if (p == null) {
            src.sendFeedback(Text.literal("§cNo tiers found for §f" + username
                    + "§c. (" + TierManager.INSTANCE.size() + " players loaded — refreshing…)"));
            // The cache might be stale; trigger a one-off refresh in the background.
            Thread t = new Thread(() -> {
                try {
                    TierManager.INSTANCE.refresh();
                } catch (Exception ignored) {
                }
            }, "eutiers-cmd-refresh");
            t.setDaemon(true);
            t.start();
            return 0;
        }

        src.sendFeedback(TierFormat.buildFull(p));
        return 1;
    }
}
