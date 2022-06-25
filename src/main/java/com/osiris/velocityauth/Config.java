package com.osiris.velocityauth;

import com.osiris.dyml.Yaml;
import com.osiris.dyml.YamlSection;
import com.osiris.dyml.exceptions.*;

import java.io.File;
import java.io.IOException;

public class Config extends Yaml {
    public YamlSection databaseUsername;
    public YamlSection databasePassword;
    public YamlSection whitelistMode;

    public Config() throws YamlReaderException, YamlWriterException, IOException, DuplicateKeyException, IllegalListException, NotLoadedException, IllegalKeyException {
        super(new File(Main.INSTANCE.dataDirectory+"/config.yml"));
        this.load();
        databaseUsername = this.put("database", "username");
        databasePassword = this.put("database", "password");
        whitelistMode = this.put("whitelist-mode").setCountTopLineBreaks(1)
                .setDefValues("false")
                .setComments("If true, not registered players will be blocked from joining the server (proxy).",
                        "Note that in this case your players will have to register themselves",
                        "over another platform, like a website for example.");
        this.save();
    }
}
