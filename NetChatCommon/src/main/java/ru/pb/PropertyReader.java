package ru.pb;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {
    private static PropertyReader instance;
    private int port;
    private String host;
    private String dbConnectionString;
    private int historySize;
    private String historyPath;

    public String getHistoryPath() {
        return historyPath;
    }

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
//        var propFileName = "./config/application.properties";
        var propFileName = "application.properties";
//        try (InputStream inputStream = new FileInputStream(propFileName)) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName)) {
            var properties = new Properties();
            properties.load(inputStream);
            port = Integer.parseInt(properties.getProperty("server.port"));
            host = (properties.getProperty("server.host"));
            dbConnectionString = (properties.getProperty("datasource.url"));
            historySize = Integer.parseInt(properties.getProperty("history.size"));
            historyPath= (properties.getProperty("history.path"));
        } catch (Exception e) {
            System.out.println("Не удалось считать настройки: " +e.getMessage());

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

    public int getHistorySize(){
        return historySize;
    }
}