package edu.seu.shuttle;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQ_CODE = 101;
    private TextView tvPermStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvPermStatus = findViewById(R.id.tv_perm_status);
        Button btnOpenWeb = findViewById(R.id.btn_open_web);

        btnOpenWeb.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bourgjavisdes29990-ctrl.github.io/seu-shuttle/"));
            startActivity(intent);
        });

        checkAndRequestPermission();
    }

    private void checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            tvPermStatus.setText("✅ 智能定位已开启（兰台 / 九龙湖自动切换）");
            tvPermStatus.setTextColor(0xFF22C55E); // Green
            ShuttleWidgetProvider.updateAllWidgets(this);
        } else {
            tvPermStatus.setText("📍 正在请求定位权限...");
            ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                PERMISSION_REQ_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tvPermStatus.setText("✅ 智能定位已开启（兰台 / 九龙湖自动切换）");
                tvPermStatus.setTextColor(0xFF22C55E);
                Toast.makeText(this, "定位开启成功！小部件将自动识别所在校区", Toast.LENGTH_SHORT).show();
                ShuttleWidgetProvider.updateAllWidgets(this);
            } else {
                tvPermStatus.setText("⚠️ 未开启定位权限（卡片将使用手动切换）");
                tvPermStatus.setTextColor(0xFFF59E0B);
            }
        }
    }
}
