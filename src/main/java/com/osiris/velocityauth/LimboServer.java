package com.osiris.velocityauth;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.lingala.zip4j.ZipFile;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Properties;

public class LimboServer {
    public File pluginDir = Main.INSTANCE.dataDirectory.toFile();
    public File dir = new File(pluginDir + "/limbo-server");
    public File jar = new File(dir + "/server.jar");

    public int port = 0;
    public File pluginJar = null;
    public Properties properties;
    public Process process;
    public RegisteredServer registeredServer;

    public void start() throws IOException, URISyntaxException {
        port = findFreePort();
        pluginJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI());
        Objects.requireNonNull(pluginJar);

        // Unpack limbo server stuff from jar
        if (!dir.exists() || dir.listFiles() == null || dir.listFiles().length == 0) {
            dir.mkdirs();
            try (ZipFile zip = new ZipFile(pluginJar)) {
                zip.extractFile("limbo-server/", pluginDir.getAbsolutePath()); // plugindir/limbo-server
            }
        }

        // Load and update properties
        properties = new Properties();
        try (BufferedReader reader = new BufferedReader(new FileReader(dir + "/server.properties"))) {
            properties.load(reader);
        }
        properties.put("allow-flight", ""+true);
        properties.put("allow-chat", ""+false);
        properties.put("bungeecord", ""+true);
        properties.put("default-gamemode", "spectator");
        properties.put("forwarding-secrets", new Toml().read(new File(pluginJar.getParentFile().getParentFile() + "/velocity.toml"))
                .getString("forwarding-secret"));
        properties.put("velocity-modern", ""+true);
        properties.put("server-port", ""+port);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dir + "/server.properties"))) {
            properties.store(writer, null);
        }

        registeredServer = Main.INSTANCE.server.registerServer(
                new ServerInfo("velocity_auth_limbo", new InetSocketAddress("127.0.0.1", port)));

        process = new ProcessBuilder()
                .directory(dir)
                .command("java", "-jar", jar.getAbsolutePath(), "--nogui")
                .start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (process != null && process.isAlive())
                process.destroy();
        }));

    }

    private int findFreePort() {
        int start = 30000;
        int end = 65000;
        for (int port = start; port < end; port++) {
            try {
                new ServerSocket(port).close();
                return port;
            } catch (IOException ignored) {
            }
        }
        return end;
    }
}
