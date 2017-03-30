package fi.aalto.mss.enhancedkeystore;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.SerializationUtils;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ExchangeActivity extends AppCompatActivity {

    private static String TAG = ExchangeActivity.class.getSimpleName();

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mDeviceName));
                            mConnectButton.setEnabled(false);
                            mExchangeInitButton.setEnabled(true);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus(getString(R.string.title_connecting));
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setStatus(getString(R.string.title_not_connected));
                            mExchangeInitButton.setEnabled(false);
                            mConnectButton.setEnabled(true);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    DHExchangeMessage messageOut = msg.getData().getParcelable(Constants.DH_PARAMS);

                    if (messageOut.getResult() == Constants.RESULT_SUCCESS) {
                        if(messageOut.getType() == Constants.RESPOND_MESSAGE || messageOut.getType() == Constants.COMPLETE_MESSAGE) {
                            DHExchangeResult result = msg.getData().getParcelable(Constants.DH_RESULT);
                            keyHandle = result.getKeyID();
                            if (messageOut.getType() == Constants.COMPLETE_MESSAGE) {
                                doKeyConfirmation();
                                Toast.makeText(ExchangeActivity.this, "Key exchange successful", Toast.LENGTH_SHORT).show();
                                finalizeTASession();
                            }
                        }
                    } else {
                        Toast.makeText(ExchangeActivity.this, "Key exchange failed", Toast.LENGTH_SHORT).show();
                        finalizeTASession();
                    }

                    byte[] writeBuf = SerializationUtils.serialize(messageOut);
                    mBluetoothService.write(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    DHExchangeMessage messageIn = SerializationUtils.deserialize(Arrays.copyOfRange(readBuf, 0, msg.arg1));
                    if (messageIn.getResult() == Constants.RESULT_SUCCESS) {
                        Handler workerHandler = mWorker.getHandler();
                        Message workerMsg = null;
                        Bundle bundle = null;
                        switch (messageIn.getType()) {
                            case Constants.INIT_MESSAGE:
                                Log.d(TAG, "handleMessage: received init");
                                initTASession();
                                bundle = new Bundle();
                                bundle.putParcelable(Constants.DH_PARAMS, messageIn.getParams());
                                workerMsg = workerHandler.obtainMessage(Worker.CMD_RESPOND_DH);
                                workerMsg.setData(bundle);
                                workerHandler.sendMessage(workerMsg);

                                break;
                            case Constants.RESPOND_MESSAGE:
                                Log.d(TAG, "handleMessage: received respond");
                                bundle = new Bundle();
                                bundle.putParcelable(Constants.DH_PARAMS, messageIn.getParams());
                                workerMsg = workerHandler.obtainMessage(Worker.CMD_COMPLETE_DH);
                                workerMsg.setData(bundle);
                                workerHandler.sendMessage(workerMsg);

                                break;
                            case Constants.COMPLETE_MESSAGE:
                                Log.d(TAG, "handleMessage: received complete");
                                doKeyConfirmation();
                                Toast.makeText(ExchangeActivity.this, "Key exchange successful", Toast.LENGTH_SHORT).show();
                                finalizeTASession();
                                break;
                            default:
                                Log.e(TAG, "handleMessage: did not understand received DH message");
                                break;
                        }
                    } else {
                        Toast.makeText(ExchangeActivity.this, "Key exchange failed", Toast.LENGTH_SHORT).show();
                        finalizeTASession();
                    }


                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(ExchangeActivity.this, "Connected to "
                            + mDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(ExchangeActivity.this, msg.getData().getString(Constants.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;

                case Constants.MESSAGE_KEY_CONFIRMATION:
                    showKeyConfirmation(msg.getData().getByteArray(Constants.KEY_HASH));
                    break;
            }
        }
    };

    private String mDeviceAddress;
    private String mDeviceName;
    private BluetoothService mBluetoothService;
    private BluetoothAdapter mBluetoothAdapter;
    private Worker mWorker;
    private byte[] keyHandle;

    @BindView(R.id.bt_status_value_tv) TextView mStatusTextView;
    @BindView(R.id.bt_connect_btn) Button mConnectButton;
    @BindView(R.id.bt_init_exchange_btn) Button mExchangeInitButton;
    @BindView(R.id.tv_key_confirmation) TextView mKeyConfirmationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange);
        ButterKnife.bind(this);

        Intent passedIntent = getIntent();

        if (passedIntent != null) {
            mDeviceAddress = passedIntent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);
            mDeviceName = passedIntent.getStringExtra(MainActivity.EXTRA_DEVICE_NAME);
        }

        mBluetoothService = new BluetoothService(this, mHandler);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectDevice();
            }
        });

        mExchangeInitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKeyConfirmationTextView.setText("");
                initTASession();
                initKeyExchange();
            }
        });

        mWorker = new Worker("Enhanced Keystore Worker", mHandler, getApplicationContext());
        mWorker.start();
    }


    @Override
    protected void onResume() {
        Intent passedIntent = getIntent();

        if (passedIntent != null) {
            mDeviceAddress = passedIntent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);
            mDeviceName = passedIntent.getStringExtra(MainActivity.EXTRA_DEVICE_NAME);
        }

        if (mBluetoothService != null) {
            mBluetoothService.start();
        } else {
            mBluetoothService = new BluetoothService(this, mHandler);
            mBluetoothService.start();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mBluetoothService != null) {
            mBluetoothService.stop();
        }
        super.onPause();
    }

    private void setStatus(String status) {
        mStatusTextView.setText(status);
    }

    private void connectDevice() {
        BluetoothDevice pairedDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);

        mBluetoothService.connect(pairedDevice);
    }

    private void initKeyExchange() {
        Handler workerHandler = mWorker.getHandler();
        Message msg = workerHandler.obtainMessage(Worker.CMD_INIT_DH);
        workerHandler.sendMessage(msg);
    }

    private void initTASession() {
        Handler workerHandler = mWorker.getHandler();
        Message msg = workerHandler.obtainMessage(Worker.CMD_INIT);
        workerHandler.sendMessage(msg);
    }

    private void finalizeTASession() {
        Handler workerHandler = mWorker.getHandler();
        Message msg = workerHandler.obtainMessage(Worker.CMD_FINALIZE);
        workerHandler.sendMessage(msg);

    }

    private void doKeyConfirmation() {
        Handler workerHandler = mWorker.getHandler();
        Bundle bundle = new Bundle();
        bundle.putByteArray(Constants.KEY_HANDLE, keyHandle);
        Message workerMsg = workerHandler.obtainMessage(Worker.CMD_GET_KEY_HASH);
        workerMsg.setData(bundle);
        workerHandler.sendMessage(workerMsg);
    }

    private void showKeyConfirmation(byte[] cipher) {
        String confirmation = bytesToHex(cipher);

        mKeyConfirmationTextView.setText(confirmation);
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
