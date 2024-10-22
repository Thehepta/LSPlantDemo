package com.hepta.lsplantdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.nio.charset.StandardCharsets;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class LoadEntry {

    public static String TAG = "PDDHOOK";

    public static void Entry(Context ctx, String source, String argument) {
        PreLoadNativeSO(ctx,source);

        hook_activity();


    }

    public static void PreLoadNativeSO(Context context, String source) {
        try {
            String abi= "arm64-v8a";
            if(!android.os.Process.is64Bit()){
                abi = "armeabi-v7a";
            }
            String sopath = source+"!/lib/"+abi+"/liblsplant" +
                    ".so";
            System.load(sopath);

            String sopath1 = source+"!/lib/"+abi+"/liblsplantdemo" +
                    ".so";
            System.load(sopath1);
        }catch (Exception e){
            Log.e("LoadEntry","LoadSo error");
        }
    }

    static void  hook_activity(){
        try {
            XposedBridge.hookMethod(Activity.class.getDeclaredMethod("onCreate", Bundle.class), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Log.e("hepta", "Before   onCreate()");
                    super.beforeHookedMethod(param);
                }
            });
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }



}
