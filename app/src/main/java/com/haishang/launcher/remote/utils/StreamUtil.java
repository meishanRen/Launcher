package com.haishang.launcher.remote.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {

    /**
     * 保存解析的XML信息
     *
     * @return
     * @throws IOException
     */
    public static String streamToString(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        String string = new String(baos.toByteArray(), "GBK");
        is.close();
        baos.close();
        return string;
    }
}

