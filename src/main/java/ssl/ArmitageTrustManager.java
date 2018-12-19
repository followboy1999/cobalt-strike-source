package ssl;

import javax.net.ssl.X509TrustManager;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class ArmitageTrustManager implements X509TrustManager {
    protected ArmitageTrustListener checker;

    public ArmitageTrustManager(ArmitageTrustListener checker) {
        this.checker = checker;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] ax509certificate, String authType) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] ax509certificate, String authType) throws CertificateException {
        try {
            for (X509Certificate anAx509certificate : ax509certificate) {
                byte[] bytesOfMessage = anAx509certificate.getEncoded();
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] thedigest = md.digest(bytesOfMessage);
                BigInteger bi = new BigInteger(1, thedigest);
                String fingerprint = bi.toString(16);
                if (this.checker == null || this.checker.trust(fingerprint)) continue;
                throw new CertificateException("Certificate Rejected. Press Cancel.");
            }
            return;
        } catch (CertificateException cex) {
            throw cex;
        } catch (Exception ex) {
            throw new CertificateException(ex.getMessage());
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}

