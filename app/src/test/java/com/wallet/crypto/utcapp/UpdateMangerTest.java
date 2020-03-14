package com.wallet.crypto.utcapp;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.google.gson.Gson;
import com.wallet.crypto.utcapp.entity.UpdateVersionInfo;
import com.wallet.crypto.utcapp.updateversion.UpdateVersionManager;
import com.wallet.crypto.utcapp.util.Base64;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.web3j.abi.datatypes.Address;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.wallet.crypto.utcapp.C.SERVER_URL;
import static com.wallet.crypto.utcapp.updateversion.UpdateVersionManager.getStrFromInsByCode;
import static org.junit.Assert.assertEquals;


public class UpdateMangerTest {
    /*   @Rule
       public ActivityTestRule<HomeActivity> mActivityTestRule = new ActivityTestRule<>(HomeActivity.class);*/
    @Test
    public void useAppContext() {

        // Context of the app under test.
        HttpURLConnection conn = null;
        Message msg = new Message();
        msg.what = GET_UPDATAINFO_ERROR;
        try {
            URL url = new URL(SERVER_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) throw new Exception("connect fail");
            // 从服务器获得一个输入流
            inputStream = conn.getInputStream();
            updateVersionInfo = new Gson().fromJson(getStrFromInsByCode(inputStream, "UTF-8"), UpdateVersionInfo.class);
            if (updateVersionInfo == null) throw new Exception("get version fail");
            updateVersionInfo = null;
            if (!checkVersion(updateVersionInfo.getVersion(), localVersion))
                msg.what = UPDATE_NONEED;
            else
                msg.what = UPDATE_CLIENT;
        } catch (Exception e) {
            msg.what = GET_UPDATAINFO_ERROR;
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (conn != null) conn.disconnect();
            } catch (Exception e) {
            }
//           handler.sendMessage(msg);
        }

    }
    @Test
    public  void testJIma(){
        String password="nihao";
        String jiami=Base64.byteArrayToBase64(password.getBytes());

      String a=new String(Base64.base64ToByteArray(jiami));
int b=0;
int ss=b+b;

    }
    //updateVersionInfo=null
    @Test
    public void useAppContext1() {

        // Context of the app under test.
        HttpURLConnection conn = null;
        Message msg = new Message();
        msg.what = GET_UPDATAINFO_ERROR;
        try {
            URL url = new URL(SERVER_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) throw new Exception("connect fail");
            // 从服务器获得一个输入流
            inputStream = conn.getInputStream();
            updateVersionInfo =null;
            if (updateVersionInfo == null) throw new Exception("get version fail");
            updateVersionInfo = null;
            if (!checkVersion(updateVersionInfo.getVersion(), localVersion))
                msg.what = UPDATE_NONEED;
            else
                msg.what = UPDATE_CLIENT;
        } catch (Exception e) {
            msg.what = GET_UPDATAINFO_ERROR;
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (conn != null) conn.disconnect();
            } catch (Exception e) {
            }
//           handler.sendMessage(msg);
        }

    }
    @Test
    public void test1(){

        BigInteger aa=  new BigInteger(" 4f891d7a9a794f0ea3aad61e305f8604220a7258", 16);
        int a=0;

    }
    //模拟responseCode！=200
    @Test
    public void useAppContext2() {

        // Context of the app under test.
        HttpURLConnection conn = null;
        Message msg = new Message();
        msg.what = GET_UPDATAINFO_ERROR;
        try {
            URL url = new URL(SERVER_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            int responseCode = 201;
            if (responseCode != 200) throw new Exception("connect fail");
            // 从服务器获得一个输入流
            inputStream = conn.getInputStream();
            updateVersionInfo = new Gson().fromJson(getStrFromInsByCode(inputStream, "UTF-8"), UpdateVersionInfo.class);
            if (updateVersionInfo == null) throw new Exception("get version fail");
            updateVersionInfo = null;
            if (!checkVersion(updateVersionInfo.getVersion(), localVersion))
                msg.what = UPDATE_NONEED;
            else
                msg.what = UPDATE_CLIENT;
        } catch (Exception e) {
            msg.what = GET_UPDATAINFO_ERROR;
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (conn != null) conn.disconnect();
            } catch (Exception e) {
            }
//           handler.sendMessage(msg);
        }

    }

    InputStream inputStream;
    UpdateVersionInfo updateVersionInfo;
    String localVersion = "1.0.1";
    public static final int UPDATE_NONEED = 0;
    private final int UPDATE_CLIENT = 1;
    public static final int GET_UPDATAINFO_ERROR = 2;
    Handler handler = new Handler();

    /**
     * 版本号比较
     *
     * @return结果说明：是否需要更新 true 更新   false 不更新
     */
    private boolean checkVersion(String serverVersion, String localVersion) {
        if (localVersion == null) return true;
        if (serverVersion != null && !serverVersion.equals("")) {
            if (serverVersion.equals(localVersion)) {
                return false;
            }
            String[] version1Array = serverVersion.split("\\.");
            String[] version2Array = localVersion.split("\\.");
            int index = 0;
            // 获取最小长度值
            int minLen = Math.min(version1Array.length, version2Array.length);
            int diff = 0;
            // 循环判断每位的大小
            while (index < minLen
                    && (diff = Integer.parseInt(version1Array[index])
                    - Integer.parseInt(version2Array[index])) == 0) {
                index++;
            }
            if (diff == 0) {
                // 如果位数不一致，比较多余位数
                for (int i = index; i < version1Array.length; i++) {
                    if (Integer.parseInt(version1Array[i]) > 0) {
                        return true;
                    }
                }

                for (int i = index; i < version2Array.length; i++) {
                    if (Integer.parseInt(version2Array[i]) > 0) {
                        return false;
                    }
                }
                return false;
            } else {
                return diff > 0;
            }
        } else {
            return false;
        }
    }
}
