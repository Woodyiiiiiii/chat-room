package connector;

import java.io.File;

/**
 * 存储关于connector组件的常用静态变量
 */
public class ConnectorUtils {

    public static final String WEB_ROOT =
            System.getProperty("user.dir") + File.separator  + "webroot";

    public static final String PROTOCOL = "HTTP/1.1";

    public static final String CARRIAGE = "\r";

    public static final String NEWLINE = "\n";

    public static final String SPACE = " ";

    public static String renderStatus(HttpStatus status) {
        StringBuilder s = new StringBuilder(PROTOCOL)
                .append(SPACE)
                .append(status.getStatusCode())
                .append(SPACE)
                .append(status.getReason())
                .append(CARRIAGE).append(NEWLINE)
                .append(CARRIAGE).append(NEWLINE);

        return s.toString();
    }

}
