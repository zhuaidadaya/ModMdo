package com.github.zhuaidadaya.modmdo.commands;

import com.github.zhuaidadaya.modmdo.utils.times.*;
import com.github.zhuaidadaya.modmdo.utils.usr.User;
import com.github.zhuaidadaya.modmdo.utils.usr.UserUtil;
import com.github.zhuaidadaya.modmdo.utils.command.SimpleCommandOperation;
import com.github.zhuaidadaya.modmdo.whitelist.*;
import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import org.json.JSONObject;

import java.util.Locale;

import static com.github.zhuaidadaya.modmdo.storage.Variables.*;
import static net.minecraft.server.command.CommandManager.literal;

public class ModMdoUserCommand extends SimpleCommandOperation implements ConfigurableCommand {
    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("user").then(literal("hereMessage").executes(getHereReceive -> {
                if(commandApplyToPlayer(1, getPlayer(getHereReceive), this, getHereReceive)) {
                    ServerPlayerEntity player = getHereReceive.getSource().getPlayer();
                    getHereReceive.getSource().sendFeedback(formatProfileReturnMessage("receiveHereMessage", getUserHereReceive(player.getUuid())), false);
                }
                return 2;
            }).then(literal("receive").executes(receive -> {
                if(commandApplyToPlayer(1, getPlayer(receive), this, receive)) {
                    ServerPlayerEntity player = receive.getSource().getPlayer();
                    setUserProfile(new User(player.getName().asString(), player.getUuid()), "receiveHereMessage", "receive");
                    receive.getSource().sendFeedback(receiveHereMessage(), false);
                }
                return 1;
            })).then(literal("rejection").executes(rejection -> {
                if(commandApplyToPlayer(1, getPlayer(rejection), this, rejection)) {
                    ServerPlayerEntity player = rejection.getSource().getPlayer();
                    setUserProfile(new User(player.getName().asString(), player.getUuid()), "receiveHereMessage", "rejection");
                    rejection.getSource().sendFeedback(rejectionHereMessage(), false);
                }
                return 0;
            }))).then(literal("deadMessage").executes(getHereReceive -> {
                if(commandApplyToPlayer(1, getPlayer(getHereReceive), this, getHereReceive)) {
                    ServerPlayerEntity player = getHereReceive.getSource().getPlayer();
                    getHereReceive.getSource().sendFeedback(formatProfileReturnMessage("receiveDeadMessage", getUserDeadMessageReceive(player.getUuid())), false);
                }
                return 2;
            }).then(literal("receive").executes(receive -> {
                if(commandApplyToPlayer(1, getPlayer(receive), this, receive)) {
                    ServerPlayerEntity player = receive.getSource().getPlayer();
                    setUserProfile(new User(player.getName().asString(), player.getUuid()), "receiveDeadMessage", "receive");
                    receive.getSource().sendFeedback(receiveDeadMessage(), false);
                }
                return 1;
            })).then(literal("rejection").executes(rejection -> {
                if(commandApplyToPlayer(1, getPlayer(rejection), this, rejection)) {
                    ServerPlayerEntity player = rejection.getSource().getPlayer();
                    setUserProfile(new User(player.getName().asString(), player.getUuid()), "receiveDeadMessage", "rejection");
                    rejection.getSource().sendFeedback(rejectionDeadMessage(), false);
                }
                return 0;
            }))));
        });
    }

    public TranslatableText formatProfileReturnMessage(String profileKey, String profileValue) {
        return new TranslatableText("profile.feedback", profileKey, profileValue);
    }

    public TranslatableText rejectionHereMessage() {
        return new TranslatableText("command.here.rejection");
    }

    public TranslatableText receiveHereMessage() {
        return new TranslatableText("command.here.receive");
    }

    public TranslatableText rejectionDeadMessage() {
        return new TranslatableText("dead.rejection");
    }

    public TranslatableText receiveDeadMessage() {
        return new TranslatableText("dead.receive");
    }

    public void init() {
        Object projectConf = config.getConfig("user_profiles");
        if(projectConf != null) {
            users = new UserUtil(new JSONObject(projectConf.toString()));
        } else {
            users = new UserUtil();
            config.set("user_profiles", new JSONObject());
        }

        updateUserProfiles();
    }
}
