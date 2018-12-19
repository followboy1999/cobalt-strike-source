package common;

import c2profile.Profile;
import net.jsign.DigestAlgorithm;
import net.jsign.PESigner;
import net.jsign.pe.PEFile;
import net.jsign.timestamp.TimestampingMode;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
import java.security.KeyStore;

public class CodeSigner implements Serializable {
    protected byte[] keystore;
    protected String password = null;
    protected String alias = null;
    protected String digest_algorithm = null;
    protected String program_name = null;
    protected String program_url = null;
    protected boolean timestamp = false;
    protected String timestamp_url = null;
    protected String timestamp_mode = null;

    public CodeSigner() {
        this.keystore = new byte[0];
    }

    protected String get(Profile profile, String option) {
        if (profile.hasString(option) && !"".equals(profile.getString(option))) {
            return profile.getString(option);
        }
        return null;
    }

    public CodeSigner(Profile profile) {
        if (!profile.isFile(".code-signer.keystore")) {
            this.keystore = new byte[0];
            return;
        }
        this.keystore = CommonUtils.readFile(profile.getString(".code-signer.keystore"));
        this.password = profile.getString(".code-signer.password");
        this.alias = profile.getString(".code-signer.alias");
        this.digest_algorithm = this.get(profile, ".code-signer.digest_algorithm");
        this.program_name = this.get(profile, ".code-signer.program_name");
        this.program_url = this.get(profile, ".code-signer.program_url");
        this.timestamp_url = this.get(profile, ".code-signer.timestamp_url");
        this.timestamp_mode = this.get(profile, ".code-signer.timestamp_mode");
        this.timestamp = profile.option(".code-signer.timestamp");
    }

    public boolean available() {
        return this.keystore.length > 0;
    }

    public byte[] sign(byte[] data) {
        if (!this.available()) {
            return data;
        }
        String file = CommonUtils.writeToTemp("signme", "exe", data);
        this.sign(new File(file));
        byte[] dataz = CommonUtils.readFile(file);
        new File(file).delete();
        return dataz;
    }

    public void sign(File file) {
        if (!this.available()) {
            return;
        }
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new ByteArrayInputStream(this.keystore), this.password.toCharArray());
            PESigner signer = new PESigner(ks, this.alias, this.password);
            signer.withTimestamping(this.timestamp);
            if (this.program_name != null) {
                signer.withProgramName(this.program_name);
            }
            if (this.program_url != null) {
                signer.withProgramURL(this.program_url);
            }
            if (this.timestamp_mode != null) {
                signer.withTimestampingMode(TimestampingMode.valueOf(this.timestamp_mode));
            }
            if (this.timestamp_url != null) {
                signer.withTimestampingAuthority(this.timestamp_url);
            }
            if (this.digest_algorithm != null) {
                signer.withDigestAlgorithm(DigestAlgorithm.valueOf(this.digest_algorithm));
            }
            signer.sign(new PEFile(file));
        } catch (Exception ex) {
            MudgeSanity.logException("Could not sign '" + file + "'", ex, false);
        }
    }
}

