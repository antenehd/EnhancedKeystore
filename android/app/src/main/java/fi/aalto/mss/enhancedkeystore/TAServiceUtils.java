package fi.aalto.mss.enhancedkeystore;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by max on 22.03.17.
 */

public class TAServiceUtils {
    private static final String TAG = TAServiceUtils.class.getSimpleName();


    public static UUID getUUID() {
        // Return UUID of EnhancedKeystore TA
        return new UUID(0x316cd877ebae59d2L, 0xa7666cce17e1d471L);
    }

    public static byte[] getUtilBuffer(int keyLength, int bufferLength) {
        byte[] util = new byte[bufferLength];
        // Fill first 4 bytes of util with keyLength
        return ByteBuffer.allocate(bufferLength).putInt(keyLength).array();
    }

}
