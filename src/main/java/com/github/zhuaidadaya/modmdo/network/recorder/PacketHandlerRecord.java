package com.github.zhuaidadaya.modmdo.network.recorder;

public class PacketHandlerRecord {
    private final String name;
    private long count;

    public PacketHandlerRecord(String name) {
        this.name = name;
    }

    public void add() {
        count++;
    }

    public long getCount() {
        return count;
    }

    public String getName() {
        return name;
    }
}
