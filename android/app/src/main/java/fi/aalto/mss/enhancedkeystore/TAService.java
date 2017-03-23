package fi.aalto.mss.enhancedkeystore;

import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.OTHelper;
import fi.aalto.ssg.opentee.exception.BadParametersException;
import fi.aalto.ssg.opentee.exception.CommunicationErrorException;
import fi.aalto.ssg.opentee.exception.TEEClientException;
import fi.aalto.ssg.opentee.imps.OpenTEE;

/**
 * Created by max on 17.03.17.
 */

public class TAService {

    public static final String TAG = TAService.class.getSimpleName();
    private static final String ENHANCED_KEY_STORE_TA = "libeks_ta.so";
    private static final int KEY_LENGTH = 256;
    private static final int G_BUFFER_SIZE = 4;
    private static final int PUBLIC_KEY_BUFFER_SIZE = 256;
    private static final int P_BUFFER_SIZE = 256;
    private static final int KEY_ID_BUFFER_SIZE = 8;


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

    public static DHGenParameters initDHExchange(ITEEClient client, ITEEClient.IContext context, ITEEClient.ISession session) {
        if (session == null || client == null || context == null) return null;

        DHGenParameters params = null;
        byte[] publicKey_buffer = new byte[PUBLIC_KEY_BUFFER_SIZE];
        byte[] g_buffer = new byte[G_BUFFER_SIZE];
        byte[] p_buffer = new byte[P_BUFFER_SIZE];

        int CMD_INIT_DH = 0x00000001;
        final int param_sm_flag = ITEEClient.ISharedMemory.TEEC_MEM_OUTPUT;
        final ITEEClient.IRegisteredMemoryReference.Flag param_rmr_flag = ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_OUTPUT;
        final ITEEClient.IValue.Flag param_value_flag = ITEEClient.IValue.Flag.TEEC_VALUE_INOUT;
        ITEEClient.ISharedMemory publicKey_sm = null;
        ITEEClient.IRegisteredMemoryReference publicKey_rmr = null;
        ITEEClient.ISharedMemory g_sm = null;
        ITEEClient.IRegisteredMemoryReference g_rmr = null;
        ITEEClient.ISharedMemory p_sm = null;
        ITEEClient.IRegisteredMemoryReference p_rmr = null;

        try {
            publicKey_sm = context.registerSharedMemory(publicKey_buffer, param_sm_flag);
        } catch (TEEClientException e) {

            try {
                session.closeSession();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }


            try {
                context.finalizeContext();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }

        }

        try {
            publicKey_rmr = client.RegisteredMemoryReference(publicKey_sm, param_rmr_flag, 0);
        } catch (BadParametersException e) {
            e.printStackTrace();
        }

        try {
            g_sm = context.registerSharedMemory(g_buffer, param_sm_flag);
        } catch (TEEClientException e) {

            try {
                session.closeSession();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }


            try {
                context.finalizeContext();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }

        }

        try {
            g_rmr = client.RegisteredMemoryReference(g_sm, param_rmr_flag, 0);
        } catch (BadParametersException e) {
            e.printStackTrace();
        }

        try {
            p_sm = context.registerSharedMemory(p_buffer, param_sm_flag);
        } catch (TEEClientException e) {

            try {
                session.closeSession();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }


            try {
                context.finalizeContext();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }

        }

        try {
            p_rmr = client.RegisteredMemoryReference(p_sm, param_rmr_flag, 0);
        } catch (BadParametersException e) {
            e.printStackTrace();
        }

        ITEEClient.IValue keyLen_val = client.Value(param_value_flag, 0, KEY_LENGTH);

        ITEEClient.IOperation operation = client.Operation(publicKey_rmr, g_rmr, p_rmr, keyLen_val);

        try {
            session.invokeCommand(CMD_INIT_DH, operation);
        } catch (TEEClientException e) {
            e.printStackTrace();
            return params;
        }

        byte[] publicKey = null;
        byte[] g = null;
        byte[] p = null;

        if(publicKey_buffer.length < publicKey_rmr.getReturnSize()){
            Log.e(TAG, "initDHExchange: incorrect size of returned shared memory public key");
        }else{
            publicKey = Arrays.copyOf(publicKey_buffer, publicKey_rmr.getReturnSize());
        }

        if(g_buffer.length < g_rmr.getReturnSize()){
            Log.e(TAG, "initDHExchange: incorrect size of returned shared memory g");
        }else{
            g = Arrays.copyOf(g_buffer, g_rmr.getReturnSize());
        }

        if(p_buffer.length < p_rmr.getReturnSize()){
            Log.e(TAG, "initDHExchange: incorrect size of returned shared memory p");
        }else{
            p = Arrays.copyOf(p_buffer, p_rmr.getReturnSize());
        }

        if (publicKey != null && g != null && p != null) {
            params = new DHGenParameters(g, p, publicKey);
        } else {
            Log.e(TAG, "initDHExchange: could not get parameters");
        }

        try {
            context.releaseSharedMemory(publicKey_sm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            context.releaseSharedMemory(g_sm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            context.releaseSharedMemory(p_sm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }


        return params;
    }

    public static synchronized DHExchangeResult respondDHExchange(ITEEClient client, ITEEClient.IContext context, ITEEClient.ISession session, DHGenParameters passedParams) {
        if (session == null || client == null || context == null || passedParams == null) return null;

        DHExchangeResult dhExchangeResult = null;
        byte[] publicKey_buffer = passedParams.getPublicKey();
        byte[] p_buffer = passedParams.getPrime();
        byte[] keyID_buffer = new byte[KEY_ID_BUFFER_SIZE];

        int CMD_RESPOND_DH = 0x00000002;
        final int input_sm_flag = ITEEClient.ISharedMemory.TEEC_MEM_INPUT;
        final ITEEClient.IRegisteredMemoryReference.Flag input_rmr_flag = ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_INPUT;
        final int output_sm_flag = ITEEClient.ISharedMemory.TEEC_MEM_OUTPUT;
        final ITEEClient.IRegisteredMemoryReference.Flag output_rmr_flag = ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_OUTPUT;
        final int inout_sm_flag = ITEEClient.ISharedMemory.TEEC_MEM_INPUT | ITEEClient.ISharedMemory.TEEC_MEM_OUTPUT;
        final ITEEClient.IRegisteredMemoryReference.Flag inout_rmr_flag = ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_INOUT;
        final ITEEClient.IValue.Flag input_val_flag = ITEEClient.IValue.Flag.TEEC_VALUE_INPUT;
        ITEEClient.ISharedMemory publicKey_sm = null;
        ITEEClient.IRegisteredMemoryReference publicKey_rmr = null;
        ITEEClient.ISharedMemory keyID_sm = null;
        ITEEClient.IRegisteredMemoryReference keyID_rmr = null;
        ITEEClient.ISharedMemory p_sm = null;
        ITEEClient.IRegisteredMemoryReference p_rmr = null;


        try {
            publicKey_sm = context.registerSharedMemory(publicKey_buffer, inout_sm_flag);
        } catch (TEEClientException e) {

            try {
                session.closeSession();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }


            try {
                context.finalizeContext();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }

        }

        try {
            publicKey_rmr = client.RegisteredMemoryReference(publicKey_sm, inout_rmr_flag, 0);
        } catch (BadParametersException e) {
            e.printStackTrace();
        }

        try {
            keyID_sm = context.registerSharedMemory(keyID_buffer, output_sm_flag);
        } catch (TEEClientException e) {

            try {
                session.closeSession();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }


            try {
                context.finalizeContext();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }

        }

        try {
            keyID_rmr = client.RegisteredMemoryReference(keyID_sm, output_rmr_flag, 0);
        } catch (BadParametersException e) {
            e.printStackTrace();
        }

        try {
            p_sm = context.registerSharedMemory(p_buffer, input_sm_flag);
        } catch (TEEClientException e) {

            try {
                session.closeSession();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }


            try {
                context.finalizeContext();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }

        }

        try {
            p_rmr = client.RegisteredMemoryReference(p_sm, input_rmr_flag, 0);
        } catch (BadParametersException e) {
            e.printStackTrace();
        }

        ITEEClient.IValue keyLen_g_val = client.Value(input_val_flag, KEY_LENGTH, passedParams.getGenerator());

        ITEEClient.IOperation operation = client.Operation(publicKey_rmr, keyID_rmr, p_rmr, keyLen_g_val);

        try {
            session.invokeCommand(CMD_RESPOND_DH, operation);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        byte[] publicKey = null;
        byte[] keyID = null;

        if(publicKey_buffer.length < publicKey_rmr.getReturnSize()){
            Log.e(TAG, "respondDHExchange: incorrect size of returned shared memory publicKey");
        }else{
            publicKey = Arrays.copyOf(publicKey_buffer, publicKey_rmr.getReturnSize());
        }

        if(keyID_buffer.length < keyID_rmr.getReturnSize()){
            Log.e(TAG, "respondDHExchange: incorrect size of returned shared memory util");
        }else{
            keyID = Arrays.copyOf(keyID_buffer, keyID_rmr.getReturnSize());
        }

        if (publicKey != null && keyID != null) {
            DHGenParameters params = new DHGenParameters(passedParams.getGenerator(), passedParams.getPrime(), publicKey);
            dhExchangeResult = new DHExchangeResult(keyID, params);
        } else {
            Log.e(TAG, "respondDHExchange: could not get parameters");
        }

        try {
            context.releaseSharedMemory(publicKey_sm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            context.releaseSharedMemory(p_sm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            context.releaseSharedMemory(keyID_sm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        return dhExchangeResult;
    }

    public static synchronized DHExchangeResult completeDHExchange(ITEEClient client, ITEEClient.IContext context, ITEEClient.ISession session, DHGenParameters passedParams) {
        if (session == null || client == null || context == null || passedParams == null) return null;

        DHExchangeResult dhExchangeResult = null;
        byte[] publicKey_buffer = passedParams.getPublicKey();
        byte[] keyID_buffer = new byte[KEY_ID_BUFFER_SIZE];

        int CMD_COMPLETE_DH = 0x00000003;
        final int input_sm_flag = ITEEClient.ISharedMemory.TEEC_MEM_INPUT;
        final ITEEClient.IRegisteredMemoryReference.Flag input_rmr_flag = ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_INPUT;
        final int output_sm_flag = ITEEClient.ISharedMemory.TEEC_MEM_OUTPUT;
        final ITEEClient.IRegisteredMemoryReference.Flag output_rmr_flag = ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_OUTPUT;
        ITEEClient.ISharedMemory publicKey_sm = null;
        ITEEClient.IRegisteredMemoryReference publicKey_rmr = null;
        ITEEClient.ISharedMemory keyID_sm = null;
        ITEEClient.IRegisteredMemoryReference keyID_rmr = null;

        try {
            publicKey_sm = context.registerSharedMemory(publicKey_buffer, input_sm_flag);
        } catch (TEEClientException e) {

            try {
                session.closeSession();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }


            try {
                context.finalizeContext();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }

        }

        try {
            publicKey_rmr = client.RegisteredMemoryReference(publicKey_sm, input_rmr_flag, 0);
        } catch (BadParametersException e) {
            e.printStackTrace();
        }

        try {
            keyID_sm = context.registerSharedMemory(keyID_buffer, output_sm_flag);
        } catch (TEEClientException e) {

            try {
                session.closeSession();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }


            try {
                context.finalizeContext();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }

        }

        try {
            keyID_rmr = client.RegisteredMemoryReference(keyID_sm, output_rmr_flag, 0);
        } catch (BadParametersException e) {
            e.printStackTrace();
        }

        ITEEClient.IOperation operation = client.Operation(publicKey_rmr, keyID_rmr);

        try {
            session.invokeCommand(CMD_COMPLETE_DH, operation);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        byte[] publicKey = null;
        byte[] keyID = null;

        if(publicKey_buffer.length < publicKey_rmr.getReturnSize()){
            Log.e(TAG, "completeDHExchange: incorrect size of returned shared memory publicKey");
        }else{
            publicKey = Arrays.copyOf(publicKey_buffer, publicKey_rmr.getReturnSize());
        }

        if(keyID_buffer.length < keyID_rmr.getReturnSize()){
            Log.e(TAG, "completeDHExchange: incorrect size of returned shared memory keyID");
        }else{
            keyID = Arrays.copyOf(keyID_buffer, keyID_rmr.getReturnSize());
        }

        if (publicKey != null && keyID != null) {
            DHGenParameters params = new DHGenParameters(passedParams.getGenerator(), passedParams.getPrime(), publicKey);
            dhExchangeResult = new DHExchangeResult(keyID, params);
        } else {
            Log.e(TAG, "respondDHExchange: could not get parameters");
        }

        try {
            context.releaseSharedMemory(publicKey_sm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            context.releaseSharedMemory(keyID_sm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        return dhExchangeResult;
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
