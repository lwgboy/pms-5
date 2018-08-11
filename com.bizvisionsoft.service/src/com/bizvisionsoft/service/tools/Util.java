package com.bizvisionsoft.service.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class Util {

	public static final String DATE_FORMAT_JS_FULL = "yyyy-MM-dd'T'HH:mm:ss.SSS Z";

	public static final String DATE_FORMAT_DATE = "yyyy-MM-dd";

	public static final String DATE_FORMAT_TIME = "HH:mm";

	public static final String DATE_FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";

	public static Date str_date(String str) {
		if (str == null)
			return null;
		String _str = str.replace("Z", " UTC");
		SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_JS_FULL);
		try {
			return format.parse(_str);
		} catch (ParseException e) {
			return null;
		}
	}

	public static boolean equals(Object v1, Object v2) {
		return v1 != null && v1.equals(v2) || v1 == null && v2 == null;
	}

	public static boolean isEmptyOrNull(String s) {
		return s == null || s.trim().isEmpty();
	}

	public static boolean notEmptyOrNull(String s, Consumer<String> c) {
		if (!isEmptyOrNull(s)) {
			if (c != null)
				c.accept(s);
			return true;
		} else {
			return false;
		}
	}

	public static boolean isEmptyOrNull(List<?> s) {
		return s == null || s.isEmpty();
	}

	public static int str_int(String text, String message) {
		if (isEmptyOrNull(text)) {
			return 0;
		}
		try {
			return Integer.parseInt(text.trim());
		} catch (Exception e) {
			throw new RuntimeException(message);
		}
	}

	public static double str_double(String text, String message) {
		if (isEmptyOrNull(text)) {
			return 0d;
		}
		try {
			return Double.parseDouble(text.trim());
		} catch (Exception e) {
			throw new RuntimeException(message);
		}
	}

	public static String getText(InputStream is) throws IOException {
		StringBuffer buffer = new StringBuffer();
		InputStreamReader isr = new InputStreamReader(is, "utf-8"); //$NON-NLS-1$
		Reader in = new BufferedReader(isr);
		int i;
		while ((i = in.read()) > -1) {
			buffer.append((char) i);
		}
		in.close();
		isr.close();
		return buffer.toString();
	}

	public static void writeFile(String content, String path, String encoding) throws IOException {
		File file = new File(path);
		file.delete();
		file.createNewFile();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
		writer.write(content);
		writer.close();
	}

	public static String readFile(String path, String encoding) throws IOException {
		String content = "";
		File file = new File(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
		String line = null;
		while ((line = reader.readLine()) != null) {
			content += line + "\n";
		}
		reader.close();
		return content;
	}
	
    public static boolean deleteFolder(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // �ж�Ŀ¼���ļ��Ƿ����
        if (!file.exists()) {  // �����ڷ��� false
            return flag;
        } else {
            // �ж��Ƿ�Ϊ�ļ�
            if (file.isFile()) {  // Ϊ�ļ�ʱ����ɾ���ļ�����
                return deleteFile(sPath);
            } else {  // ΪĿ¼ʱ����ɾ��Ŀ¼����
                return deleteDirectory(sPath);
            }
        }
    }
    
    public static boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // ·��Ϊ�ļ��Ҳ�Ϊ�������ɾ��
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }
    
    public static boolean deleteDirectory(String sPath) {
        //���sPath�����ļ��ָ�����β���Զ�����ļ��ָ���
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //���dir��Ӧ���ļ������ڣ����߲���һ��Ŀ¼�����˳�
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        //ɾ���ļ����µ������ļ�(������Ŀ¼)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            //ɾ�����ļ�
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } //ɾ����Ŀ¼
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //ɾ����ǰĿ¼
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

}
