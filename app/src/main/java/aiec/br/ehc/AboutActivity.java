package aiec.br.ehc;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            TextView lblVersion = (TextView) findViewById(R.id.app_version);
            lblVersion.setText("Versão " + version.toString());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
