package com.ecnu.utils.algorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 不可逆干扰之密码散列函数MD5实现
 * MD5 cipher
 * @author zou yuanyuan on 2018/02/09
 */
public class MD5Util {
    /**
     * 针对单个String 的加密
     * @param originData
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String md5Cipher(String originData) throws NoSuchAlgorithmException {
        //用指定算法生成MessageDigest对象。
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        //使用指定的 byte 数组更新messageDigest
        messageDigest.update(originData.getBytes());
        //完成hash计算，同时调用digest方法之后messageDigest对象被重置
        byte[] digest = messageDigest.digest();
        return toHex(digest);
    }

    /**
     * 将16位byte[] 转换为32位String
     * @param buffer
     * @return
     */
    private static String toHex(byte[] buffer) {
        StringBuffer sb = new StringBuffer(buffer.length * 2);
        for (int i = 0; i < buffer.length; i++) {
            sb.append(Character.forDigit((buffer[i] & 240) >> 4, 16));
            sb.append(Character.forDigit(buffer[i] & 15, 16));
        }
        return sb.toString();
    }
}
