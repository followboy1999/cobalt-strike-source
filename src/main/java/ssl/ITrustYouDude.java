package ssl;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class ITrustYouDude implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] ax509certificate, String authType) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] ax509certificate, String authType) {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}

