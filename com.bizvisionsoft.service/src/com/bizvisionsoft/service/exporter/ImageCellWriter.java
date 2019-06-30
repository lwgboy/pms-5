package com.bizvisionsoft.service.exporter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import org.apache.poi.bizvisionsoft.word.WordUtil;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public class ImageCellWriter extends CellWriter {

	private String[] value;
	private int width;

	public ImageCellWriter(ExportableFormField f, String[] value, int width) {
		super(f);
		this.value = value;
		this.width = width;
	}

	public void write(org.apache.poi.xwpf.usermodel.XWPFTableCell cell) {
		if (value != null) {
			String imgFile = value[1];
			int format = WordUtil.getImageFormat(imgFile);
			if (format == 0)
				return;// 格式不支持
			XWPFParagraph p = cell.getParagraphArray(0);
			XWPFRun pRun = p.createRun();
			InputStream is;
			try {
				URL url = new URL(value[0]);
				URLConnection connection = url.openConnection();
				connection.setDoOutput(true);
				is = connection.getInputStream();
				BufferedImage image = ImageIO.read(is);
				is.close();

				int srcWidth = image.getWidth(); // 源图宽度
				int srcHeight = image.getHeight(); // 源图高度
				float aspectRadio = 1f * srcHeight / srcWidth;// 获得纵横比

				double widthInPixel = WordUtil.mm2px(WordUtil.halfPt2mm(width));
				int widthInEMU = Units.toEMU(widthInPixel);
				double heightInPixel = (widthInPixel * aspectRadio);
				int heightInEMU = Units.toEMU(heightInPixel);

				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ImageIO.write(image, "png", os);
				is = new ByteArrayInputStream(os.toByteArray());
				pRun.addPicture(is, format, imgFile, widthInEMU, heightInEMU);
				is.close();
			} catch (Exception e) {
				logger.error("转换docx图片出错", e);
			}
		}
	}

}
