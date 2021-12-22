package com.github.zhuaidadaya.modMdo.mixins;

import com.github.zhuaidadaya.MCH.times.TimeType;
import com.github.zhuaidadaya.MCH.times.Times;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

import static com.github.zhuaidadaya.modMdo.storage.Variables.*;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Shadow
    @Final
    private Map<RegistryKey<World>, ServerWorld> worlds;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickStart(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if(enableTickAnalyzer) {
            tickMap.put("tick_start", System.currentTimeMillis());
            tickStartTime = Times.getTime(TimeType.ALL);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void tickEnd(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if(enableTickAnalyzer) {
            StringBuilder result = new StringBuilder();
            tickMap.put("tick_time", System.currentTimeMillis() - tickMap.get("tick_start"));
            long baseTickStart = tickMap.get("tick_worlds_start");
            result.append("tick_").append(baseTickStart).append("(").append(tickStartTime).append(")").append("\n");
            result.append("tick: ").append(tickMap.get("tick_time")).append("ms").append("\n");
            result.append("|--tick world: ").append(tickMap.get("tick_worlds_time")).append("ms").append("\n");
            result.append("|    |--step start: ").append(tickMap.get("tick_start") - baseTickStart).append("ms*").append("\n");
            result.append("|    |--worlds: ").append(worlds.size()).append("\n");
            for(int i = 1; i < worlds.size() + 1; i++) {
                String name = "unknown";
                switch(i) {
                    case 1 -> name = "overworld?";
                    case 2 -> name = "the_nether?";
                    case 3 -> name = "the_end?";
                }

                try {
                    long tickWorldTime = tickMap.get("tick_world" + i + "_time");
                    long tickWorldStart = tickMap.get("tick_world" + i + "_start");
                    long tickEntitiesTime = tickMap.get("tick_world" + i + "_entities_time");
                    long tickEntitiesStart = tickMap.get("tick_world" + i + "_entities_start");
                    long tickEntitiesLoadChunksTime = tickMap.get("tick_world" + i + "_entities_load_chunk_time");
                    long tickEntitiesLoadChunksStart = tickMap.get("tick_world" + i + "_entities_load_chunk_start");
                    long tickEntitiesUnloadChunksTime = tickMap.get("tick_world" + i + "_entities_unload_chunk_time");
                    long tickEntitiesUnloadChunksStart = tickMap.get("tick_world" + i + "_entities_unload_chunk_start");
                    long tickWorldChunksTime = tickMap.get("tick_world" + i + "_chunks_time");
                    long tickWorldChunksStart = tickMap.get("tick_world" + i + "_chunks_start");
                    result.append("|    |    |--world").append(i).append("(").append(name).append("): ").append(tickWorldTime).append("ms").append("\n");
                    result.append("|    |    |    |--step start: ").append(tickWorldStart - baseTickStart).append("ms*").append("\n");
                    result.append("|    |    |    |--tick chunks: ").append(tickWorldChunksTime).append("ms").append("\n");
                    result.append("|    |    |    |    |--step time: ").append(tickWorldChunksStart - baseTickStart).append("ms").append("\n");
                    result.append("|    |    |    |--tick entities(W): ").append(tickEntitiesTime).append("ms").append("\n");
                    result.append("|    |    |    |    |--step start: ").append(tickEntitiesStart - baseTickStart).append("ms*").append("\n");
                    result.append("|    |    |    |    |    |--entities: ").append(tickMap.get("world" + i + "_entities")).append("\n");
                    LinkedHashMap<String, Integer> entities = tickEntitiesMap.get("world" + i + "_entities");
                    for(String s : entities.keySet()) {
                        result.append("|    |    |    |    |    |    |--").append(s).append(": ").append(entities.get(s)).append("\n");
                    }
                    result.append("|    |    |    |    |--load chunks(M): ").append(tickEntitiesLoadChunksTime).append("ms").append("\n");
                    result.append("|    |    |    |    |    |--step start: ").append(tickEntitiesLoadChunksStart - baseTickStart).append("ms*").append("\n");
                    result.append("|    |    |    |    |--unload chunks(M): ").append(tickEntitiesUnloadChunksTime).append("ms").append("\n");
                    result.append("|    |    |    |    |    |--step start: ").append(tickEntitiesUnloadChunksStart - baseTickStart).append("ms*").append("\n");
                } catch (Exception e) {
                    result.append("|    |    |--world").append(i).append("(unknown): 0ms").append("\n");
                }
            }
            result.append("|--network IO: ").append(tickMap.get("tick_network_time")).append("ms").append("\n");
            result.append("|    |--step start: ").append(tickMap.get("tick_network_start") - baseTickStart).append("ms*").append("\n");

            try {
                new File(tickAnalyzerFile).getParentFile().mkdirs();
                BufferedWriter writer = new BufferedWriter(new FileWriter(tickAnalyzerFile,true));
                writer.write(result.toString());
                writer.close();
            } catch (Exception e) {

            }

            analyzedTick++;

            LOGGER.info("snap tick(" + analyzedTick + "tick), cached time analyzed");

            if(shortAnalyze & analyzedTick > 59) {
                enableTickAnalyzer = false;

                LOGGER.info("tick analyze finished, result at " + tickAnalyzerFile);
            }
        }
    }

    @Inject(method = "tickWorlds", at = @At("HEAD"))
    public void tickWorldsStart(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if(enableTickAnalyzer) {
            tickMap.put("tick_worlds_start", System.currentTimeMillis());
            tickMap.put("ticking_world", 1L);
            tickEntitiesMap = new LinkedHashMap<>();
        }
    }

    @Inject(method = "tickWorlds", at = @At("RETURN"))
    public void tickWorldsEnd(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if(enableTickAnalyzer) {
            tickMap.put("tick_worlds_time", System.currentTimeMillis() - tickMap.get("tick_worlds_start"));
        }
    }
}
