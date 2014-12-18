# SmartLog

基于ACRA的 LOG处理框架


-------------------

## 简介

> SmartLog 是一个基于 [ACRA](https://github.com/ACRA/acra)的 LOG处理框架，如果你的项目需要将业务流程Log写入指定文件，而且当你发布的之后，想要了解产品的ERROR Log，SmartLog就是你的选择。

### 使用

#### 1. Error Log的使用配置方式
在ACRA中添加了一个**`savePath`**配置项，用于说明error log 文件存储的地方，
而且补充了一个ACRA mode: **`ReportingInteractionMode.NONE`**，用于说明不需要上传log文件到服务端。

配置方法如下：
##### 代码块
    @ReportsCrashes(
            formKey = "", // This is required for backward compatibility but not used
            formUri = "http://192.168.xxx.xxx",
            customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME, 
                ReportField.ANDROID_VERSION,
                ......
            }, 
            mode = ReportingInteractionMode.NONE,
            savePath = "/sdcard/cloudLogs/errorLogs"
            )
    public class MyApplication extends Application {}

#### 2. 业务Log的使用配置方式
此工程中包含了一个**`Logger`**的class，其有一系列和android.util.Log类似的方法：
>Logger.debug()
Logger.info()
Logger.error()
Logger.warn()
Logger.verbose()

其可以利用Adapter，将Log写入指定文件，使用者也可以继承覆写Adapter，以完成指定的功能，
一般初始化方法如下：
##### 代码块
    private void initLog() {
        MultipleAppender ma = new MultipleAppender();
        String fileName = "savelog.txt";
        String userDir;

        if (isSDCardMounted()) {
            userDir = Environment.getExternalStorageDirectory().getPath()
                    + System.getProperty("file.separator") + "AppLogs/workLogs";

            File dir = new File(userDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } else {
            userDir = getFilesDir().getAbsolutePath() + System.getProperty("file.separator");
        }

        FileAppender fileAppender = new FileAppender(userDir, fileName);
        fileAppender.setLogContentType(!isSDCardMounted());
        fileAppender.setMaxFileSize(5 * 1024 * 1024); // Set 5M log size
        ma.addAppender(fileAppender);

        // If we are running in the emulator, we also use the AndroidLogger
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        // must have android.permission.READ_PHONE_STATE
        String deviceId = tm.getDeviceId();
        if ("000000000000000".equals(deviceId) || "debug".equals(BuildInfo.MODE)) {
            // This is an emulator, or a debug build
            AndroidLogAppender androidLogAppender = new AndroidLogAppender("YourApp");
            ma.addAppender(androidLogAppender);
        }

        // level <= Logger.WARN, will write into file.
        Logger.initLog(ma, Logger.WARN); //第二个参数很重要，凡是级别小于这个参数的Log都会被写入文件.
        Logger.setRelease(true); //如果是发布产品，那么就不希望业务相关的log被打印出来，那就传入true，反之，false.
        
        Logger.info(TAG_LOG, "Log file created into: " + userDir + fileName);
        Logger.info("Memory card present: " + isSDCardMounted());
    }

-------------------
###希望大家多多支持，谢谢!