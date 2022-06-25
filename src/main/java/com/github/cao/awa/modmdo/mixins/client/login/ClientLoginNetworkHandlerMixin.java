package com.github.cao.awa.modmdo.mixins.client.login;

import net.minecraft.client.*;
import net.minecraft.client.network.*;
import net.minecraft.client.util.*;
import net.minecraft.network.*;
import net.minecraft.network.encryption.*;
import net.minecraft.network.packet.c2s.login.*;
import net.minecraft.network.packet.s2c.login.*;
import net.minecraft.text.*;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;

import javax.crypto.*;
import java.math.*;
import java.security.*;
import java.util.function.*;

import static com.github.cao.awa.modmdo.storage.SharedVariables.TRACKER;

@Mixin(ClientLoginNetworkHandler.class)
public abstract class ClientLoginNetworkHandlerMixin {
    @Shadow @Final private Consumer<Text> statusConsumer;

    @Shadow @Final private ClientConnection connection;

    @Shadow @Nullable protected abstract Text joinServerSession(String serverId);

    @Shadow @Final private MinecraftClient client;

    /**
     * @author 草awa
     */
    @Overwrite
    public void onHello(LoginHelloS2CPacket packet) {
        boolean isModMdo = packet.getServerId().endsWith(":ModMdo");
        Cipher cipher;
        Cipher cipher2;
        String string;
        LoginKeyC2SPacket loginKeyC2SPacket;
        try {
            SecretKey secretKey = NetworkEncryptionUtils.generateKey();
            PublicKey publicKey = packet.getPublicKey();
            string = (new BigInteger(NetworkEncryptionUtils.generateServerId(packet.getServerId(), publicKey, secretKey))).toString(16);
            cipher = NetworkEncryptionUtils.cipherFromKey(2, secretKey);
            cipher2 = NetworkEncryptionUtils.cipherFromKey(1, secretKey);
            loginKeyC2SPacket = new LoginKeyC2SPacket(secretKey, publicKey, packet.getNonce());
        } catch (NetworkEncryptionException var8) {
            throw new IllegalStateException("Protocol error", var8);
        }

        this.statusConsumer.accept(new TranslatableText("connect.authorizing"));
        NetworkUtils.EXECUTOR.submit(() -> {
            if (!isModMdo) {
                Text text = this.joinServerSession(string);
                if (text != null) {
                    if (this.client.getCurrentServerEntry() == null || ! this.client.getCurrentServerEntry().isLocal()) {
                        this.connection.disconnect(text);
                        return;
                    }

                    TRACKER.warn(text.getString());
                }
            }

            this.statusConsumer.accept(new TranslatableText("connect.encrypting"));
            this.connection.send(loginKeyC2SPacket, (future) -> {
                this.connection.setupEncryption(cipher, cipher2);
            });
        });
    }
}
