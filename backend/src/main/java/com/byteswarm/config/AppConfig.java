package com.byteswarm.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties props = new Properties();
    private static boolean loaded = false;

    private AppConfig() {}

    public static synchronized void load() {
        if (loaded) return;
        try (InputStream in = AppConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
                loaded = true;
                System.out.println("✅ Loaded application.properties");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    public static String get(String key) {
        if (!loaded) load();
        return props.getProperty(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static int getInt(String key, int defaultValue) {
        String v = get(key);
        return v == null ? defaultValue : Integer.parseInt(v);
    }
}