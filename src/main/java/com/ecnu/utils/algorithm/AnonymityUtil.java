package com.ecnu.utils.algorithm;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * k-匿名算法
 * @author wang hao
 */
public class AnonymityUtil {

    /**
     * 调用k-匿名算法的入口，只能处理数值类型的数据列
     * 转换类型  ["1","2","3"] -> [["1"],["2"],["3"]]
     */
    public static List<String> anonymityIntNumPrepare(List<String> lineArray, int k) throws Exception {
        List<String[]> list = new ArrayList<>();
        for (int i = 0; i < lineArray.size(); i++) {
            String[] arr = new String[]{lineArray.get(i)};
            list.add(arr);
        }
        return anonymityIntNum(list, 0, k);
    }

    private static List<String> anonymityIntNum(List<String[]> lineArray, int lineNum, int k) throws Exception {
        // 用来处理的List
        List<Item> lineNumArray = new ArrayList<>();
        // 存放结果List
        List<Item> resultList = new ArrayList<>();

        // 将字符串存入item对象
        for (int i = 0; i < lineArray.size(); i++) {
            String[] strArray = lineArray.get(i);
            Item item = new Item();
            item.num = i;
            item.lineValue = Float.valueOf(strArray[lineNum]);
            item.line = strArray[lineNum];
            lineNumArray.add(item);
        }
        int size = lineNumArray.size();
        int tripleK = 3 * k;
        int doubleK = 2 * k;
        //  行数是否>3*k
        while ( size > tripleK) {
            // 求出T的中心  x_average
            float sum = 0;
            for (int i = 0; i < lineNumArray.size(); i++) {
                Item item = lineNumArray.get(i);
                sum += item.lineValue;
            }
            float x_average = sum / lineNumArray.size();
            // 求出离x最远的点 r
            int r = farthest(lineNumArray, x_average);
            Item item_r = lineNumArray.get(r);
            // 找到r最近的k个点(包括r)记录为一个等价类 并从T（lineArray）中删除
            List<Item> min = nearest(lineNumArray, lineNumArray.get(r).lineValue, k);
            // 导出一个等价类
            // 泛化敏感属性
            List<Item> kList = generalize(lineNumArray, min, lineNum, k);
            resultList.addAll(kList);
            //，将其从T中删除
            lineNumArray.removeAll(min);
            // 求出离r最远s的点s
            int s = farthest(lineNumArray, item_r.lineValue);
            // 找到s最近的k-1个点记录为一个等价类 并从T（lineArray）中删除
            List<Item> min_s = nearest(lineNumArray, lineNumArray.get(s).lineValue, k);

            // 导出一个等价类
            // 泛化敏感属性
            List<Item> kList_s = generalize(lineNumArray, min_s, lineNum, k);
            resultList.addAll(kList_s);
            // 将其从T中删除
            lineNumArray.removeAll(min_s);
        }

        // 行数>2k 行数<3k
        while (size >= doubleK) {
            // 求出剩余T的中心  x_average
            float sum = 0;
            for (int i = 0; i < lineNumArray.size(); i++) {
                Item item = lineNumArray.get(i);
                sum += item.lineValue;
            }
            float x_average = sum / lineNumArray.size();

            // 找出离x最远的r
            int r = farthest(lineNumArray, x_average);

            // 以r为中心找出k-1个最近的记录为一个等价类
            List<Item> min = nearest(lineNumArray, x_average, k);

            // 导出一个等价类，将其从T中删除
            // 泛化敏感属性

            List<Item> kList = generalize(lineNumArray, min, lineNum, k);
            resultList.addAll(kList);
            //，将其从T中删除
            lineNumArray.removeAll(min);

        }
        // 将剩余的T作为一个等价类
        List<Item> kList = generalize(lineNumArray, null, lineNum, lineNumArray.size());
        resultList.addAll(kList);
        // 将其从T中删除
        lineNumArray.removeAll(kList);

        // 按照num排序
        Collections.sort(resultList, (o1, o2) -> {
            int i = o1.num - o2.num;
            if (i == 0) {
                return 0;
            }
            return i;
        });

        // 返回匿名的列信息
        List<String> resultLineList = new ArrayList<>();
        for (int i = 0; i < resultList.size(); i++) {
            Item item = resultList.get(i);
            float value = (float) (Math.round(item.maskedColumn * 100)) / 100;
            resultLineList.add(String.valueOf(value));
        }
        return resultLineList;
    }

    /**
     * 求出最远的点r
     */
    private static int farthest(List<Item> list, float x_average) {
        float max_distance = 0;
        int r = 0;
        for (int i = 0; i < list.size(); i++) {
            Item item = list.get(i);
            float temp = Math.abs(item.lineValue - x_average);
            if (max_distance < temp) {
                max_distance = temp;
                r = i;
            }
        }
        return r;
    }

    /**
     * 求出minNum个最近的点r
      */
    private static List<Item> nearest(List<Item> list, float x_average, int minNum) {

        float min_distance = 0;
        List<Item> tempList = new ArrayList<>(list);
        List<Item> minNumArray = new ArrayList<>();

        // 计算出templist的distance
        for (Item item : tempList) {
            item.distance = Math.abs(item.lineValue - x_average);
        }

        // 对距离进行排序
        Collections.sort(tempList);

        // 取出前minNum个
        for (int i = 0; i < minNum; i++) {
            Item item = tempList.get(i);
            minNumArray.add(item);
        }

        return minNumArray;
    }


    /**
     * 对k个等价类进行泛化  gnum: 泛化的敏感列
     */
    private static List<Item> generalize(List<Item> lineNumArray, List<Item> minItem, int gnum, int k) {

        // 泛化敏感属性
        float[] value_generalize = new float[k];
        if (minItem == null) {
            minItem = lineNumArray;
        }
        for (int i = 0; i < minItem.size(); i++) {
            Item item = minItem.get(i);
            value_generalize[i] = item.lineValue;
        }
        // 计算聚类的平均值，代替原本item的敏感数据

        for (int i = 0; i < minItem.size(); i++) {
            Item item = minItem.get(i);
            item.lineValueArray = value_generalize;
            float sum = 0;
            item.maskedColumnStr = "";

            float min = item.lineValueArray[0];
            float max = item.lineValueArray[0];
            for (int j = 0; j < item.lineValueArray.length; j++) {
                if (item.lineValueArray[j] > max) {
                    max = item.lineValueArray[j];
                }
                if (item.lineValueArray[j] < min) {
                    min = item.lineValueArray[j];
                }
                sum += item.lineValueArray[j];
                String valueStr = String.valueOf(item.lineValueArray[j]);
                item.maskedColumnStr = item.maskedColumnStr.concat(",").concat(valueStr);
            }
            item.maskedColumn = sum / item.lineValueArray.length;
            item.maskedColumnStr = "[".concat(String.valueOf(min)).concat("-").concat(String.valueOf(max)).concat("]");

        }
        return minItem;
    }

}

/**
 * 一行属性   line：原字符串
 */
class Item implements Comparable<Item> {
    int num;
    /**
     * 要匿名的列的num
     */
    int sensiveColumn;
    /**
     * 泛化的值
     */
    float lineValue;
    /**
     * 聚类在一个数组
     */
    float[] lineValueArray;
    /**
     * 原始行
     */
    String line;
    float distance;
    /**
     * 匿名后的列
     */
    float maskedColumn;
    /**
     * 匿名后的列
     */
    String maskedColumnStr;

    @Override
    public String toString() {
        String valueStr = "";
        // 组合敏感属性
        for (int i = 0; i < lineValueArray.length; i++) {
            valueStr = valueStr.concat(String.valueOf(lineValueArray[i]));
            if (i != lineValueArray.length - 1) {
                valueStr = valueStr.concat("、");
            }
        }
        String[] stringArray = line.split(",");
        stringArray[sensiveColumn] = valueStr;

        line = "";
        // 组合整个line
        for (int i = 0; i < stringArray.length; i++) {
            line = line.concat(String.valueOf(stringArray[i]));
            if (i != stringArray.length - 1) {
                line = line.concat(",");
            }
        }
        line.concat("," + maskedColumn);

        return line;
    }

    /**
     * 比较距离
     */
    @Override
    public int compareTo(Item o) {
        if (this.distance < o.distance) {
            return -1;
        } else if (this.distance == o.distance) {
            return 0;
        } else {
            return 1;
        }
    }
}