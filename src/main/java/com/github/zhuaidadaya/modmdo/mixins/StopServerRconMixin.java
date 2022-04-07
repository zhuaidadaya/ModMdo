package com.github.zhuaidadaya.modmdo.mixins;

import net.minecraft.server.rcon.RconBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.management.ManagementFactory;

import static com.github.zhuaidadaya.modmdo.storage.Variables.LOGGER;
import static com.github.zhuaidadaya.modmdo.storage.Variables.server;

@Mixin(RconBase.class)
public class StopServerRconMixin {

    /**
     * @author 草awa
     */
    @Inject(at = @At("HEAD"),method = "stop")
    public void stop(CallbackInfo ci) {
        boolean in = false;
        if(server.isRunning()) {
            in = true;
            if(server.getThread() != null) {
                int i = 0;

                while(server.getThread().isAlive()) {
                    try {
                        LOGGER.info("failed to stop server, ModMdo trying stop again");

                        i++;

                        if(i > 5) {
                            LOGGER.info("failed to stop server in 5 times try, ModMdo trying force stop task");
                            break;
                        }
                    } catch(Exception ex) {
                        break;
                    }
                }
            }
        }

        if(in) {
            LOGGER.info("trying kill task by task manager");

            String name = ManagementFactory.getRuntimeMXBean().getName();
            String pid = name.split("@")[0];

            try {
                if(System.getProperty("os.name").contains("Linux"))
                    Runtime.getRuntime().exec("kill -9 " + pid);
                else
                    Runtime.getRuntime().exec("taskkill /F /PID " + pid);

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {

                }

            } catch (Exception e) {

            }
            LOGGER.info("ModMdo cannot stop server! it will waiting for a long time!");
        }
    }
}
