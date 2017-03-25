package fi.aalto.mss.enhancedkeystore;

/**
 * Created by max on 14.03.17.
 */

/**
 * Defines several constants.
 */
public interface Constants {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Key names received from Worker
    public static final String DH_PARAMS = "dh_params";
    public static final String DH_RESULT = "dh_result";
    public static final String CIPHER_TEXT = "cipher_text";
    public static final String KEY_HANDLE = "key_handle";
    public static final String KEY_HASH = "key_hash";

    // Message types for key exchange messages
    public static final int INIT_MESSAGE = 11;
    public static final int RESPOND_MESSAGE = 12;
    public static final int COMPLETE_MESSAGE = 13;
    public static final int RESULT_SUCCESS = 14;
    public static final int RESULT_FAILURE = 15;
    public static final int MESSAGE_KEY_CONFIRMATION = 16;

}
