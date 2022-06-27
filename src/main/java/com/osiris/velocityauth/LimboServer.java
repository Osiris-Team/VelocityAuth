package com.osiris.velocityauth;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.lingala.zip4j.ZipFile;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    public Thread errorReaderThread;

    public void start() throws IOException, URISyntaxException {
        port = findFreePort();
        pluginJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI());
        Objects.requireNonNull(pluginJar);

        // Unpack limbo server stuff from jar
        if (!dir.exists() || dir.listFiles() == null || dir.listFiles().length == 0) {
            try (ZipFile zip = new ZipFile(pluginJar)) {
                zip.extractFile("limbo-server/", pluginDir.getAbsolutePath()); // plugindir/limbo-server
            }
            // Download limbo server jar
            try(ReadableByteChannel readableByteChannel = Channels.newChannel(new URL("" +
                    "https://ci.loohpjames.com/job/Limbo/lastSuccessfulBuild/artifact/target/Limbo-0.6.16-ALPHA-1.19.jar")
                    .openStream());){
                if(!jar.exists()) jar.createNewFile();
                try(FileChannel c = new FileOutputStream(jar).getChannel();){
                    c.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                }
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
        File velocityToml = new File(pluginJar.getParentFile().getParentFile() + "/velocity.toml");
        if (!velocityToml.exists())
            throw new FileNotFoundException("File does not exist or failed to find: "+velocityToml);
        String forwardingSecret = new Toml().read(velocityToml)
                .getString("forwarding-secret");
        if(forwardingSecret == null || forwardingSecret.trim().isEmpty()){
            String forwardingSecretFilePath = new Toml().read(velocityToml).getString("forwarding-secret-file");
            if(forwardingSecretFilePath == null || forwardingSecretFilePath.trim().isEmpty())
                throw new NullPointerException("The 'forwarding-secret' or 'forwarding-secret-file' fields cannot be null or empty! Checked config: "+velocityToml);
            File forwadingSecretFile = null;
            if(forwardingSecretFilePath.startsWith("/") || forwardingSecretFilePath.startsWith("\\"))
                forwadingSecretFile = new File(velocityToml.getParentFile()+forwardingSecretFilePath);
            else
                forwadingSecretFile = new File(velocityToml.getParentFile()+"/"+forwardingSecretFilePath);
            if(!forwadingSecretFile.exists())
                throw new FileNotFoundException("Failed to find file containing 'forwarding-secret'. Does not exist: "+forwadingSecretFile);
            forwardingSecret = new String(Files.readAllBytes(forwadingSecretFile.toPath()), StandardCharsets.UTF_8);
            if(forwardingSecret.trim().isEmpty())
                throw new NullPointerException("The 'forwarding-secret' or 'forwarding-secret-file' fields cannot be null or empty! Checked config: "+velocityToml);
        }
        properties.put("forwarding-secrets", forwardingSecret);
        properties.put("velocity-modern", ""+true);
        properties.put("server-port", ""+port);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dir + "/server.properties"))) {
            properties.store(writer, null);
        }

        registeredServer = Main.INSTANCE.server.registerServer(
                new ServerInfo("velocity_auth_limbo", new InetSocketAddress("127.0.0.1", port)));

        process = new ProcessBuilder()
                .directory(dir)
                .command("java","-Dorg.jline.terminal.dumb=true", "-jar", jar.getAbsolutePath(), "--nogui")
                .start();

        if(errorReaderThread != null) errorReaderThread.interrupt();
        errorReaderThread = new Thread(() -> {
           try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))){
               String line = null;
               while ((line = reader.readLine()) != null){
                   System.err.println("(error-limbo) "+line);
               }
           } catch (Exception e) {
               e.printStackTrace();
           }
        });
        errorReaderThread.start();
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
