package fi.aalto.mss.enhancedkeystore;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ExchangeActivity extends AppCompatActivity {

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
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Toast message = Toast.makeText(ExchangeActivity.this, readMessage, Toast.LENGTH_SHORT);
                    message.setGravity(Gravity.CENTER, 0, 0);
                    message.show();
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
            }
        }
    };

    private String mDeviceAddress;
    private String mDeviceName;
    private BluetoothService mBluetoothService;
    private BluetoothAdapter mBluetoothAdapter;

    @BindView(R.id.bt_status_value_tv) TextView mStatusTextView;
    @BindView(R.id.bt_connect_btn) Button mConnectButton;
    @BindView(R.id.bt_init_exchange_btn) Button mExchangeInitButton;

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
                initKeyExchange();
            }
        });
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
        mBluetoothService.write("Hi there!".getBytes());
    }

}
