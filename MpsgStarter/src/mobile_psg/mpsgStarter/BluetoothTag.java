package mobile_psg.mpsgStarter;

import java.io.IOException;
import java.util.UUID;

import com.example.mobile_psg.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothTag extends Activity {
	private static CharSequence bluetoothToastText = "";
	private static int toastDuration = Toast.LENGTH_SHORT;
	private static int REQUEST_ENABLE_BT;
	private static Toast bluetoothToast = null;
	private ArrayAdapter<String> newDeviceListArrayAdapter;
	private TextView bluetoothTitleText;
	private ListView newDevicesList;
//	private final UUID my_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
	private BluetoothSocket socket;
	private BluetoothDevice connect_device = null;
	
	private static BluetoothAdapter bluetooth = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth);
		bluetooth = BluetoothAdapter.getDefaultAdapter();
		
		bluetoothTitleText = (TextView) findViewById(R.id.bluetoothTitleText);
		newDevicesList = (ListView) findViewById(R.id.new_bluetooth_devices);
		
		enableBluetooth( bluetooth );
	};
	
	private void enableBluetooth( BluetoothAdapter bluetooth ) {
		if( bluetooth == null ) {
			bluetoothToastText = "Bluetooth not supported on this device.";
			bluetoothToast = Toast.makeText(this, bluetoothToastText, toastDuration);
			bluetoothToast.show();
		}
		else {
			if( !bluetooth.isEnabled() ) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
			else {
				connectBluetoothTag( bluetooth );
			}
		}
    };
    
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
    	if( resultCode == RESULT_OK ) {
    		connectBluetoothTag( bluetooth );
    	}
    	else {
    		bluetoothToastText = "Bluetooth needs to be enabled.";
			bluetoothToast = Toast.makeText(this, bluetoothToastText, toastDuration);
			bluetoothToast.show();
    	}
    };
    
    private void connectBluetoothTag( BluetoothAdapter bluetooth) {
    	bluetoothToastText = "In connect bluetooth tag";
		bluetoothToast = Toast.makeText(this, bluetoothToastText, toastDuration);
		bluetoothToast.show();
		
		newDeviceListArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
	    newDevicesList.setAdapter(newDeviceListArrayAdapter);
	    
	    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	    filter.addAction(BluetoothDevice.ACTION_UUID);
        registerReceiver(mReceiver, filter);
        newDeviceListArrayAdapter.clear();
        bluetooth.startDiscovery();
        
        //new_devices_list click
        newDevicesList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                BluetoothTag.bluetooth.cancelDiscovery();
                final String info = ((TextView) arg1).getText().toString();

                //get the device address when click the device item
                String address = info.substring(info.length()-17);
                
            //connect the device when item is click
            if( BluetoothAdapter.checkBluetoothAddress(address) ) {
            	connect_device = BluetoothTag.bluetooth.getRemoteDevice(address);
            	
            	connect_device.fetchUuidsWithSdp();

            }
            else {
            	Log.d("BLUETOOTH", "Error in address [" + address + "]");
            }
            
            
	            

            }
        });
    };
    
    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
            	Log.d("BLUETOOTHACTION", "action : " + action);
                BluetoothDevice bdevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);              
                //short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);;
                if(bdevice.getBondState() != BluetoothDevice.BOND_BONDED)
                Log.d("BLUETOOTHACTION", "bdevice : " + bdevice.getName());
                newDeviceListArrayAdapter.add("\n" + bdevice.getName() + "\n" + bdevice.getAddress());
                newDeviceListArrayAdapter.notifyDataSetChanged();
            }
            
            if(BluetoothDevice.ACTION_UUID.equals(action)) {
            	Log.d("BLUETOOTH", "testing");
            	try {
            		socket = connect_device.createRfcommSocketToServiceRecord(connect_device.getUuids()[0].getUuid());
    	            socket.connect();
	            } catch (IOException e) {
	                // TODO Auto-generated catch block
	            	Log.d("BLUETOOTHACTION", "Connection failed for " + connect_device.getUuids()[0].getUuid());
	                e.printStackTrace();
	            }
            }
        }
    };

    protected void onDestroy() {

        super.onDestroy();
        if(bluetooth != null)
            bluetooth.cancelDiscovery();
        unregisterReceiver(mReceiver);
    }
}