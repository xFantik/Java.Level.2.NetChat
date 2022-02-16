package ru.pb;

import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {
    private static PropertyReader instance;
    private int port;
    private String host;
    private String dbConnectionString;

    private PropertyReader() {
        getPropValues();
    }

    public static PropertyReader getInstance() {
        if (instance == null) {
            instance = new PropertyReader();
        }
        return instance;
    }

    public void getPropValues() {
        var propFileName = "application.properties";
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName)) {
            var properties = new Properties();
            properties.load(inputStream);
            port = Integer.parseInt(properties.getProperty("server.port"));
            host = (properties.getProperty("server.host"));
            dbConnectionString = (properties.getProperty("datasource.url"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }
    public String  getDbConnectionName() {
        return dbConnectionString;
    }
}