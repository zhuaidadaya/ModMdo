package com.github.cao.awa.modmdo.event.server.tick;

import com.github.cao.awa.modmdo.annotations.*;
import com.github.cao.awa.modmdo.event.delay.*;
import com.github.cao.awa.modmdo.event.entity.*;
import com.github.zhuaidadaya.rikaishinikui.handler.universal.entrust.*;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.entity.*;
import net.minecraft.server.*;

@Auto
public class GameTickEndEvent extends EntityTargetedEvent<GameTickEndEvent> {
    private final MinecraftServer server;

    public GameTickEndEvent(MinecraftServer server) {
        this.server = server;
    }

    private GameTickEndEvent() {
        this.server = null;
    }

    public static GameTickEndEvent snap() {
        return new GameTickEndEvent();
    }

    public MinecraftServer getServer() {
        return server;
    }

    public GameTickEndEvent fuse(Previously<GameTickEndEvent> previously, GameTickEndEvent delay) {
        return previously.target();
    }

    public String synopsis() {
        return EntrustParser.tryCreate(() -> "GameTickEndEvent{}", toString());
    }

    @Override
    public String abbreviate() {
        return "GameTickEndEvent";
    }

    public String clazz() {
        return getClass().getName();
    }

    @Override
    public ObjectArrayList<? extends Entity> getTargeted() {
        return EntrustParser.operation(new ObjectArrayList<>(), targeted -> EntrustExecution.tryTemporary(() -> targeted.addAll(server.getPlayerManager().getPlayerList())));
    }

    @Override
    public void adaptive(GameTickEndEvent event) {
        skipDelay(event);
    }
}
