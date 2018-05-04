package com.ecnu.utils.algorithm;

import com.idealista.fpe.FormatPreservingEncryption;
import com.idealista.fpe.builder.FormatPreservingEncryptionBuilder;

import java.security.NoSuchAlgorithmException;

/**
 * Format Preserving Encryption
 * @author asus
 */
public class FormatPreservingUtil {
    private static FormatPreservingEncryption formatPreservingEncryption;

    public static void setup() throws NoSuchAlgorithmException {
        // TODO: anyKey
        byte[] anyKey = new byte[]{
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
                (byte) 0x02, (byte) 0x02, (byte) 0x02, (byte) 0x02,
                (byte) 0x03, (byte) 0x03, (byte) 0x03, (byte) 0x03
        };

        formatPreservingEncryption = FormatPreservingEncryptionBuilder
                .ff1Implementation()
                .withDefaultDomain()
                .withDefaultPseudoRandomFunction(anyKey)
                .withDefaultLengthRange()
                .build();
    }

    /**
     * 保形加密的入口，目前只能处理小写字母的原始字符串
     * @param plainText
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String execute(String plainText) throws NoSuchAlgorithmException {
        // TODO: Capital letter & numbers
        return formatPreservingEncryption.encrypt(plainText, new byte[0]);
    }
}
