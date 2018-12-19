package phish;

import common.CommonUtils;
import common.RegexParser;

import java.util.Map;

public class PhishingUtils {
    public static String updateMessage(String message, Map options, String url, String token) {
        for (Object o : options.entrySet()) {
            Map.Entry next = (Map.Entry) o;
            message = CommonUtils.strrep(message, "%" + next.getKey() + "%", next.getValue() + "");
        }
        message = CommonUtils.strrep(message, "%TOKEN%", token);
        if (!"".equals(url) && url.length() > 0) {
            url = CommonUtils.strrep(url, "%TOKEN%", token);
            message = CommonUtils.strrep(message, "%URL%", url);
            String newv = "$1\"" + url + "\"";
            String oldv = "(?is:(href=)[\"'].*?[\"'])";
            message = message.replaceAll(oldv, newv);
        }
        return message;
    }

    public static MailServer parseServerString(String server) {
        MailServer result = new MailServer();
        RegexParser parser = new RegexParser(server);
        if (parser.matches("(.*?):(.*?)@(.*)")) {
            result.username = parser.group(1);
            result.password = parser.group(2);
            parser.whittle(3);
        }
        if (parser.matches("(.*?),(\\d+)")) {
            result.delay = Integer.parseInt(parser.group(2));
            parser.whittle(1);
        } else {
            result.delay = 0;
        }
        result.ssl = parser.endsWith("-ssl");
        result.starttls = parser.endsWith("-starttls");
        if (parser.matches("(.*?):(.*)")) {
            result.lhost = parser.group(1);
            result.lport = Integer.parseInt(parser.group(2));
        } else {
            result.lhost = parser.getText();
            result.lport = 25;
        }
        return result;
    }
}

