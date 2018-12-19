package dns;

import javax.crypto.SecretKey;

public class QuickSecurity
        extends BaseSecurity {
    public static short getCryptoScheme() {
        return 1;
    }

    @Override
    protected byte[] do_encrypt(SecretKey key, byte[] plaintext) {
        return plaintext;
    }

    @Override
    protected byte[] do_decrypt(SecretKey key, byte[] encrypted) {
        return encrypted;
    }
}

