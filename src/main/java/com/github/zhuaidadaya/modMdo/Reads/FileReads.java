package com.github.zhuaidadaya.modMdo.Reads;

import java.io.BufferedReader;

public class FileReads {
    public  static  String read(BufferedReader reader) {
        String cache;
        StringBuilder builder = new StringBuilder();
        try {
            while((cache = reader.readLine()) != null)
                builder.append(cache).append("\n");
        } catch (Exception e) {
            return "";
        }
        return builder.toString();
    }
}
