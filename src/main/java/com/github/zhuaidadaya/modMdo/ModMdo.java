package com.github.zhuaidadaya.modMdo;

import com.github.zhuaidadaya.MCH.Utils.Config.ConfigUtil;
import com.github.zhuaidadaya.modMdo.Commands.*;
import com.github.zhuaidadaya.modMdo.Lang.LanguageDictionary;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

import static com.github.zhuaidadaya.modMdo.Storage.Variables.*;
import static net.minecraft.server.command.CommandManager.literal;

public class ModMdo implements ModInitializer {
    @Override
    public void onInitialize() {
        LOGGER.info("loading for ModMdo");

        config = new ConfigUtil("config/","ModMdo.mhf",entrust).setNote("""
                this file is database file of "ModMdo"
                not configs only
                so this file maybe get large and large
                but usually, it will smaller than 1MB
                
                """);

        languageDictionary = new LanguageDictionary("/format/format.json");

        new ArgumentInit().init();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("here").executes(context -> {
                return new HereCommand().here(context);
            }));
            dispatcher.register(literal("dhere").executes(context -> {
                return new DimensionHereCommand().dhere(context);
            }));
        });

        new ProjectCommand().register();
        new ModMdoUserCommand().register();
    }
}
