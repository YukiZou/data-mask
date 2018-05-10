package com.ecnu.vo;

import com.ecnu.model.UserFile;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zou yuanyuan
 */
@Data
public class FileDataRecordVO implements Serializable {
    private int id;
    private String tableName;
    private String fileName;

    public FileDataRecordVO() {

    }

    public FileDataRecordVO(UserFile userFile) {
        this.id = userFile.getId();
        this.tableName = userFile.getTableName();
        this.fileName = userFile.getFileName();
    }
}
