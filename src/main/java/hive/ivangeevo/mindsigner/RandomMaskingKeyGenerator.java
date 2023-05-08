package hive.ivangeevo.mindsigner;

import org.glassfish.tyrus.core.MaskingKeyGenerator;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

public class RandomMaskingKeyGenerator implements MaskingKeyGenerator {

    private static final int MASKING_KEY_LENGTH = 4;
    private final SecureRandom secureRandom = new SecureRandom();

    public byte[] generateMaskingKey(ByteBuffer buffer) {
        byte[] maskingKey = new byte[MASKING_KEY_LENGTH];
        secureRandom.nextBytes(maskingKey);
        return maskingKey;
    }
    @Override
    public int nextInt() {
        return secureRandom.nextInt();
    }
}
