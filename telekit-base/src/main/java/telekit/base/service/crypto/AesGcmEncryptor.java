package telekit.base.service.crypto;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class AesGcmEncryptor extends AbstractEncryptor {

    /**
     * The AES-GCM specification recommends 96, 104, 112, 120 or 128 tag length,
     * although 32 or 64 bits may be acceptable in some applications.
     */
    public static final int GCM_TAG_LENGTH = 16 * 8; // 128 bits

    public AesGcmEncryptor() {
        super(Algorithm.AES_GCM, Algorithm.AES_GCM.getNonceLength());
    }

    @Override
    protected Cipher getCipher(int mode, Key key, byte[] nonce) throws
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            NoSuchPaddingException,
            NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(alg.getTransformation());
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
        cipher.init(mode, key, spec);
        return cipher;
    }
}
