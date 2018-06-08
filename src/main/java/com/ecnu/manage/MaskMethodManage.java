package com.ecnu.manage;

import com.ecnu.utils.enums.MaskMethodEnum;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zou yuanyuan
 */
@Service
public class MaskMethodManage {
    public List<String> allMaskMethods() {
        List<String> maskMethod = new ArrayList<>();
        for (MaskMethodEnum maskMethodEnum : MaskMethodEnum.values()) {
            maskMethod.add(maskMethodEnum.getDesc());
        }
        return maskMethod;
    }
}
