package ru.robot_mitya.easybluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends Activity {
    private boolean mConnected;
    private BluetoothSocket mBluetoothSocket;
    private OutputStream mOutputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText editTextMac = (EditText) findViewById(R.id.edit_text_mac);
        final Button buttonConnect = (Button) findViewById(R.id.button_connect);
        final Button buttonDisconnect = (Button) findViewById(R.id.button_disconnect);
        final CheckBox checkBoxLED = (CheckBox) findViewById(R.id.checkbox_led);
        checkBoxLED.setVisibility(View.INVISIBLE); // will become visible after bluetooth connection

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    String macAddress = "";
                    if (editTextMac != null) {
                        macAddress = String.valueOf(editTextMac.getText());
                    }
                    if (connect(macAddress)) {
                        checkBoxLED.setVisibility(View.VISIBLE);
                        showToast("Connected");
                    }
                } catch (final Exception e) {
                    showToast(e.getMessage());
                }
            }
        });

        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                disconnect();
                checkBoxLED.setVisibility(View.INVISIBLE);
                showToast("Disconnected");
            }
        });

        checkBoxLED.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    if (isChecked) {
                        send("1");
                        showToast("LED is turned on");
                    } else {
                        send("0");
                        showToast("LED is turned off");
                    }
                } catch (IOException e) {
                    showToast(e.getMessage());
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        disconnect();
        super.onPause();
    }

    /**
     * Text output.
     * @param hint text to be displayed on the screen.
     */
    private void showToast(final String hint) {
        Context context = getApplicationContext();
        if (context != null) {
            Toast.makeText(context, hint, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Connect to remote bluetooth node.
     * @param remoteMacAddress MAC address of remote bluetooth node.
     * @return true if connected.
     */
    private boolean connect(final String remoteMacAddress) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        if (mConnected) {
            return true;
        }

        // Check that Bluetooth adapter exists.
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showToast("Bluetooth adapter not found");
            return false;
        }

        // If Bluetooth adapter exists but it is turned off then turn it on and exit.
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
            return false;
        }

        // Connection.
        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(remoteMacAddress);
        Method m = bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
        mBluetoothSocket = (BluetoothSocket) m.invoke(bluetoothDevice, 1);
        mBluetoothSocket.connect();

        // Init output stream. mOutputStream will be used in send() method.
        mOutputStream = mBluetoothSocket.getOutputStream();

        mConnected = true;
        return true;
    }

    /**
     * Disconnect and dispatch bluetooth resources.
     */
    private void disconnect() {
        if (mConnected) {
            try {
                if (mBluetoothSocket != null) {
                    mBluetoothSocket.close();
                    mBluetoothSocket = null;
                }
            } catch (IOException e) {
                mBluetoothSocket = null;
            }
        }
        mConnected = false;
    }

    /**
     * Sends sequence of chars to remote bluetooth node.
     * @param message to be sent.
     */
    private void send(final String message) throws IOException {
        if (mConnected) {
            if (mBluetoothSocket != null) {
                mOutputStream.write(message.getBytes());
            }
        }
    }
}
