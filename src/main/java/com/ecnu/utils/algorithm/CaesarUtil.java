package com.ecnu.utils.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * 可逆置换-凯撒
 * Caesar Substitution
 * @author asus zou yuanyuan on 2018/01/09
 */
public class CaesarUtil {
    /**
     * 针对List<String> col 执行基于凯撒置换的脱敏算法， k为偏移量。
     * @param col
     * @param k
     * @return
     */
    public static List<String> caesarCipher(List<String> col, int k) {
        try {
            List<String> resCol = new ArrayList<>();
            int colLen = col.size();
            for (int i = 0; i < colLen; i++) {
                resCol.add(col.get((k + i) % colLen));
            }
            return resCol;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}

