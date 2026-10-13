package com.zain.pasahu;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothSocket> activeSockets = new ArrayList<>();
    private List<OutputStream> activeOutputStreams = new ArrayList<>();

    private int pattern = 0, speed = 100, intensity = 100;
    private TextView lblSpeed, lblIntensity;
    private Spinner patternSpinner;

    private static final String PERMISSION_BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT";
    private static final String PERMISSION_BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkAndRequestPermissions();

        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.parseColor("#060A17"));

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 40, 40, 40);
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView txtTitle = new TextView(this);
        txtTitle.setText("ZAIN PASAHU OS");
        txtTitle.setTextColor(Color.parseColor("#00F5FF"));
        txtTitle.setTextSize(28);
        txtTitle.setTypeface(null, Typeface.BOLD);
        txtTitle.setPadding(0, 20, 0, 10);
        mainLayout.addView(txtTitle);

        TextView txtSub = new TextView(this);
        txtSub.setText("مستقبل الاتزان الحيوي بين العقل والجسد");
        txtSub.setTextColor(Color.parseColor("#8892B0"));
        txtSub.setTextSize(14);
        txtSub.setGravity(Gravity.CENTER);
        txtSub.setPadding(0, 0, 0, 30);
        mainLayout.addView(txtSub);

        Button btnConnect = new Button(this);
        btnConnect.setText("📡 ربط السوارين المزدوج");
        btnConnect.setBackgroundColor(Color.parseColor("#00F5FF"));
        btnConnect.setTextColor(Color.BLACK);
        btnConnect.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 0, 0, 30);
        btnConnect.setLayoutParams(btnParams);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToDualDevices();
            }
        });
        mainLayout.addView(btnConnect);

        patternSpinner = new Spinner(this);
        patternSpinner.setBackgroundColor(Color.parseColor("#112240"));
        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        spinnerParams.setMargins(0, 0, 0, 30);
        patternSpinner.setLayoutParams(spinnerParams);
        patternSpinner.setPadding(20, 30, 20, 30);
        mainLayout.addView(patternSpinner);

        String[] patterns = {"وضع الاستعداد / STANDBY", "الثبات والحركة / WALK", "الاسترخاء والخوف / ANTI-FEAR", "شحن الطاقة / ENERGY", "الاستشفاء العضلي / RECOVER", "التركيز / FOCUS", "التهدئة / CALM", "النوم / SLEEP"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, patterns);
        patternSpinner.setAdapter(adapter);
        patternSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pattern = position;
                sendDataPacket();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // صف التحكم بالسرعة
        LinearLayout speedLayout = new LinearLayout(this);
        speedLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        speedLayout.setBackgroundColor(Color.parseColor("#111C30"));
        speedLayout.setPadding(25, 25, 25, 25);
        speedLayout.setOrientation(LinearLayout.HORIZONTAL);
        speedLayout.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams speedMargin = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        speedMargin.setMargins(0, 0, 0, 20);
        speedLayout.setLayoutParams(speedMargin);

        lblSpeed = new TextView(this);
        lblSpeed.setText("🏃 السرعة: 100%");
        lblSpeed.setTextColor(Color.WHITE);
        lblSpeed.setTextSize(16);
        lblSpeed.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        speedLayout.addView(lblSpeed);

        Button btnSpeedMinus = new Button(this);
        btnSpeedMinus.setText("-");
        btnSpeedMinus.setBackgroundColor(Color.parseColor("#00F5FF"));
        btnSpeedMinus.setTextColor(Color.BLACK);
        btnSpeedMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speed = Math.max(40, speed - 10);
                updateUI();
            }
        });
        speedLayout.addView(btnSpeedMinus);

        Button btnSpeedPlus = new Button(this);
        btnSpeedPlus.setText("+");
        btnSpeedPlus.setBackgroundColor(Color.parseColor("#00F5FF"));
        btnSpeedPlus.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p1.setMarginStart(15);
        btnSpeedPlus.setLayoutParams(p1);
        btnSpeedPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speed = Math.min(200, speed + 10);
                updateUI();
            }
        });
        speedLayout.addView(btnSpeedPlus);
        mainLayout.addView(speedLayout);

        // صف التحكم بالشدة
        LinearLayout intLayout = new LinearLayout(this);
        intLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        intLayout.setBackgroundColor(Color.parseColor("#111C30"));
        intLayout.setPadding(25, 25, 25, 25);
        intLayout.setOrientation(LinearLayout.HORIZONTAL);
        intLayout.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams intMargin = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        intMargin.setMargins(0, 0, 0, 40);
        intLayout.setLayoutParams(intMargin);

        lblIntensity = new TextView(this);
        lblIntensity.setText("⚡ الشدة: 100%");
        lblIntensity.setTextColor(Color.WHITE);
        lblIntensity.setTextSize(16);
        lblIntensity.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        intLayout.addView(lblIntensity);

        Button btnIntMinus = new Button(this);
        btnIntMinus.setText("-");
        btnIntMinus.setBackgroundColor(Color.parseColor("#00F5FF"));
        btnIntMinus.setTextColor(Color.BLACK);
        btnIntMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intensity = Math.max(10, intensity - 10);
                updateUI();
            }
        });
        intLayout.addView(btnIntMinus);

        Button btnIntPlus = new Button(this);
        btnIntPlus.setText("+");
        btnIntPlus.setBackgroundColor(Color.parseColor("#00F5FF"));
        btnIntPlus.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p2.setMarginStart(15);
        btnIntPlus.setLayoutParams(p2);
        btnIntPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intensity = Math.min(180, intensity + 10);
                updateUI();
            }
        });
        intLayout.addView(btnIntPlus);
        mainLayout.addView(intLayout);

        // زر الإيقاف الفوري
        Button btnStop = new Button(this);
        btnStop.setText("🛑 إيقاف النظام الفوري");
        btnStop.setBackgroundColor(Color.parseColor("#E63946"));
        btnStop.setTextColor(Color.WHITE);
        btnStop.setTypeface(null, Typeface.BOLD);
LinearLayout.LayoutParams stopParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
btnStop.setLayoutParams(stopParams);
btnStop.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
pattern = 0;
patternSpinner.setSelection(0);
sendDataPacket();
Toast.makeText(MainActivity.this, "تم إرسال حزمة الإيقاف الفوري للنظام 🛑", Toast.LENGTH_SHORT).show();
}
});
mainLayout.addView(btnStop);
scrollView.addView(mainLayout);
setContentView(scrollView);
}
private void checkAndRequestPermissions() {
if (Build.VERSION.SDK_INT >= 31) {
if (ContextCompat.checkSelfPermission(this, PERMISSION_BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
ActivityCompat.requestPermissions(this, new String[]{
PERMISSION_BLUETOOTH_CONNECT,
PERMISSION_BLUETOOTH_SCAN
}, 101);
}
}
}
private void connectToDualDevices() {
if (Build.VERSION.SDK_INT >= 31 &&
ActivityCompat.checkSelfPermission(this, PERMISSION_BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
Toast.makeText(this, "يرجى منح صلاحية الأجهزة القريبة!", Toast.LENGTH_SHORT).show();
return;
}
if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
Toast.makeText(this, "يرجى تشغيل بلوتوث الهاتف أولاً!", Toast.LENGTH_SHORT).show();
return;
}
Set pairedDevices = bluetoothAdapter.getBondedDevices();
int connectionCount = 0;
closeConnections();
Object[] devicesArray = pairedDevices.toArray();
for (Object obj : devicesArray) {
BluetoothDevice device = (BluetoothDevice) obj;
if (device.getName() != null && device.getName().contains("ZAIN_PASAHU_BLE")) {
try {
BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MY_UUID);
socket.connect();
activeSockets.add(socket);
activeOutputStreams.add(socket.getOutputStream());
connectionCount++;
if (connectionCount == 2) break;
} catch (IOException e) {
Toast.makeText(this, "فشل ربط: " + device.getName(), Toast.LENGTH_SHORT).show();
}
}
}
Toast.makeText(this, "تم الاتصال بنجاح بـ (" + connectionCount + ") من الأساور 📡", Toast.LENGTH_LONG).show();
}
private void updateUI() {
lblSpeed.setText("🏃 السرعة: " + speed + "%");
lblIntensity.setText("⚡ الشدة: " + intensity + "%");
sendDataPacket();
}
private void sendDataPacket() {
String packet = pattern + String.format("%03d", speed) + String.format("%03d", intensity) + String.format("%03d", intensity) + "\n";
for (OutputStream os : activeOutputStreams) {
try { os.write(packet.getBytes()); os.flush(); } catch (IOException e) {}
}
}
private void closeConnections() {
for (OutputStream os : activeOutputStreams) { try { os.close(); } catch (Exception e) {} }
for (BluetoothSocket socket : activeSockets) { try { socket.close(); } catch (Exception e) {} }
activeOutputStreams.clear();
activeSockets.clear();
}
@Override
protected void onDestroy() { super.onDestroy(); closeConnections(); }
}
