package adris.altoclef.webserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class WebConfig {
    private static final String CONFIG_FILE = "altoclef/web_config.json";
    private static final ObjectMapper mapper = new ObjectMapper();
    
    private String host = "127.0.0.1";
    private int port = 25580;

    public static WebConfig load() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            WebConfig defaultConfig = new WebConfig();
            defaultConfig.save();
            return defaultConfig;
        }

        try {
            return mapper.readValue(configFile, WebConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new WebConfig();
        }
    }

    public void save() {
        try {
            File configFile = new File(CONFIG_FILE);
            configFile.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getters and setters
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
} 