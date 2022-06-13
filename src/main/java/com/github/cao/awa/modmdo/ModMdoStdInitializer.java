package com.github.cao.awa.modmdo;

import com.github.cao.awa.modmdo.event.*;
import com.github.cao.awa.modmdo.extra.loader.*;
import com.github.cao.awa.modmdo.identifier.*;
import com.github.cao.awa.modmdo.listeners.*;
import com.github.cao.awa.modmdo.reads.*;
import com.github.cao.awa.modmdo.resourceLoader.*;
import com.github.cao.awa.modmdo.storage.*;
import com.github.cao.awa.modmdo.type.*;
import com.github.cao.awa.modmdo.utils.enchant.*;
import com.github.cao.awa.modmdo.utils.usr.*;
import com.github.zhuaidadaya.rikaishinikui.handler.config.*;
import com.github.zhuaidadaya.rikaishinikui.handler.universal.entrust.*;
import net.fabricmc.api.*;
import net.minecraft.server.*;
import net.minecraft.util.*;
import org.json.*;

import java.io.*;
import java.nio.charset.*;

import static com.github.cao.awa.modmdo.storage.SharedVariables.*;

public class ModMdoStdInitializer implements ModInitializer {
    public static void initForLevel(MinecraftServer server) {
        TRACKER.submit("ModMdo extra init");
        SharedVariables.extras.getExtra(ModMdo.class, SharedVariables.EXTRA_ID).setServer(server);
        SharedVariables.extras.load();

        loadEvent(false);
    }

    public static Pair<Integer, Integer> loadEvent(boolean reload) {
        TRACKER.submit(reload ? "ModMdo event reloading" : "ModMdo event loading");
        int old = 0;
        if (event != null) {
            old = event.registered();
        }
        event = new ModMdoEventTracer();
        event.build();
        return new Pair<>(event.registered(), old);
    }

    public static void initModMdoVariables(ModMdoType type) {
        if (type == ModMdoType.SERVER) {
            TRACKER.submit("Init level as server mode");
        } else {
            TRACKER.submit("Init level as client mode");
        }
        EntrustExecution.notNull(config.getConfigBoolean("here_command"), here -> {
            SharedVariables.enableHereCommand = here;
            TRACKER.submit("Init config here_command as " + here);
        });
        EntrustExecution.notNull(config.getConfigBoolean("secure_enchant"), enchant -> {
            SharedVariables.enableSecureEnchant = enchant;
            TRACKER.submit("Init config secure_enchant as " + enchant);
        });
        EntrustExecution.notNull(config.getConfigBoolean("time_active"), tac -> {
            SharedVariables.timeActive = tac;
            TRACKER.submit("Init config time_active as " + tac);
        });
        EntrustExecution.notNull(config.getConfigBoolean("enchantment_clear_if_level_too_high"), clear -> {
            SharedVariables.clearEnchantIfLevelTooHigh = clear;
            TRACKER.submit("Init config enchantment_clear_if_level_too_high as " + clear);
        });
        EntrustExecution.notNull(config.getConfigBoolean("reject_no_fall_chest"), rejectNoFall -> {
            SharedVariables.rejectNoFallCheat = rejectNoFall;
            TRACKER.submit("Init config reject_no_fall_chest as " + rejectNoFall);
        });
        EntrustExecution.notNull(config.getConfigBoolean("modmdo_whitelist"), whitelist -> {
            SharedVariables.modmdoWhitelist = whitelist;
            TRACKER.submit("Init config modmdo_whitelist as " + whitelist);
        });

        if (type == ModMdoType.SERVER) {
            SharedVariables.initWhiteList();
            SharedVariables.initBan();
        }
    }

    @Override
    public void onInitialize() {
        EntrustExecution.tryAssertNotNull(System.getProperty("-DmodmdoDebug=true"), debug -> {
            SharedVariables.debug = Boolean.parseBoolean(debug);
            TRACKER.submit("Init modmdo debug as " + debug);
        });
        TRACKER.submit("ModMdo loading");
        TRACKER.info("Loading ModMdo " + SharedVariables.VERSION_ID + " (step 1/2)");
        TRACKER.info("ModMdo Std Initiator running");
        TRACKER.info("Loading for ModMdo Std init");

        staticConfig = new DiskObjectConfigUtil("ModMdo", "config/modmdo", "modmdo", false);

        staticConfig.setIfNoExist("identifier", RandomIdentifier.randomIdentifier());

        event = new ModMdoEventTracer();

        SharedVariables.loginUsers = new UserUtil();
        SharedVariables.rejectUsers = new UserUtil();

        parseMapFormat();

        new ServerStartListener().listener();
        new ServerTickListener().listener();

        Resource<String> enchant = new Resource<>();
        enchant.set("enchantment_level", "/assets/modmdo/format/enchantment_level.json");
        SharedVariables.enchantLevelController = new EnchantLevelController(enchant);

        TRACKER.info("Registering for ModMdo major");
        SharedVariables.extras = new ModMdoExtraLoader(new ModMdo().setName("ModMdo").setId(SharedVariables.EXTRA_ID), RandomIdentifier.randomIdentifier());

        TRACKER.info("Registering for ModMdo extra");
        for (ModMdoExtra<?> extra : SharedVariables.extrasWaitingForRegister) {
            SharedVariables.extras.register(extra.getId(), extra);
        }

        SharedVariables.loaded = true;
    }

    public void parseMapFormat() {
        try {
            JSONObject versionMap = new JSONObject(FileReads.read(new BufferedReader(new InputStreamReader(Resources.getResource("/assets/modmdo/versions/versions_map.json", getClass()), StandardCharsets.UTF_8))));

            for (String s : versionMap.keySet())
                SharedVariables.modMdoIdToVersionMap.put(Integer.valueOf(s), versionMap.getString(s));

            for (String s : versionMap.keySet())
                SharedVariables.modMdoVersionToIdMap.put(versionMap.getString(s), Integer.valueOf(s).intValue());
        } catch (Exception e) {

        }
    }
}
