package fi.aalto.mss.enhancedkeystore;

import android.util.Log;

import java.util.UUID;

/**
 * Created by max on 22.03.17.
 */

public class TAServiceUtils {
    private static final String TAG = TAServiceUtils.class.getSimpleName();
    private static final String ENHANCED_KEY_STORE_TA_UUID = "316cd877-ebae-59d2-6766-6cce17e1d471";


    public static UUID getUUID() {
        Log.d(TAG, "getUUID from String: " + UUID.fromString(ENHANCED_KEY_STORE_TA_UUID).toString());
        return UUID.fromString(ENHANCED_KEY_STORE_TA_UUID);
    }


    public class DHGenParameters {
        private int generator;
        private int prime;
        private int publicKey;

        public DHGenParameters(int generator, int prime, int publicKey) {
            this.generator = generator;
            this.prime = prime;
            this.publicKey = publicKey;
        }

        public int getGenerator() {
            return generator;
        }

        public int getPrime() {
            return prime;
        }

        public int getPublicKey() {
            return publicKey;
        }
    }
}
