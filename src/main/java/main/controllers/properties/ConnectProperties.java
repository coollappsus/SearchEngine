package main.controllers.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("connect")
public class ConnectProperties {

    private static String userAgent;
    private static String refferer;
    private static String pathToWeb;

    public static String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        ConnectProperties.userAgent = userAgent;
    }

    public static String getRefferer() {
        return refferer;
    }

    public void setRefferer(String refferer) {
        ConnectProperties.refferer = refferer;
    }

    public static String getPathToWeb() {
        return pathToWeb;
    }

    public void setPathToWeb(String pathToWeb) {
        ConnectProperties.pathToWeb = pathToWeb;
    }
}
