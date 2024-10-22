package com.hepta.lsplantdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hepta.lsplantdemo.databinding.ActivityMainBinding;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'lsplantdemo' library on application startup.
    static {
        System.loadLibrary("lsplantdemo");
    }
    private static final String TAG = "LSPlant_MainActivity";
    private ActivityMainBinding binding;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mContext = this;
        ClassLoader classLoader = mContext.getClassLoader();
        binding.btnHook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XposedHelpers.findAndHookMethod(
                        "android.provider.Settings.Secure",
                        classLoader,
                        "getString",
                        ContentResolver.class,
                        String.class, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                super.beforeHookedMethod(param);
                                Log.d(TAG, "beforeHookedMethod: call android id ");
                            }

                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);
                                String androidId = (String) param.getResult();
                                Log.d(TAG, "afterHookedMethod: call android id :" + androidId);
                                param.setResult("123456789");
                            }
                        });
            }
        });
        binding.btnAndroidId.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("HardwareIds")
            @Override
            public void onClick(View v) {
                String androidId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
                Log.d(TAG, "onClick androidId: " + androidId);
                Toast.makeText(mContext, "androidId:" + androidId, Toast.LENGTH_SHORT).show();
            }
        });
    }

}