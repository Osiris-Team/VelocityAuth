package com.osiris.velocityauth;

import com.osiris.dyml.Yaml;
import com.osiris.dyml.YamlSection;
import com.osiris.dyml.exceptions.*;
import com.osiris.velocityauth.database.Database;

import java.io.File;
import java.io.IOException;

public class Config extends Yaml {
    public YamlSection databaseRawUrl;
    public YamlSection databaseUrl;
    public YamlSection databaseUsername;
    public YamlSection databasePassword;
    public YamlSection whitelistMode;
    public YamlSection sessionMaxHours;
    public YamlSection debugAuthServerName;

    public Config() throws YamlReaderException, YamlWriterException, IOException, DuplicateKeyException, IllegalListException, NotLoadedException, IllegalKeyException {
        super(new File(Main.INSTANCE.dataDirectory + "/config.yml"));
        this.load();
        databaseRawUrl = this.put("database", "raw-url").setDefValues(Database.rawUrl);
        databaseUrl = this.put("database", "url").setDefValues(Database.url);
        databaseUsername = this.put("database", "username");
        databasePassword = this.put("database", "password");

        whitelistMode = this.put("whitelist-mode").setCountTopLineBreaks(1)
                .setDefValues("false")
                .setComments("If true, not registered players will be blocked from joining the server (proxy).",
                        "Note that in this case your players will have to register themselves",
                        "over another platform, like a website for example.",
                        "If you proxy is in offline mode people are able to bypass this by naming themselves like registered users.");

        this.put("session").setCountTopLineBreaks(1).setComments("A session is created at successful player login and linked to the players ip.",
                "Players won't have to re-login every time they join,",
                " but only when their ip changes, or the session expires.");
        sessionMaxHours = this.put("session", "max-hours-valid").setDefValues("720").setComments("The maximum time (hours) a session is valid.",
                "Default is one month (30 days * 24h = 720h).");

        this.put("debug").setCountTopLineBreaks(1).setComments("Options useful for debugging stuff.",
                "Could disappear in future releases without notice.",
                "Changing any of this values not advised.");
        debugAuthServerName = this.put("debug", "auth-server-name").setComments("When null, default pre-installed limbo server will",
        "be started and used. Otherwise the provided server will be used as auth-server.");
        this.save();
    }
}
