package com.github.zhuaidadaya.modmdo.commands;

import com.github.zhuaidadaya.modmdo.simple.vec.*;
import com.github.zhuaidadaya.modmdo.utils.command.*;
import com.github.zhuaidadaya.modmdo.utils.dimension.*;
import net.minecraft.entity.effect.*;
import net.minecraft.server.*;
import net.minecraft.server.command.*;
import net.minecraft.server.network.*;
import net.minecraft.text.*;

import static com.github.zhuaidadaya.modmdo.storage.Variables.*;
import static com.github.zhuaidadaya.modmdo.utils.command.SimpleCommandOperation.*;
import static net.minecraft.server.command.CommandManager.*;

public class HereCommand extends SimpleCommand {
    public HereCommand register() {
        commandRegister.register(literal("here").executes(here -> {
            ServerCommandSource source = here.getSource();
            if (enableHereCommand) {
                try {
                    ServerPlayerEntity whoUseHere = source.getPlayer();
                    PlayerManager p = source.getServer().getPlayerManager();
                    XYZ xyz = new XYZ(whoUseHere.getX(), whoUseHere.getY(), whoUseHere.getZ());
                    String dimension = whoUseHere.getEntityWorld().getDimension().getEffects().getPath();
                    for (ServerPlayerEntity player : p.getPlayerList()) {
                        if (isUserHereReceive(player.getUuid())) {
                            TranslatableText hereMessage = formatHereTip(dimension, xyz, whoUseHere);
                            sendMessage(player, hereMessage, false, 1);
                        }
                    }
                    whoUseHere.addStatusEffect(new StatusEffectInstance(StatusEffect.byRawId(24), 400, 5), whoUseHere);
                    sendFeedback(source, formatHereFeedBack(whoUseHere), 1);
                    return 1;
                } catch (Exception e) {
                    sendError(source, formatHereFailedFeedBack(), 1);

                    return - 1;
                }
            } else {
                sendError(source, formatHereDisabled(), 1);
            }
            return 0;
        }));
        return this;
    }

    public TranslatableText formatHereDisabled() {
        return new TranslatableText("here_command.disable.rule.format");
    }

    public TranslatableText formatHereTip(String dimension, XYZ xyz, ServerPlayerEntity whoUseHere) {
        String useHerePlayerName = whoUseHere.getName().asString();

        return new TranslatableText("command.here", useHerePlayerName, "", DimensionUtil.getDimensionColor(dimension) + useHerePlayerName, DimensionUtil.getDimensionName(dimension), "§e" + xyz.getIntegerXYZ());
    }

    public TranslatableText formatHereFeedBack(ServerPlayerEntity player) {
        return new TranslatableText("command.here.feedback", player.getName().asString());
    }

    public TranslatableText formatHereFailedFeedBack() {
        return new TranslatableText("command.here.failed.feedback");
    }
}
