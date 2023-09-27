package com.archko.subtitle;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    public static File open(Context context, String str) {
        return new File(context.getExternalCacheDir().getAbsolutePath() + "/qjscache_" + str + ".js");
    }

    public static boolean writeSimple(byte[] data, File dst) {
        try {
            if (dst.exists())
                dst.delete();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dst));
            bos.write(data);
            bos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static byte[] readSimple(File src) {
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(src));
            int len = bis.available();
            byte[] data = new byte[len];
            bis.read(data);
            bis.close();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void copyFile(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    public static void recursiveDelete(File file) {
        try {
            if (!file.exists())
                return;
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    recursiveDelete(f);
                }
            }
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCache(Context context, String name) {
        try {
            String code = "";
            File file = open(context, name);
            if (file.exists()) {
                code = new String(readSimple(file));
            }
            if (TextUtils.isEmpty(code)) {
                return "";
            }
            JsonObject asJsonObject = (new Gson().fromJson(code, JsonObject.class)).getAsJsonObject();
            if (((long) asJsonObject.get("expires").getAsInt()) > System.currentTimeMillis() / 1000) {
                return asJsonObject.get("data").getAsString();
            }
            recursiveDelete(open(context, name));
            return "";
        } catch (Exception e4) {
            return "";
        }
    }

    public static void setCache(Context context, int time, String name, String data) {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("expires", (int) (time + (System.currentTimeMillis() / 1000)));
            jSONObject.put("data", data);
            writeSimple(jSONObject.toString().getBytes(), open(context, name));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File getExternalCacheDir(Context context) {
        return context.getExternalCacheDir();
    }

    public static String getExternalCachePath(Context context) {
        return getExternalCacheDir(context).getAbsolutePath();
    }

    public static String getCachePath(Context context) {
        return getExternalCacheDir(context).getAbsolutePath();
    }

    public static void cleanPlayerCache(Context context) {
        String thunderCachePath = getCachePath(context) + "/thunder/";
        File thunderCacheDir = new File(thunderCachePath);
        try {
            if (thunderCacheDir.exists()) recursiveDelete(thunderCacheDir);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String ijkCachePath = getExternalCachePath(context) + "/ijkcaches/";
        File ijkCacheDir = new File(ijkCachePath);
        try {
            if (ijkCacheDir.exists()) recursiveDelete(ijkCacheDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getFileNameWithoutExt(String filePath) {
        if (TextUtils.isEmpty(filePath)) return "";
        String fileName = filePath;
        int p = fileName.lastIndexOf(File.separatorChar);
        if (p != -1) {
            fileName = fileName.substring(p + 1);
        }
        p = fileName.indexOf('.');
        if (p != -1) {
            fileName = fileName.substring(0, p);
        }
        return fileName;
    }

}