package com.github.zhuaidadaya.modmdo.commands;

import com.github.zhuaidadaya.modmdo.commands.argument.*;
import com.github.zhuaidadaya.modmdo.lang.Language;
import com.github.zhuaidadaya.modmdo.permission.PermissionLevel;
import com.github.zhuaidadaya.modmdo.storage.Variables;
import com.github.zhuaidadaya.modmdo.utils.command.SimpleCommandOperation;
import com.github.zhuaidadaya.modmdo.utils.translate.TranslateUtil;
import com.github.zhuaidadaya.modmdo.whitelist.*;
import com.github.zhuaidadaya.rikaishinikui.handler.universal.entrust.*;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.*;
import com.mojang.brigadier.exceptions.*;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EnchantmentArgumentType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.server.command.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.Locale;

import static com.github.zhuaidadaya.modmdo.storage.Variables.*;
import static com.github.zhuaidadaya.modmdo.storage.Variables.getLanguage;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ModMdoConfigCommand extends SimpleCommandOperation implements SimpleCommand {
    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("modmdo").requires(level -> level.hasPermissionLevel(4)).then(literal("here").executes(here -> {
                sendFeedback(here, formatConfigReturnMessage("here_command"), 1);
                return 2;
            }).then(literal("enable").executes(enableHere -> {
                enableHereCommand = true;
                updateModMdoVariables();
                sendFeedback(enableHere, formatEnableHere(), 1);
                return 1;
            })).then(literal("disable").executes(disableHere -> {
                if (commandApplyToPlayer(1, getPlayer(disableHere), this, disableHere)) {
                    enableHereCommand = false;
                    updateModMdoVariables();
                    sendFeedback(disableHere, formatDisableHere());
                }
                return 0;
            }))).then(literal("secureEnchant").executes(secureEnchant -> {
                if (commandApplyToPlayer(1, getPlayer(secureEnchant), this, secureEnchant)) {

                    sendFeedback(secureEnchant, formatConfigReturnMessage("secure_enchant"));
                }
                return 2;
            }).then(literal("enable").executes(enableSecureEnchant -> {
                if (commandApplyToPlayer(1, getPlayer(enableSecureEnchant), this, enableSecureEnchant)) {
                    Variables.enableSecureEnchant = true;
                    updateModMdoVariables();
                    sendFeedback(enableSecureEnchant, formatEnableSecureEnchant());
                }
                return 1;
            })).then(literal("disable").executes(disableSecureEnchant -> {
                if (commandApplyToPlayer(1, getPlayer(disableSecureEnchant), this, disableSecureEnchant)) {
                    enableSecureEnchant = false;
                    updateModMdoVariables();
                    sendFeedback(disableSecureEnchant, formatDisableSecureEnchant());
                }
                return 0;
            }))).then(literal("useModMdoWhitelist").executes(whitelist -> {
                if (commandApplyToPlayer(1, getPlayer(whitelist), this, whitelist)) {
                    updateModMdoVariables();

                    sendFeedback(whitelist, formatConfigReturnMessage("modmdo_whitelist"));
                }
                return 2;
            }).then(literal("enable").executes(enableWhitelist -> {
                if (commandApplyToPlayer(1, getPlayer(enableWhitelist), this, enableWhitelist)) {
                    config.set("modmdo_whitelist", true);
                    updateModMdoVariables();

                    sendFeedback(enableWhitelist, formatUseModMdoWhitelist());

                    for (ServerPlayerEntity player : getServer(enableWhitelist).getPlayerManager().getPlayerList()) {
                        EntrustExecution.tryTemporary(() -> {
                            if (config.getConfigBoolean("whitelist_only_id")) {
                                if (whitelist.getFromId(loginUsers.getUser(player).getIdentifier()) == null) {
                                    throw new Exception();
                                }
                            } else {
                                if (! whitelist.get(player.getName().asString()).getIdentifier().equals(loginUsers.getUser(player).getIdentifier())) {
                                    throw new Exception();
                                }
                            }
                        }, () -> {
                            if (player.networkHandler.connection.isOpen()) {
                                player.networkHandler.disconnect(new TranslatableText("multiplayer.disconnect.not_whitelisted"));
                            }
                        });
                    }
                }
                return 1;
            })).then(literal("disable").executes(disableWhitelist -> {
                if (commandApplyToPlayer(1, getPlayer(disableWhitelist), this, disableWhitelist)) {
                    config.set("modmdo_whitelist", false);
                    updateModMdoVariables();
                    sendFeedback(disableWhitelist, formatDisableModMdoWhitelist());
                }
                return 0;
            }))).then(literal("rejectReconnect").executes(rejectReconnect -> {
                if (commandApplyToPlayer(1, getPlayer(rejectReconnect), this, rejectReconnect)) {
                    sendFeedback(rejectReconnect, formatConfigReturnMessage("reject_reconnect"));
                }
                return 2;
            }).then(literal("enable").executes(reject -> {
                if (commandApplyToPlayer(1, getPlayer(reject), this, reject)) {
                    enableRejectReconnect = true;
                    updateModMdoVariables();

                    sendFeedback(reject, formatEnableRejectReconnect());
                }
                return 1;
            })).then(literal("disable").executes(receive -> {
                if (commandApplyToPlayer(1, getPlayer(receive), this, receive)) {
                    enableRejectReconnect = false;
                    updateModMdoVariables();
                    sendFeedback(receive, formatDisableRejectReconnect());
                }
                return 0;
            }))).then(literal("deadMessage").executes(deadMessage -> {
                if (commandApplyToPlayer(1, getPlayer(deadMessage), this, deadMessage)) {
                    sendFeedback(deadMessage, formatConfigReturnMessage("dead_message"));
                }
                return 2;
            }).then(literal("enable").executes(enabled -> {
                if (commandApplyToPlayer(1, getPlayer(enabled), this, enabled)) {
                    enableDeadMessage = true;
                    updateModMdoVariables();

                    sendFeedback(enabled, formatEnableDeadMessage());
                }
                return 1;
            })).then(literal("disable").executes(disable -> {
                if (commandApplyToPlayer(1, getPlayer(disable), this, disable)) {

                    enableDeadMessage = false;
                    updateModMdoVariables();
                    sendFeedback(disable, formatDisabledDeadMessage());
                }
                return 0;
            }))).then(literal("itemDespawnTicks").executes(getDespawnTicks -> {
                if (commandApplyToPlayer(1, getPlayer(getDespawnTicks), this, getDespawnTicks)) {
                    sendFeedback(getDespawnTicks, formatItemDespawnTicks());
                }
                return 2;
            }).then(literal("become").then(argument("ticks", IntegerArgumentType.integer(- 1)).executes(setTicks -> {
                if (commandApplyToPlayer(1, getPlayer(setTicks), this, setTicks)) {
                    itemDespawnAge = Integer.parseInt(setTicks.getInput().split(" ")[3]);

                    sendFeedbackAndInform(setTicks, formatItemDespawnTicks());
                }
                return 1;
            }))).then(literal("original").executes(setTicksToDefault -> {
                if (commandApplyToPlayer(1, getPlayer(setTicksToDefault), this, setTicksToDefault)) {
                    itemDespawnAge = 6000;

                    sendFeedbackAndInform(setTicksToDefault, formatItemDespawnTicks());
                }
                return 2;
            }))).then(literal("tickingEntities").executes(getTickingEntities -> {
                if (commandApplyToPlayer(1, getPlayer(getTickingEntities), this, getTickingEntities)) {
                    sendFeedbackAndInform(getTickingEntities, formatTickingEntitiesTick());
                }
                return 0;
            }).then(literal("enable").executes(enableTickingEntities -> {
                if (commandApplyToPlayer(1, getPlayer(enableTickingEntities), this, enableTickingEntities)) {
                    cancelEntitiesTick = false;

                    sendFeedbackAndInform(enableTickingEntities, formatTickingEntitiesTick());
                }
                return 1;
            })).then(literal("disable").executes(disableTickingEntities -> {
                if (commandApplyToPlayer(1, getPlayer(disableTickingEntities), this, disableTickingEntities)) {
                    cancelEntitiesTick = true;

                    sendFeedbackAndInform(disableTickingEntities, formatTickingEntitiesTick());
                }
                return 2;
            }))).then(literal("joinServerFollow").executes(getJoinServerFollowLimit -> {
                if (commandApplyToPlayer(10, getPlayer(getJoinServerFollowLimit), this, getJoinServerFollowLimit)) {
                    sendFeedback(getJoinServerFollowLimit, formatJoinGameFollow());
                }
                return 0;
            }).then(literal("disable").executes(disableJoinServerFollow -> {
                if (commandApplyToPlayer(10, getPlayer(disableJoinServerFollow), this, disableJoinServerFollow)) {
                    config.set("join_server_follow", PermissionLevel.UNABLE);

                    sendFeedback(disableJoinServerFollow, formatJoinGameFollow());
                }
                return 1;
            })).then(literal("all").executes(enableJoinServerFollowForAll -> {
                if (commandApplyToPlayer(10, getPlayer(enableJoinServerFollowForAll), this, enableJoinServerFollowForAll)) {
                    config.set("join_server_follow", PermissionLevel.ALL);

                    sendFeedback(enableJoinServerFollowForAll, formatJoinGameFollow());
                }
                return 2;
            })).then(literal("ops").executes(enableJoinServerFollowForOps -> {
                if (commandApplyToPlayer(10, getPlayer(enableJoinServerFollowForOps), this, enableJoinServerFollowForOps)) {
                    config.set("join_server_follow", PermissionLevel.OPS);

                    sendFeedback(enableJoinServerFollowForOps, formatJoinGameFollow());
                }
                return 3;
            }))).then(literal("runCommandFollow").executes(getJoinServerFollowLimit -> {
                if (commandApplyToPlayer(10, getPlayer(getJoinServerFollowLimit), this, getJoinServerFollowLimit)) {
                    sendFeedback(getJoinServerFollowLimit, formatRunCommandFollow());
                }
                return 0;
            }).then(literal("disable").executes(disableJoinServerFollow -> {
                if (commandApplyToPlayer(10, getPlayer(disableJoinServerFollow), this, disableJoinServerFollow)) {
                    config.set("run_command_follow", PermissionLevel.UNABLE);

                    sendFeedback(disableJoinServerFollow, formatRunCommandFollow());
                }
                return 1;
            })).then(literal("all").executes(enableJoinServerFollowForAll -> {
                if (commandApplyToPlayer(10, getPlayer(enableJoinServerFollowForAll), this, enableJoinServerFollowForAll)) {
                    config.set("run_command_follow", PermissionLevel.ALL);

                    sendFeedback(enableJoinServerFollowForAll, formatRunCommandFollow());
                }
                return 2;
            })).then(literal("ops").executes(enableJoinServerFollowForOps -> {
                if (commandApplyToPlayer(10, getPlayer(enableJoinServerFollowForOps), this, enableJoinServerFollowForOps)) {
                    config.set("run_command_follow", PermissionLevel.OPS);

                    sendFeedback(enableJoinServerFollowForOps, formatRunCommandFollow());
                }
                return 3;
            }))).then(literal("timeActive").executes(getTimeActive -> {
                if (commandApplyToPlayer(15, getPlayer(getTimeActive), this, getTimeActive)) {
                    sendFeedback(getTimeActive, formatConfigReturnMessage("time_active"));
                }
                return 0;
            }).then(literal("enable").executes(enableTimeActive -> {
                if (commandApplyToPlayer(15, getPlayer(enableTimeActive), this, enableTimeActive)) {
                    timeActive = true;

                    updateModMdoVariables();

                    sendFeedback(enableTimeActive, formatConfigReturnMessage("time_active"));
                }
                return 0;
            })).then(literal("disable").executes(disableTimeActive -> {
                if (commandApplyToPlayer(15, getPlayer(disableTimeActive), this, disableTimeActive)) {
                    timeActive = false;

                    updateModMdoVariables();

                    sendFeedback(disableTimeActive, formatConfigReturnMessage("time_active"));
                }
                return 0;
            }))).then(literal("loginCheckTimeLimit").executes(getTimeLimit -> {
                if (commandApplyToPlayer(16, getPlayer(getTimeLimit), this, getTimeLimit)) {
                    sendFeedback(getTimeLimit, formatCheckerTimeLimit());
                }
                return 0;
            }).then(argument("ms", IntegerArgumentType.integer(500)).executes(setTimeLimit -> {
                if (commandApplyToPlayer(16, getPlayer(setTimeLimit), this, setTimeLimit)) {
                    config.set("checker_time_limit", IntegerArgumentType.getInteger(setTimeLimit, "ms"));

                    updateModMdoVariables();

                    sendFeedback(setTimeLimit, formatCheckerTimeLimit());
                }
                return 0;
            }))).then(literal("language").executes(getLanguage -> {
                sendFeedback(getLanguage, new TranslatableText("language.default", getLanguage()), 20);
                return 0;
            }).then(literal("chinese").executes(chinese -> {
                config.set("default_language", Language.CHINESE);
                updateModMdoVariables();
                sendFeedback(chinese, new TranslatableText("language.default", getLanguage()), 20);
                return 0;
            })).then(literal("english").executes(english -> {
                config.set("default_language", Language.ENGLISH);
                updateModMdoVariables();
                sendFeedback(english, new TranslatableText("language.default", getLanguage()), 20);
                return 0;
            }))).then(literal("maxEnchantmentLevel").executes(getEnchantControlEnable -> {
                sendFeedback(getEnchantControlEnable, TranslateUtil.translatableText(enchantLevelController.isEnabledControl() ? "enchantment.level.controller.enabled" : "enchantment.level.controller.disabled"), 21);
                return 0;
            }).then(literal("enable").executes(enableEnchantLimit -> {
                enchantLevelController.setEnabledControl(true);
                saveEnchantmentMaxLevel();
                sendFeedback(enableEnchantLimit, TranslateUtil.translatableText(enchantLevelController.isEnabledControl() ? "enchantment.level.controller.enabled" : "enchantment.level.controller.disabled"), 21);
                return 0;
            })).then(literal("disable").executes(disableEnchantLimit -> {
                enchantLevelController.setEnabledControl(false);
                saveEnchantmentMaxLevel();
                sendFeedback(disableEnchantLimit, TranslateUtil.translatableText(enchantLevelController.isEnabledControl() ? "enchantment.level.controller.enabled" : "enchantment.level.controller.disabled"), 21);
                return 0;
            })).then(literal("limit").then(literal("all").then(argument("all", IntegerArgumentType.integer(0, Short.MAX_VALUE)).executes(setDef -> {
                short level = (short) IntegerArgumentType.getInteger(setDef, "all");
                enchantLevelController.setAll(level);
                sendFeedback(setDef, new TranslatableText("enchantment.max.level.limit.all", level), 21);
                return 0;
            })).then(literal("default").executes(recoveryAll -> {
                enchantLevelController.allDefault();
                sendFeedback(recoveryAll, new TranslatableText("enchantment.max.level.limit.all.default"), 21);
                return 0;
            }))).then(literal("appoint").then(argument("appoint", EnchantmentArgumentType.enchantment()).executes(getLimit -> {
                Identifier name = EnchantmentHelper.getEnchantmentId(EnchantmentArgumentType.getEnchantment(getLimit, "appoint"));
                short level = enchantLevelController.get(name).getMax();
                sendFeedback(getLimit, new TranslatableText("enchantment.max.level.limit", name, level), 21);
                saveEnchantmentMaxLevel();
                return 0;
            }).then(argument("limit", IntegerArgumentType.integer(0, Short.MAX_VALUE)).executes(setLimit -> {
                Identifier name = EnchantmentHelper.getEnchantmentId(EnchantmentArgumentType.getEnchantment(setLimit, "appoint"));
                short level = (short) IntegerArgumentType.getInteger(setLimit, "limit");
                enchantLevelController.set(name, level);
                saveEnchantmentMaxLevel();
                sendFeedback(setLimit, new TranslatableText("enchantment.max.level.limit", name, level), 21);
                return 0;
            })).then(literal("default").executes(recoveryLevel -> {
                Identifier name = EnchantmentHelper.getEnchantmentId(EnchantmentArgumentType.getEnchantment(recoveryLevel, "appoint"));
                short level = enchantLevelController.get(name).getDefaultMax();
                enchantLevelController.set(name, level);
                saveEnchantmentMaxLevel();
                sendFeedback(recoveryLevel, new TranslatableText("enchantment.max.level.limit", name, level), 21);
                return 0;
            })))))).then(literal("clearEnchantIfLevelTooHigh").executes(getClear -> {
                sendFeedback(getClear, TranslateUtil.formatRule("enchantment_clear_if_level_too_high", clearEnchantIfLevelTooHigh ? "enabled" : "disabled"), 21);
                return 0;
            }).then(literal("enable").executes(enableClear -> {
                clearEnchantIfLevelTooHigh = true;
                updateModMdoVariables();
                sendFeedback(enableClear, TranslateUtil.formatRule("enchantment_clear_if_level_too_high", "enabled"), 21);
                return 0;
            })).then(literal("disable").executes(disableClear -> {
                clearEnchantIfLevelTooHigh = false;
                updateModMdoVariables();
                sendFeedback(disableClear, TranslateUtil.formatRule("enchantment_clear_if_level_too_high", "disabled"), 21);
                return 0;
            }))).then(literal("rejectNoFallCheat").executes(getRejectNoFall -> {
                sendFeedback(getRejectNoFall, new TranslatableText(rejectNoFallCheat ? "player.no.fall.cheat.reject" : "player.no.fall.cheat.receive"), 21);
                return 0;
            }).then(literal("enable").executes(reject -> {
                rejectNoFallCheat = true;
                updateModMdoVariables();
                sendFeedback(reject, new TranslatableText(rejectNoFallCheat ? "player.no.fall.cheat.reject" : "player.no.fall.cheat.receive"), 21);
                return 0;
            })).then(literal("disable").executes(receive -> {
                rejectNoFallCheat = false;
                updateModMdoVariables();
                sendFeedback(receive, new TranslatableText(rejectNoFallCheat ? "player.no.fall.cheat.reject" : "player.no.fall.cheat.receive"), 21);
                return 0;
            }))).then(literal("onlyCheckIdentifier").executes(check -> {
                sendFeedback(check, formatConfigReturnMessage("whitelist_only_id"));
                return 0;
            }).then(literal("enable").executes(enable -> {
                config.set("whitelist_only_id", true);
                sendFeedback(enable, formatConfigReturnMessage("whitelist_only_id"));
                return 0;
            })).then(literal("disable").executes(disable -> {
                config.set("whitelist_only_id", false);
                sendFeedback(disable, formatConfigReturnMessage("whitelist_only_id"));
                return 0;
            }))).then(literal("whitelist").then(literal("remove").then(argument("name", ModMdoWhitelistArgumentType.whitelist()).executes(remove -> {
                Whitelist wl = ModMdoWhitelistArgumentType.getWhiteList(remove, "name");
                if (whitelist.containsName(wl.getName())) {
                    whitelist.remove(wl.getName());
                    sendFeedback(remove, new TranslatableText("modmdo.whitelist.removed", wl.getName()));
                    updateWhitelistNames(getServer(remove), true);
                    return 0;
                }
                sendError(remove, new TranslatableText("arguments.permanent.whitelist.not.registered"), 25);
                return - 1;
            }))).then(literal("list").executes(showWhiteList -> {
                showWhitelist(showWhiteList);
                return 0;
            }))));
        });
    }

    public void showWhitelist(CommandContext<ServerCommandSource> source) throws CommandSyntaxException {
        flushTemporaryWhitelist();
        ServerPlayerEntity player = getPlayer(source);
        if (whitelist.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (Whitelist wl : whitelist.values()) {
                builder.append(wl.getName()).append(", ");
            }
            builder.delete(builder.length() - 2, builder.length());
            sendMessage(player, new TranslatableText("commands.modmdo.whitelist.list", whitelist.size(), builder.toString()), false, 22);
        } else {
            sendMessage(player, new TranslatableText("commands.modmdo.whitelist.none"), false, 22);

        }
    }

    public TranslatableText formatConfigReturnMessage(String config) {
        return new TranslatableText(config + "." + Variables.config.getConfigString(config) + ".rule.format");
    }

    public TranslatableText formatCheckerTimeLimit() {
        return new TranslatableText("checker_time_limit.rule.format", config.getConfigInt("checker_time_limit"));
    }

    public TranslatableText formatJoinGameFollow() {
        return TranslateUtil.formatRule("follow.join.server", config.getConfigString("join_server_follow").toLowerCase(Locale.ROOT));
    }

    public TranslatableText formatRunCommandFollow() {
        return TranslateUtil.formatRule("follow.run.command", config.getConfigString("run_command_follow").toLowerCase(Locale.ROOT));
    }

    public TranslatableText formatTickingEntitiesTick() {
        return new TranslatableText(cancelEntitiesTick ? "ticking.entities.false.rule.format" : "ticking.entities.true.rule.format");
    }

    public TranslatableText formatItemDespawnTicks() {
        if (itemDespawnAge > - 1) {
            return new TranslatableText("item.despawn.ticks.rule.format", itemDespawnAge);
        } else {
            return new TranslatableText("item.despawn.ticks.false.rule.format", itemDespawnAge);
        }
    }

    public TranslatableText formatEnableHere() {
        return new TranslatableText("here_command.true.rule.format");
    }

    public TranslatableText formatDisableHere() {
        return new TranslatableText("here_command.false.rule.format");
    }

    public TranslatableText formatEnableSecureEnchant() {
        return new TranslatableText("secure_enchant.true.rule.format");
    }

    public TranslatableText formatDisableSecureEnchant() {
        return new TranslatableText("secure_enchant.false.rule.format");
    }

    public TranslatableText formatUseModMdoWhitelist() {
        return new TranslatableText("modmdo_whitelist.true.rule.format");
    }


    public TranslatableText formatDisableModMdoWhitelist() {
        return new TranslatableText("modmdo_whitelist.false.rule.format");
    }

    public TranslatableText formatEnableRejectReconnect() {
        return new TranslatableText("reject_reconnect.true.rule.format");
    }


    public TranslatableText formatDisableRejectReconnect() {
        return new TranslatableText("reject_reconnect.reject.false.rule.format");
    }

    public TranslatableText formatEnableDeadMessage() {
        return new TranslatableText("dead_message.true.rule.format");
    }


    public TranslatableText formatDisabledDeadMessage() {
        return new TranslatableText("dead_message.false.rule.format");
    }
}
