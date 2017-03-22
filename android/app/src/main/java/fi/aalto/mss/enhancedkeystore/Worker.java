/*
 * Copyright (c) 2016 Aalto University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.aalto.mss.enhancedkeystore;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.mss.enhancedkeystore.TAServiceUtils.DHGenParameters;

/**
 * Example code to deal with remote TEE service in a separate thread.
 */
public class Worker extends HandlerThread implements WorkerCallback {
    final int OMS_MAX_RSA_MODULO_SIZE = 128;

    final String TAG = "Worker";

    public final static int CMD_INIT = 1;
    public final static int CMD_INIT_DH = 2;
    public final static int CMD_RESPOND_DH = 3;
    public final static int CMD_FINALIZE_DH = 4;
    public final static int CMD_DO_ENC = 5;
    public final static int CMD_DO_DEC = 6;
    public final static int CMD_FINALIZE = 7;

    Handler mUiHandler;
    Context mContext;
    WorkerCallback mCallback = null;

    ITEEClient client = null;
    ITEEClient.IContext ctx = null;
    ITEEClient.ISession session = null;

    private final Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.d(TAG, "handleMsg in " + currentThread().getId());

            if(Looper.getMainLooper() == Looper.myLooper()){
                Log.e(TAG, "within the main thread");
            }
            Log.d(TAG, "handleMessage: " + msg.what);
            switch (msg.what){

                case CMD_INIT:
                    Log.d(TAG, "handleMessage: asked to initialize TA");

                    boolean status_init = TAService.initEnhancedKeystoreTA(mContext, mCallback);

//                    Message uiMsg_init = mUiHandler.obtainMessage(MainActivity.CMD_UPDATE_LOGVIEW,
//                            MainActivity.ID_INI_BUTTON,
//                            status_init? 1: 0,
//                            status_init? (mLineNum++) + ")INFO: Environment initialized.\n" : " fail to initialize\n");
//
//                    mUiHandler.sendMessage(uiMsg_init);
                    break;

                case CMD_INIT_DH:
                    Log.d(TAG, "handleMessage: asked to initialize DH exchange");

                    DHGenParameters params = TAService.initDHExchange(client, session);

                    break;

                case CMD_FINALIZE:
                    Log.d(TAG, "handleMessage: asked to finalize");

                    TAService.finalizeEnhancedKeystoreTA(ctx, session);
//
//                    Message uiMsg_finalize = mUiHandler.obtainMessage(MainActivity.CMD_UPDATE_LOGVIEW,
//                            MainActivity.ID_FINALIZE_BUTTON,
//                            1,
//                            (mLineNum++) + ")INFO: Environment finalized.\n");
//
//                    mUiHandler.sendMessage(uiMsg_finalize);
                    break;

                case CMD_DO_ENC:
//                    boolean status_do_enc = true;
//
//                    try {
//                        data = Omnishare.doCrypto(client, ctx, session,
//                                Omnishare.CRYPTO_OP.CRYPTO_ENC_FILE,
//                                keychain.toByteArray(),
//                                keychain.getKeyCount(),
//                                keychain.getKeySize(),
//                                data);
//                    } catch (BadParametersException e) {
//                        e.printStackTrace();
//                        status_do_enc = false;
//                    } catch (BadFormatException e) {
//                        e.printStackTrace();
//                        status_do_enc = false;
//                    }
//
//                    Message uiMsg_doEnc = mUiHandler.obtainMessage(MainActivity.CMD_UPDATE_LOGVIEW,
//                            MainActivity.ID_DO_ENCRY_BUTTON,
//                            status_do_enc? 1 : 0,
//                            status_do_enc?
//                                    (mLineNum++) + ")INFO: Encryption Complete, Key Count: " + keychain.getKeyCount() + " Key Size:" + keychain.getKeySize() + "\n" +  "Encrypted buffer: " + HexUtils.encodeHexString(data) + "\n":
//                                    "ERROR: File Encryption Failed\n\n");
//
//                    mUiHandler.sendMessage(uiMsg_doEnc);

                    break;

                case CMD_DO_DEC:
//                    boolean status_do_dec = true;
//
//                    try {
//                        data = Omnishare.doCrypto(client, ctx, session,
//                                Omnishare.CRYPTO_OP.CRYPTO_DEC_FILE,
//                                keychain.toByteArray(),
//                                keychain.getKeyCount(),
//                                keychain.getKeySize(),
//                                data);
//                    } catch (BadParametersException e) {
//                        e.printStackTrace();
//                        status_do_dec = false;
//                    } catch (BadFormatException e) {
//                        e.printStackTrace();
//                        status_do_dec = false;
//                    }
//
//                    Message uiMsg_doDec = mUiHandler.obtainMessage(MainActivity.CMD_UPDATE_LOGVIEW,
//                            MainActivity.ID_DO_DECRY_BUTTON,
//                            status_do_dec? 1:0,
//                            status_do_dec? (mLineNum++) + ")INFO: Decryption Complete, Key Count: " + keychain.getKeyCount() + " Key Size:" + keychain.getKeySize() + "\n" +  "Decrypted buffer: \n" + HexUtils.encodeHexString(data) + "\n":
//                                    "ERROR: File Encryption Failed\n\n");
//                    mUiHandler.sendMessage(uiMsg_doDec);

                    break;

                default:
                    Log.e(TAG, "unknown message type or unhandled message");
                    break;
            }
            return true;
        }
    };

    Handler mHandler;

    public Worker(String name, Handler uiHandler, Context context) {
        super(name);
        this.mUiHandler = uiHandler;
        this.mContext = context;
        this.mCallback = this;
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        Looper looper = getLooper();
        mHandler = new Handler(looper, this.callback);
    }

    public Handler getHandler(){
        return this.mHandler;
    }

    @Override
    public void updateClient(ITEEClient client) {
        this.client = client;
    }

    @Override
    public void updateContext(ITEEClient.IContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void updateSession(ITEEClient.ISession session) {
        this.session = session;
    }
}
