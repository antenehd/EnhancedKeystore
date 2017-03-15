package fi.aalto.mss.enhancedkeystore;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.paired_devices) ListView pairedDevicesListView;

    BluetoothAdapter mBluetoothAdapter;

    private static int REQUEST_ENABLE_BT = 42;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String EXTRA_DEVICE_NAME = "device_name";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported by this device", Toast.LENGTH_LONG);
            this.finish();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                fillListView();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==REQUEST_ENABLE_BT) {
            if(resultCode==RESULT_OK) {
                fillListView();
            } else {
                Toast.makeText(this, "Please enable Bluetooth to use this application", Toast.LENGTH_LONG);
                this.finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void fillListView() {

        ArrayAdapter<String> pairedDevicesArrayAdapter =
                new ArrayAdapter<String>(this, R.layout.paired_device);
        // Find and set up the ListView for paired devices
        pairedDevicesListView.setAdapter(pairedDevicesArrayAdapter);
        pairedDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the device MAC address, which is the last 17 chars in the View
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);
                String name = info.substring(0, info.length() - 17);

                startExchangeActivity(address, name);
            }
        });

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.no_paired_devies).toString();
            pairedDevicesArrayAdapter.add(noDevices);
        }
    }

    private void startExchangeActivity(String address, String name) {
        Intent intent = new Intent(this, ExchangeActivity.class);
        intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
        intent.putExtra(EXTRA_DEVICE_NAME, name);
        startActivity(intent);
    }

}
