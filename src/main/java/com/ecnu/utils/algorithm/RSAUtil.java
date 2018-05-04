package com.ecnu.utils.algorithm;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**RSA加密
 * @author asus
 */
public class RSAUtil {
    /**填充模式*/
    final private static int PKCS1 = 1;

    private static RSAPublicKey publicKey;
    private static RSAPrivateKey privateKey;
    private static KeyPair pair;

    /**多次操作的计数器（预留）*/
    private static int pairIsChanged=0;

    /**
     * 用户获取秘钥
     */
    public static RSAPrivateKey getPrivateKey(){
        return privateKey;
    }

    public static RSAPublicKey getPublicKey(){
        return publicKey;
    }

    /**
     * 用公钥对明文进行RSA加密的入口。
     * @param originData
     * @return
     * @throws Exception
     */
    public static String execute(String originData) throws Exception {

        byte[] cipher=publicEncrypt(originData.getBytes(), 1);
        return changeBytesToString(cipher);
    }

    /**
     * 使用公钥加密
     * @param clearBytes
     * @param type
     * @return
     */
    private static byte[] publicEncrypt(byte[] clearBytes, int type) {
        BigInteger mod = publicKey.getModulus();
        // 指数
        BigInteger publicExponent = publicKey.getPublicExponent();
        RSAKeyParameters para = new RSAKeyParameters(false, mod, publicExponent);

        AsymmetricBlockCipher engine = new RSAEngine();
        if(type == PKCS1) {
            engine = new PKCS1Encoding(engine);
        }
        engine.init(true, para);
        try {
            byte[] data = engine.processBlock(clearBytes, 0, clearBytes.length);
            return data;
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String changeBytesToString(byte[] data) {
        return new String(Hex.encode(data));
    }

    public static void generateRSAKeyPair(int keyLength,byte[] seed) {
        try {
            KeyPairGenerator rsaKeyGen = KeyPairGenerator.getInstance("RSA");
            // setKeyLength 1024,setCertaintyOfPrime
            rsaKeyGen.initialize(keyLength, new SecureRandom(seed));
            pair = rsaKeyGen.genKeyPair();
            publicKey = (RSAPublicKey) pair.getPublic();
            privateKey = (RSAPrivateKey) pair.getPrivate();
            pairIsChanged++;
        } catch (Exception e) {
            System.out.println("Exception in keypair generation. Reason: " + e);
        }
    }

    public static void generateRSAKeyPair() {
        try {
            KeyPairGenerator rsaKeyGen = KeyPairGenerator.getInstance("RSA");
            // setKeyLength 1024,setCertaintyOfPrime
            //rsaKeyGen.initialize(keyLength, new SecureRandom());
            rsaKeyGen.initialize(1024, new SecureRandom());
            pair = rsaKeyGen.genKeyPair();
            publicKey = (RSAPublicKey) pair.getPublic();
            privateKey = (RSAPrivateKey) pair.getPrivate();
            pairIsChanged++;
        } catch (Exception e) {
            System.out.println("Exception in keypair generation. Reason: " + e);
        }
    }
}
