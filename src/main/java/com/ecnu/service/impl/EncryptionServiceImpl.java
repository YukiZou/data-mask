package com.ecnu.service.impl;

import com.ecnu.utils.algorithm.*;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * 提供平台中实现的所有脱敏方法服务
 * @author zou yuanyuan
 */
@Service
public class EncryptionServiceImpl {
    /**
     * setup方法
     */
    public void setup() throws NoSuchAlgorithmException {
        RSAUtil.generateRSAKeyPair();
        FormatPreservingUtil.setup();
        PaillierUtil.keyGeneration();
    }

    /**
     * AES脱敏方法入口
     * @param originData
     * @return
     * @throws Exception
     */
    public List<String> executeAES(List<String> originData) throws Exception {
        List<String> cipherData = new ArrayList<>();
        for (String str : originData) {
            String cipherStr = AESUtil.encrypt(str);
            cipherData.add(cipherStr);
        }
        return cipherData;
    }

    /**
     * k-匿名方法入口
     * 有参数k, 只能处理能解析成 浮点型的 数据列
     * @param originData
     * @return
     * @throws Exception
     */
    public List<String> executeAnonymity(List<String> originData, int k) throws Exception {
        return AnonymityUtil.anonymityIntNumPrepare(originData, k);
    }

    /**
     * 凯撒
     * @param originData
     * @return
     * @throws Exception
     */
    public List<String> executeCaesar(List<String> originData, int parameter) throws Exception {
        return CaesarUtil.caesarCipher(originData, parameter);
    }

    /**
     * 保形加密
     * 目前只能处理小写字母组成的字符串
     * @param originData
     * @return
     * @throws Exception
     */
    public List<String> executeFormatPreserving(List<String> originData) throws Exception {
        List<String> cipherData = new ArrayList<>();
        for (String str : originData) {
            String cipherStr = FormatPreservingUtil.execute(str);
            cipherData.add(cipherStr);
        }
        return cipherData;
    }

    /**
     * Epsilon 差分隐私
     * 将String转化成 int/double再进行差分隐私
     * 有参数，通常在 0-1之间。
     * @param originData
     * @param epsilon
     * @return
     * @throws Exception
     */
    public List<String> executeEpsilonDifferentialPrivacy(List<String> originData, double epsilon) throws Exception {
        List<String> cipherData = new ArrayList<>();
        for (String str : originData) {
            double cipherStr = Double.parseDouble(str) + Laplace.pdf(1 / epsilon);
            cipherData.add(Double.toString(cipherStr));
        }
        return cipherData;
    }

    /**
     * MD5加密方法入口
     * @param originData
     * @return
     * @throws Exception
     */
    public List<String> executeMD5(List<String> originData) throws Exception {
        List<String> cipherData = new ArrayList<>();
        for (String str : originData) {
            String cipherStr = MD5Util.md5Cipher(str);
            cipherData.add(cipherStr);
        }
        return cipherData;
    }

    /**
     * 同态加密方法入口
     * 针对大整型输入数据脱敏
     * @param originData
     * @return
     * @throws Exception
     */
    public List<String> executePaillier(List<String> originData) throws Exception {
        List<String> cipherData = new ArrayList<>();
        for (String str : originData) {
            BigInteger m = new BigInteger(str);
            String cipherStr = PaillierUtil.encryption(m).toString();
            cipherData.add(cipherStr);
        }
        return cipherData;
    }

    /**
     * 栅栏置换方法入口
     * @param originData
     * @param parameter
     * @return
     * @throws Exception
     */
    public List<String> executeRailFence(List<String> originData, int parameter) throws Exception {
        return RailFenceUtil.railFenceCipher(originData, parameter);
    }

    /**
     * RSA脱敏方法入口
     * @param colList
     * @return
     * @throws Exception
     */
    public List<String> executeRSA(List<String> colList) throws Exception {
        List<String> cipherData = new ArrayList<>();
        for (String cell : colList) {
            String cipherStr = RSAUtil.execute(cell);
            cipherData.add(cipherStr);
        }
        return cipherData;
    }
}
