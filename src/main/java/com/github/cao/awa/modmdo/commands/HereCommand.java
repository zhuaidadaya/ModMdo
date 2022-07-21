package com.github.cao.awa.modmdo.commands;

import com.github.cao.awa.modmdo.develop.text.*;
import com.github.cao.awa.modmdo.storage.*;
import com.github.cao.awa.modmdo.utils.dimension.*;
import com.github.cao.awa.modmdo.utils.entity.*;
import com.github.cao.awa.modmdo.utils.text.*;
import com.github.zhuaidadaya.rikaishinikui.handler.universal.entrust.*;
import net.minecraft.entity.effect.*;
import net.minecraft.server.*;
import net.minecraft.server.command.*;
import net.minecraft.server.network.*;
import net.minecraft.util.math.*;

import static net.minecraft.server.command.CommandManager.*;

public class HereCommand extends SimpleCommand {
    public HereCommand register() {
        SharedVariables.commandRegister.register(literal("here").executes(here -> {
            ServerCommandSource source = here.getSource();
            if (SharedVariables.enableHereCommand) {
                try {
                    ServerPlayerEntity whoUseHere = source.getPlayer();
                    PlayerManager p = source.getServer().getPlayerManager();
                    Vec3d xyz = new Vec3d(whoUseHere.getX(), whoUseHere.getY(), whoUseHere.getZ());
                    String dimension = DimensionUtil.getDimension(whoUseHere.getEntityWorld().getDimension());
                    EntrustExecution.parallelTryFor(p.getPlayerList(), player -> {
                        Translatable hereMessage = formatHereTip(dimension, xyz, whoUseHere);
                        sendMessage(player, hereMessage, false);
                    });
                    whoUseHere.addStatusEffect(new StatusEffectInstance(StatusEffect.byRawId(24), 400, 5), whoUseHere);
                    sendFeedback(source, TextUtil.translatable("command.here.feedback", EntityUtil.getName(whoUseHere)));
                    return 1;
                } catch (Exception e) {
                    sendError(source, TextUtil.translatable("command.here.failed.feedback"));

                    return - 1;
                }
            } else {
                sendError(source, TextUtil.translatable("here_command.false.rule.format"));
            }
            return 0;
        }));
        return this;
    }

    public Translatable formatHereTip(String dimension, Vec3d xyz, ServerPlayerEntity whoUseHere) {
        String useHerePlayerName = EntityUtil.getName(whoUseHere);

        return TextUtil.translatable("command.here", useHerePlayerName, "", DimensionUtil.getDimensionColor(dimension) + useHerePlayerName, DimensionUtil.getDimensionName(dimension), "§e" + xyz.toString());
    }
}
