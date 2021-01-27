package com.cn.hzm.core.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/17 3:30 下午
 */
public class FtpFileUtil {

    /**
     * ftp服务器ip地址
     */
    private static final String FTP_ADDRESS = "122.225.98.164";

    /**
     * 端口号
     */
    private static final int FTP_PORT = 2122;

    /**
     * 用户名
     */
    private static final String FTP_USERNAME = "liftp2";

    /**
     * 密码
     */
    private static final String FTP_PASSWORD = "liftp2pass";

    /**
     * 图片路径：付款图片
     */
    private static final String PAY_PATH = "/erp/pay/";

    /**
     * 图片路径：商品细节
     */
    private static final String DETAIL_PATH = "/erp/image/";

    private static final String URI_PRE = "http://image.hzman.com.cn";

    public static String uploadFile(String originFileName, InputStream input, String photoType) {
        FTPClient ftp = new FTPClient();
        ftp.setControlEncoding("GBK");
        String path;
        try {
            ftp.connect(FTP_ADDRESS, FTP_PORT);
            ftp.login(FTP_USERNAME, FTP_PASSWORD);

            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                return null;
            }

            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

            //根据图片功能选择路径
            path = "detail".equals(photoType) ? DETAIL_PATH : PAY_PATH;
            ftp.changeWorkingDirectory(path);
            ftp.storeFile(originFileName, input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                input.close();
                ftp.logout();
                if (ftp.isConnected()) {
                    ftp.disconnect();
                }
            } catch (IOException ignored) {
            }
        }
        return URI_PRE + path + originFileName;
    }
}
