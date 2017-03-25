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
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Example code to deal with remote TEE service in a separate thread.
 */
public class Worker extends HandlerThread implements WorkerCallback {
    final int OMS_MAX_RSA_MODULO_SIZE = 128;

    final String TAG = "Worker";

    public final static int CMD_INIT = 1;
    public final static int CMD_INIT_DH = 2;
    public final static int CMD_RESPOND_DH = 3;
    public final static int CMD_COMPLETE_DH = 4;
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

                    break;

                case CMD_INIT_DH:
                    Log.d(TAG, "handleMessage: asked to initialize DH exchange");

                    DHGenParameters paramsOut = TAService.initDHExchange(client, ctx, session);
                    if (paramsOut!= null) {
                        Log.d(TAG, "handleMessage: successfully initiated exchange");
                        Message uiMsg = mUiHandler.obtainMessage(Constants.MESSAGE_WRITE);
                        Bundle bundle = new Bundle();
                        DHExchangeMessage initMessage = new DHExchangeMessage(Constants.INIT_MESSAGE, Constants.RESULT_SUCCESS, paramsOut);
                        bundle.putParcelable(Constants.DH_PARAMS, initMessage);
                        uiMsg.setData(bundle);
                        mUiHandler.sendMessage(uiMsg);
                    } else {
                        Log.d(TAG, "handleMessage: init exchange failed");
                        Message uiMsg = mUiHandler.obtainMessage(Constants.MESSAGE_TOAST);
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.TOAST, "Initialization failed");
                        uiMsg.setData(bundle);
                        mUiHandler.sendMessage(uiMsg);
                    }

                    break;

                case CMD_RESPOND_DH:
                    Log.d(TAG, "handleMessage: asked to respond to DH exchange");
                    DHGenParameters paramsIn = msg.getData().getParcelable(Constants.DH_PARAMS);
                    DHExchangeResult resultResp = TAService.respondDHExchange(client, ctx, session, paramsIn);
                    if (resultResp != null) {
                        Log.d(TAG, "handleMessage: successfully responded to init");
                        Message uiMsg = mUiHandler.obtainMessage(Constants.MESSAGE_WRITE);
                        Bundle bundle = new Bundle();
                        DHExchangeMessage responseMessage = new DHExchangeMessage(Constants.RESPOND_MESSAGE, Constants.RESULT_SUCCESS, resultResp.getDhParams());
                        bundle.putParcelable(Constants.DH_PARAMS, responseMessage);
                        bundle.putParcelable(Constants.DH_RESULT, resultResp);
                        uiMsg.setData(bundle);
                        mUiHandler.sendMessage(uiMsg);
                    } else {
                        Message uiMsg = mUiHandler.obtainMessage(Constants.MESSAGE_WRITE);
                        Bundle bundle = new Bundle();
                        DHExchangeMessage responseMessage = new DHExchangeMessage(Constants.RESPOND_MESSAGE, Constants.RESULT_FAILURE, paramsIn);
                        bundle.putParcelable(Constants.DH_PARAMS, responseMessage);
                        uiMsg.setData(bundle);
                        mUiHandler.sendMessage(uiMsg);
                    }

                    break;

                case CMD_COMPLETE_DH:
                    Log.d(TAG, "handleMessage: asked to complete DH exchange");
                    DHGenParameters paramsResp = msg.getData().getParcelable(Constants.DH_PARAMS);
                    DHExchangeResult resultCompl = TAService.completeDHExchange(client, ctx, session, paramsResp);
                    if (resultCompl != null) {
                        Log.d(TAG, "handleMessage: successfully completed exchange");
                        Message uiMsg = mUiHandler.obtainMessage(Constants.MESSAGE_WRITE);
                        Bundle bundle = new Bundle();
                        DHExchangeMessage completeMessage = new DHExchangeMessage(Constants.COMPLETE_MESSAGE, Constants.RESULT_SUCCESS, resultCompl.getDhParams());
                        bundle.putParcelable(Constants.DH_PARAMS, completeMessage);
                        bundle.putParcelable(Constants.DH_RESULT, resultCompl);
                        uiMsg.setData(bundle);
                        mUiHandler.sendMessage(uiMsg);
                    } else {
                        Message uiMsg = mUiHandler.obtainMessage(Constants.MESSAGE_WRITE);
                        Bundle bundle = new Bundle();
                        DHExchangeMessage completeMessage = new DHExchangeMessage(Constants.RESPOND_MESSAGE, Constants.RESULT_FAILURE, paramsResp);
                        bundle.putParcelable(Constants.DH_PARAMS, completeMessage);
                        uiMsg.setData(bundle);
                        mUiHandler.sendMessage(uiMsg);
                    }

                    break;


                case CMD_FINALIZE:
                    Log.d(TAG, "handleMessage: asked to finalize");

                    TAService.finalizeEnhancedKeystoreTA(ctx, session);

                    break;

                case CMD_DO_ENC:
                    Log.d(TAG, "handleMessage: asked to encrypt");

                    byte[] keyHandle = msg.getData().getByteArray(Constants.KEY_HANDLE);
                    byte[] cipherText = TAService.encryptData(client, ctx, session, keyHandle);

                    if (cipherText != null) {
                        Log.d(TAG, "handleMessage: successfully encrypted data");

                        Message uiMsg = mUiHandler.obtainMessage(Constants.MESSAGE_KEY_CONFIRMATION);
                        Bundle bundle = new Bundle();
                        bundle.putByteArray(Constants.CIPHER_TEXT, cipherText);
                        uiMsg.setData(bundle);
                        mUiHandler.sendMessage(uiMsg);
                    }

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
