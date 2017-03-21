package fi.aalto.mss.enhancedkeystore;

import android.content.Context;
import android.util.Log;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.OTHelper;
import fi.aalto.ssg.opentee.exception.CommunicationErrorException;
import fi.aalto.ssg.opentee.exception.TEEClientException;
import fi.aalto.ssg.opentee.imps.OpenTEE;

/**
 * Created by max on 17.03.17.
 */

public class TAService {

    public static String TAG = TAService.class.getSimpleName();
    private static String ENHANCED_KEY_STORE_TA = "libeks_ta.so";


    public static synchronized void initTA(Context context) {

        ITEEClient client = OpenTEE.newTEEClient();
        ITEEClient.IContext ctx = null;

        try {
            ctx = client.initializeContext(null, context);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        OTHelper utils = (OTHelper) ctx;

        try {
            if( !utils.installTA(ENHANCED_KEY_STORE_TA) ){
                Log.e(TAG, "Install " + ENHANCED_KEY_STORE_TA + " failed.");
            }
        } catch (CommunicationErrorException e) {
            e.printStackTrace();
        }
    }
}
