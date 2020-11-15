package com.example.versiontarget;

import androidx.annotation.BinderThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static android.os.Build.VERSION.SDK_INT;

public class MainActivity extends AppCompatActivity {


    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothLeScanner bluetoothLeScanner;
    private Button mButton;
    final static int PERMISSION_GRANTED = 1;
    final static int NO_ADAPTER = 0;
    final static int NO_BLE = 4;
    final static int USER_REQUEST = 2;
    final static int PERMISSION_REQUEST = 0;
    final static int BT_ACTIVATION_REQUEST = 1;
    private static final long SCAN_PERIOD = 10000;
    private boolean mScanning = false;

    private Handler handler = new Handler();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton=findViewById(R.id.button);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBluetoothAdapter !=null) {
                    bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                    scanLeDevice();
                    Log.i("SCANN","Scann lancé");
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        switch (isBluetoothReady()) {
                // Il n'y a pas d'adaptateur BT !
            case NO_ADAPTER:
                Toast.makeText(this, "Pas d'adaptateur Bluetooth", Toast.LENGTH_LONG).show();
                finish();
                break;

            case NO_BLE:
                Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
                finish();
                break;

                // La permission a été donnée pour la localisation fine
            case PERMISSION_GRANTED:

                onBluetoothActivationRequest();
                break;

                // la demande a été faite à l'utilisateur (retour par onRequestPermissionsResult...)
            case USER_REQUEST:
                break;
        }

    }

    /**
     * Teste l'état de l'adaptateur et des droits
     * @return indication du droit (0 : pas d'adaptateur, 1 : Permission ok, 2 : demandée)
     */
    private int isBluetoothReady() {
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            return NO_ADAPTER;
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return NO_BLE;
        }

       if (SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST);
                return USER_REQUEST;
            }
        }
        return PERMISSION_GRANTED;
    }


    /**
     * Demande de permissions
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, getString(R.string.alert_bluetooth_auth), Toast.LENGTH_SHORT).show();
                    Log.i("BT","permission BT refusée");
                    finish();
                }
            }
            onBluetoothActivationRequest();
        }
    }

    /**
     * Appelé sur retour d'activation du BT
     * @param requestCode : code de retour
     * @param resultCode : résultat du retour
     * @param data : données supplémentaires échangées
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BT_ACTIVATION_REQUEST){
            if (mBluetoothAdapter.isEnabled())
                Toast.makeText(this, "le bt est activé", Toast.LENGTH_SHORT).show();
            else {
                Toast.makeText(this, getString(R.string.alert_bluetooth_inactiv), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Appelé pour l'activation du BT
     */
    private void onBluetoothActivationRequest(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBluetoothAdapter.isEnabled()){
            Intent BTActivation = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(BTActivation,BT_ACTIVATION_REQUEST);
        }
    }


    /**
     * lancement d'un scann pour une durée de 10s ou jusqu'à nouvel appel de la méthode
     */
    private void scanLeDevice() {
        if (!mScanning) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                    Log.i("SCANN","Stop");
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            mScanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    /**
     * Callback en cas de découverte
     */
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                }
            };



}
