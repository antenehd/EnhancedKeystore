package fi.aalto.mss.enhancedkeystore;

import android.content.Context;
import android.util.Log;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.OTHelper;
import fi.aalto.ssg.opentee.exception.CommunicationErrorException;
import fi.aalto.ssg.opentee.exception.TEEClientException;
import fi.aalto.ssg.opentee.imps.OpenTEE;
import fi.aalto.mss.enhancedkeystore.TAServiceUtils.*;

/**
 * Created by max on 17.03.17.
 */

public class TAService {

    public static String TAG = TAService.class.getSimpleName();
    private static String ENHANCED_KEY_STORE_TA = "libeks_ta.so";


    public static synchronized boolean initEnhancedKeystoreTA(Context context, WorkerCallback callback) {

        if (context == null || callback == null) return false;

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

        ITEEClient.ISession session = null;
        try {
            session = ctx.openSession(TAServiceUtils.getUUID(),
                    ITEEClient.IContext.ConnectionMethod.LoginPublic,
                    null,
                    null);
        } catch (TEEClientException e) {

            try {
                ctx.finalizeContext();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
                return false;
            }

            e.printStackTrace();
        }

        callback.updateContext(ctx);
        callback.updateSession(session);
        callback.updateClient(client);

        return true;
    }

    public static DHGenParameters initDHExchange(ITEEClient client,  ITEEClient.ISession session) {
        if (session == null || client == null) return null;

        int CMD_INIT_DH = 0x00000001;

        final ITEEClient.IValue.Flag param_flag = ITEEClient.IValue.Flag.TEEC_VALUE_OUTPUT;

        ITEEClient.IValue dhSpecs = client.Value(param_flag, 0, 0);






        DHGenParameters params = null;



        return params;
    }

    public static synchronized void finalizeEnhancedKeystoreTA (ITEEClient.IContext ctx, ITEEClient.ISession session){
        if(session != null){
            try {
                session.closeSession();
            } catch (TEEClientException e) {
                e.printStackTrace();
            }
        }

        if(ctx != null){
            try {
                ctx.finalizeContext();
            } catch (TEEClientException e) {
                e.printStackTrace();
            }
        }
    }
}
