package com.example.dex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.text.TextUtils;
import dalvik.system.DexClassLoader;

public class DexLoader {
	@SuppressLint("NewApi")
    public static void load(Context context, String dexFileName) {
		if(TextUtils.isEmpty(dexFileName))
			return;

        File dexDir = new File(context.getFilesDir(), "dlibs");
        dexDir.mkdir();
        File dexFile = new File(dexDir, dexFileName);
        File dexOpt = new File(dexDir, "opt");
        dexOpt.mkdir();
        try {
            InputStream ins = context.getAssets().open(dexFileName);
            if (!dexFile.exists() || dexFile.length() != ins.available()) {
                FileOutputStream fos = new FileOutputStream(dexFile);
                byte[] buf = new byte[4096];
                int l;
                while ((l = ins.read(buf)) != -1) {
                    fos.write(buf, 0, l);
                }
                fos.close();
            }
            ins.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ClassLoader cl = context.getClassLoader();
        ApplicationInfo ai = context.getApplicationInfo();
        String nativeLibraryDir = null;
        if (Build.VERSION.SDK_INT > 8) {
            nativeLibraryDir = ai.nativeLibraryDir;
        } else {
            nativeLibraryDir = "/data/data/" + ai.packageName + "/lib/";
        }
        DexClassLoader dcl = new DexClassLoader(dexFile.getAbsolutePath(),
                dexOpt.getAbsolutePath(), nativeLibraryDir, cl.getParent());

        try {
            Field f = ClassLoader.class.getDeclaredField("parent");
            f.setAccessible(true);
            f.set(cl, dcl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
