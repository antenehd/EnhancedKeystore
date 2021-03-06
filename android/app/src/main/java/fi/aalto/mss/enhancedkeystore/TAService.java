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
    private static final int INIT_VECTOR_BUFFER_SIZE = 16;
    private static final int KEY_HASH_BUFFER_SIZE = 16;


    public static synchronized boolean initEnhancedKeystoreTA(Context context, WorkerCallback callback) {

        if (context == null || callback == null) return false;

        ITEEClient client = OpenTEE.newTEEClient();
        ITEEClient.IContext ctx = null;

        try {
            Log.d(TAG, "TaService: init context");
            ctx = client.initializeContext(null, context);

        } catch (TEEClientException e) {
            Log.d(TAG, "TaService: init context failed");
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
            Log.d(TAG, "TaService: openseession");
            session = ctx.openSession(TAServiceUtils.getUUID(),
                    ITEEClient.IContext.ConnectionMethod.LoginPublic,
                    null,
                    null);
        } catch (TEEClientException e) {
            Log.d(TAG, "TaService: opensession failed");
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

    public static synchronized byte[] encryptData(ITEEClient client, ITEEClient.IContext context, ITEEClient.ISession session, byte[] keyID) {
        if (session == null || client == null || context == null || keyID == null) return null;

        byte[] plain_buffer = "fi.aalto.mss.enhancedkeystore".getBytes();
        Log.d(TAG, "encryptData: " + plain_buffer + "   length: " + plain_buffer.length );
        byte[] cipher_buffer = new byte[plain_buffer.length];
        byte[] initVector_buffer = new byte[INIT_VECTOR_BUFFER_SIZE];

        int CMD_ENCRYPT_AES = 0x00000004;
        final int input_sm_flag = ITEEClient.ISharedMemory.TEEC_MEM_INPUT;
        final ITEEClient.IRegisteredMemoryReference.Flag input_rmr_flag = ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_INPUT;
        final int output_sm_flag = ITEEClient.ISharedMemory.TEEC_MEM_OUTPUT;
        final ITEEClient.IRegisteredMemoryReference.Flag output_rmr_flag = ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_OUTPUT;
        ITEEClient.ISharedMemory plain_sm = null;
        ITEEClient.IRegisteredMemoryReference plain_rmr = null;
        ITEEClient.ISharedMemory cipher_sm = null;
        ITEEClient.IRegisteredMemoryReference cipher_rmr = null;
        ITEEClient.ISharedMemory initVector_sm = null;
        ITEEClient.IRegisteredMemoryReference initVector_rmr = null;
        ITEEClient.ISharedMemory keyID_sm = null;
        ITEEClient.IRegisteredMemoryReference keyID_rmr = null;

        try {
            plain_sm = context.registerSharedMemory(plain_buffer, input_sm_flag);
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
            plain_rmr = client.RegisteredMemoryReference(plain_sm, input_rmr_flag, 0);
        } catch (BadParametersException e) {
            e.printStackTrace();
        }

        try {
            cipher_sm = context.registerSharedMemory(cipher_buffer, output_sm_flag);
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
            cipher_rmr = client.RegisteredMemoryReference(cipher_sm, output_rmr_flag, 0);
        } catch (BadParametersException e) {
            e.printStackTrace();
        }

        try {
            initVector_sm = context.registerSharedMemory(initVector_buffer, output_sm_flag);
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
            initVector_rmr = client.RegisteredMemoryReference(initVector_sm, output_rmr_flag, 0);
        } catch (BadParametersException e) {
            e.printStackTrace();
        }

        try {
            keyID_sm = context.registerSharedMemory(keyID, input_sm_flag);
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
            keyID_rmr = client.RegisteredMemoryReference(keyID_sm, input_rmr_flag, 0);
        } catch (BadParametersException e) {
            e.printStackTrace();
        }

        ITEEClient.IOperation operation = client.Operation(plain_rmr, cipher_rmr, initVector_rmr, keyID_rmr);

        try {
            session.invokeCommand(CMD_ENCRYPT_AES, operation);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        byte[] cipher = null;
        byte[] initVector = null;

        if(cipher_buffer.length < cipher_rmr.getReturnSize()){
            Log.e(TAG, "encryptData: incorrect size of returned shared memory cipher");
        }else{
            cipher = Arrays.copyOf(cipher_buffer, cipher_rmr.getReturnSize());
        }

        if(initVector_buffer.length < initVector_rmr.getReturnSize()){
            Log.e(TAG, "encryptData: incorrect size of returned shared memory initVector");
        }else{
            initVector = Arrays.copyOf(initVector_buffer, initVector_rmr.getReturnSize());
        }

        if (cipher == null || initVector == null) {
            Log.e(TAG, "encryptData: could not get results");
        }

        try {
            context.releaseSharedMemory(plain_sm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            context.releaseSharedMemory(cipher_sm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            context.releaseSharedMemory(initVector_sm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            context.releaseSharedMemory(keyID_sm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        return cipher;
    }

    public static synchronized  byte[] getKeyHash(ITEEClient client, ITEEClient.IContext context, ITEEClient.ISession session, byte[] keyID) {

        byte[] keyHash_buffer = new byte[KEY_HASH_BUFFER_SIZE];

        int CMD_HASH_OF_KEY = 0x00000006;
        final int input_sm_flag = ITEEClient.ISharedMemory.TEEC_MEM_INPUT;
        final ITEEClient.IRegisteredMemoryReference.Flag input_rmr_flag = ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_INPUT;
        final int output_sm_flag = ITEEClient.ISharedMemory.TEEC_MEM_OUTPUT;
        final ITEEClient.IRegisteredMemoryReference.Flag output_rmr_flag = ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_OUTPUT;
        ITEEClient.ISharedMemory keyID_sm = null;
        ITEEClient.IRegisteredMemoryReference keyID_rmr = null;
        ITEEClient.ISharedMemory keyHash_sm = null;
        ITEEClient.IRegisteredMemoryReference keyHash_rmr = null;

        try {
            keyID_sm = context.registerSharedMemory(keyID, input_sm_flag);
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
            keyID_rmr = client.RegisteredMemoryReference(keyID_sm, input_rmr_flag, 0);
        } catch (BadParametersException e) {
            e.printStackTrace();
        }

        try {
            keyHash_sm = context.registerSharedMemory(keyHash_buffer, output_sm_flag);
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
            keyHash_rmr = client.RegisteredMemoryReference(keyHash_sm, output_rmr_flag, 0);
        } catch (BadParametersException e) {
            e.printStackTrace();
        }

        ITEEClient.IOperation operation = client.Operation(keyID_rmr, keyHash_rmr);

        try {
            session.invokeCommand(CMD_HASH_OF_KEY, operation);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        byte[] keyHash = null;

        if(keyHash_buffer.length < keyHash_rmr.getReturnSize()){
            Log.e(TAG, "getKeyHash: incorrect size of returned shared memory keyHash");
        }else{
            keyHash = Arrays.copyOf(keyHash_buffer, keyHash_rmr.getReturnSize());
        }

        try {
            context.releaseSharedMemory(keyID_sm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            context.releaseSharedMemory(keyHash_sm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        return keyHash;
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
