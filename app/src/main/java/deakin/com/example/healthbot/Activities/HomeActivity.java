package deakin.com.example.healthbot.Activities;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.mbientlab.bletoolbox.scanner.BleScannerFragment;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.module.Settings;

import java.util.UUID;

import bolts.Task;
import deakin.com.example.healthbot.Models.CurrentUser;
import deakin.com.example.healthbot.R;

public class HomeActivity extends AppCompatActivity implements ServiceConnection, BleScannerFragment.ScannerCommunicationBus {
    public static final int REQUEST_START_APP= 1;
    private final static UUID[] serviceUuids;

    Context context;
    LinearLayout rootView;


    static {
        serviceUuids= new UUID[] {
                MetaWearBoard.METAWEAR_GATT_SERVICE,
                MetaWearBoard.METABOOT_SERVICE
        };
    }

    static void setConnInterval(Settings settings) {
        if (settings != null) {
            Settings.BleConnectionParametersEditor editor = settings.editBleConnParams();
            if (editor != null) {
                editor.maxConnectionInterval(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 11.25f : 7.5f)
                        .commit();
            }
        }
    }
    public static Task<Void> reconnect(final MetaWearBoard board) {
        return board.connectAsync()
                .continueWithTask(task -> {
                    if (task.isFaulted()) {
                        return reconnect(board);
                    } else if (task.isCancelled()) {
                        return task;
                    }
                    return Task.forResult(null);
                });
    }

    private BtleService.LocalBinder serviceBinder;
    private MetaWearBoard mwBoard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
//        setToolBar();
        context = getApplicationContext();
        rootView = (LinearLayout) findViewById(R.id.activity_home);

        ///< Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, BtleService.class),
                this, Context.BIND_AUTO_CREATE);

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

        }
        return super.dispatchTouchEvent(event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }



    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (!CurrentUser.loggedIN) {
            Intent login = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(login);
            finish();
        } else {


        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_START_APP:
                ((BleScannerFragment) getFragmentManager().findFragmentById(R.id.scanner_fragment)).startBleScan();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDeviceSelected(final BluetoothDevice btDevice) {
        serviceBinder.removeMetaWearBoard(btDevice);
        mwBoard= serviceBinder.getMetaWearBoard(btDevice);


        final ProgressDialog connectDialog = new ProgressDialog(this);
        connectDialog.setTitle(getString(R.string.title_connecting));
        connectDialog.setMessage(getString(R.string.message_wait));
        connectDialog.setCancelable(false);
        connectDialog.setCanceledOnTouchOutside(false);
        connectDialog.setIndeterminate(true);
        connectDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.label_cancel), (dialogInterface, i) -> mwBoard.disconnectAsync());
        connectDialog.show();

        mwBoard.connectAsync()
                .continueWithTask(task -> {
                    if (task.isCancelled()) {
                        return task;
                    }
                    return task.isFaulted() ? reconnect(mwBoard) : Task.forResult(null);
                })
                .continueWith(task -> {
                    if (!task.isCancelled()) {
                        setConnInterval(mwBoard.getModule(Settings.class));
                        runOnUiThread(connectDialog::dismiss);
                        Intent navActivityIntent = new Intent(HomeActivity.this, NavigationActivity.class);
                        navActivityIntent.putExtra(NavigationActivity.EXTRA_BT_DEVICE, btDevice);
                        startActivityForResult(navActivityIntent, REQUEST_START_APP);
                    }
                    return null;
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ///< Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ///< Typecast the binder to the service's LocalBinder class
        serviceBinder = (BtleService.LocalBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }

    @Override
    public UUID[] getFilterServiceUuids() {
        return serviceUuids;
    }

    @Override
    public long getScanDuration() {
        return 10000L;
    }


}
