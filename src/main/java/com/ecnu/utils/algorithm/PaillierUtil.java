package com.ecnu.utils.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Random;

/**
 *经典同态加密算法：Paillier 加密算法
 * 针对数字加密。
 * @author zou yuanyuan on 2018/04/14
 */
public class PaillierUtil {
    private static Logger logger = LoggerFactory.getLogger(PaillierUtil.class);
    private static BigInteger p = new BigInteger("100001651");
    private static BigInteger q = new BigInteger("100000007");
    private static BigInteger n;
    private static BigInteger nsquare;
    private static BigInteger g;
    private static BigInteger lambda;

    public static void keyGeneration() {
        n = p.multiply(q);
        nsquare = n.multiply(n);
        g = new BigInteger("2");
        // gcd(a, b) * lcm(a, b) = a * b
        lambda = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE))
                .divide(p.subtract(BigInteger.ONE).gcd(q.subtract(BigInteger.ONE)));
    }

    /**
     * 针对明文 m 进行同态加密。
     * @param m 明文
     * @return
     */
    public static BigInteger encryption(BigInteger m) {
        //logger.info("start encrypt data.");
        //随机生成符合条件的r值， 0<r<n
        int bitLen = n.bitLength();
        BigInteger r = new BigInteger(bitLen - 1, new Random());
        //logger.info("r: {}", r );
        if (r.compareTo(n) >= 0 || r.signum() <= 0) {
            logger.info("r {} is not good.", r);
            System.exit(1);
        }
        //公钥（n, g）
        BigInteger b1 = g.modPow(m, nsquare);
        BigInteger b2 = r.modPow(n, nsquare);
        BigInteger b3 = b1.multiply(b2);
        return b3.mod(nsquare);
    }

    public static BigInteger decryption(BigInteger c) {
        //私钥（lambda, u）
        logger.info("start decrypt data.");
        //modInverse() 是 this的-1次方再 mod 参数
        BigInteger u = g.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).modInverse(n);
        return c.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).multiply(u).mod(n);
    }
}
