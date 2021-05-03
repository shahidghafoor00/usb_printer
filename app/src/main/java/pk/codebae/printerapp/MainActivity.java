package pk.codebae.printerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.usb.UsbConnection;
import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

public class MainActivity extends AppCompatActivity {

    private static final String ACTION_USB_PERMISSION = "pk.codebae.printerapp.USB_PERMISSION";

    TextView editText;
    Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.edit_text);
        button = findViewById(R.id.button_print);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printUsb();
//                if (editText.getText().toString().isEmpty()) {
//                    editText.setError("required");
//                } else {
//                    editText.setError(null);
//
//                }
            }
        });
    }


    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MainActivity.ACTION_USB_PERMISSION.equals(action)) {
                synchronized (MainActivity.this) {
                    UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                    UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbManager != null && usbDevice != null) {
                            EscPosPrinter printer = null;
                            try {
                                printer = new EscPosPrinter(new UsbConnection(usbManager, usbDevice), 203, 48f, 32);
                            } catch (EscPosConnectionException e) {
                                e.printStackTrace();
                            }
                            try {
                                Toast.makeText(context, "Printing...", Toast.LENGTH_SHORT).show();
                                printer
                                        .printFormattedText(
                                                "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer,
                                                        getResources().getDrawableForDensity(R.mipmap.ic_launcher, DisplayMetrics.DENSITY_MEDIUM))+"</img>\n" +
                                                        "[C]================================\n" +
                                                        "[C] Hello There\n" +
                                                        "[C]================================\n"
                                        );
                            } catch (EscPosConnectionException | EscPosParserException | EscPosEncodingException | EscPosBarcodeException e) {
                                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }
        }
    };


    public void printUsb() {
        UsbConnection usbConnection = UsbPrintersConnections.selectFirstConnected(MainActivity.this);
        UsbManager usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        if (usbConnection != null && usbManager != null) {
            Toast.makeText(this, "usb connection success", Toast.LENGTH_SHORT).show();
            PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(MainActivity.ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(MainActivity.ACTION_USB_PERMISSION);
            registerReceiver(this.usbReceiver, filter);
            usbManager.requestPermission(usbConnection.getDevice(), permissionIntent);
        }
    }
}