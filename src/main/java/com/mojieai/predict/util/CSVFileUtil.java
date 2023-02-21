package com.mojieai.predict.util;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.exception.BusinessException;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVFileUtil {
    private static Logger log = LogConstant.commonLog;

    public static List<String> readFile(String filePath, String charSet) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        return readFile(charSet, file);
    }

    public static List<String> readFile(String charSet, File file) {
        BufferedReader br = null;
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            br = new BufferedReader(new InputStreamReader(in, charSet));
        } catch (FileNotFoundException e) {
            log.error("解析文件不存在", e);
            throw new BusinessException("解析文件不存在");
        } catch (UnsupportedEncodingException e) {
            throw new BusinessException("不支持的编码格式");
        }
        String line = "";
        String everyLine = "";
        List<String> allString = new ArrayList<>();
        try {
            while ((line = br.readLine()) != null) {//读取到的内容给line变量
                everyLine = line;
                System.out.println(everyLine);
                allString.add(everyLine);
            }
        } catch (IOException e) {
            log.error("读取文件异常", e);
            throw new BusinessException("读取文件异常");
        }
        return allString;
    }
}
