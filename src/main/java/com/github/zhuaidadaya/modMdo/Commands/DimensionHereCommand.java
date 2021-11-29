package com.github.zhuaidadaya.modMdo.Commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import static com.github.zhuaidadaya.modMdo.Storage.Variables.language;
import static com.github.zhuaidadaya.modMdo.Storage.Variables.languageDictionary;

public class DimensionHereCommand implements Here {
    public int dhere(CommandContext<ServerCommandSource> context) {
        try {
            DimensionTips dimensionTips = new DimensionTips();
            PlayerManager p = context.getSource().getServer().getPlayerManager();
            String whoUseHere = context.getSource().getPlayer().getName().getString();
            for(String o : p.getPlayerNames()) {
                ServerPlayerEntity player = p.getPlayer(o);
                XYZ xyz = new XYZ(player.getX(), player.getY(), player.getZ());
                String dimension = player.getEntityWorld().getDimension().getEffects().getPath();
                player.sendMessage(new LiteralText(formatHereTip(dimension, xyz, whoUseHere, dimensionTips)), false);
                player.addStatusEffect(new StatusEffectInstance(StatusEffect.byRawId(24), 400, 5), player);
            }
            context.getSource().sendFeedback(Text.of(formatHereFeedBack(whoUseHere)), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFeedback(Text.of(formatHereFailedFeedBack()), false);
            return - 1;
        }
    }

    @Override
    public String formatHereTip(String dimension, XYZ xyz, String playerName, DimensionTips dimensionTips) {
        String convertTarget = "";
        switch(dimension) {
            case "overworld" -> convertTarget = "the_nether";
            case "the_nether" -> convertTarget = "overworld";
            case "the_end" -> {
                throw new IllegalArgumentException();
            }
        }
        String format = languageDictionary.getWord(language, "command_dhere");
        String format_startWith = languageDictionary.getWord(language, "here_start");
        XYZ convertXYZ = xyz.clone();
        if(convertTarget.equals("the_nether")) {
            convertXYZ.divideXZ(8, 8);
        } else {
            convertXYZ.multiplyXZ(8, 8);
        }
        return String.format(format_startWith, playerName) + String.format(format, dimensionTips.getDimensionColor(dimension), playerName, dimensionTips.getDimensionName(dimension), xyz.getIntegerXYZ(), languageDictionary.getWord(language, convertTarget), convertXYZ.getIntegerXYZ());
    }

    @Override
    public String formatHereFeedBack(String playerName) {
        String format = languageDictionary.getWord(language, "command_here_feedback");
        return String.format(format, playerName);
    }

    @Override
    public String formatHereFailedFeedBack() {
        return languageDictionary.getWord(language, "command_here_failed_feedback");
    }
}
