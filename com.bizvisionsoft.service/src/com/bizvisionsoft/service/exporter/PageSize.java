package com.bizvisionsoft.service.exporter;

public enum PageSize {

	A4(209, 296), //
	A3(296, 419), //
	A5(147, 209), //
	B5_JIS(181, 256), //
	K_8(259, 367), //
	K_16(183, 259), //
	K_16_BIG(209, 284), //
	K_32(129, 183), //
	K_32_BIG(139, 202), //
	ENVLOPE_3(124, 175), //
	ENVLOPE_6(119, 229), //
	ENVLOPE_9(228, 323), //
	ENVLOPE_10(104, 241), //
	ENVLOPE_B5(175, 249), //
	ENVLOPE_C5(161, 228), //
	ENVLOPE_DL(109, 219), //
	LETTER(215, 279), //
	SPFL(215, 335), //
	EXECUTIVE(183, 266);//

	private int width;

	private int height;

	private PageSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public int[] getSize(String orientation) {
		if ("∫·œÚ".equals(orientation)) {
			return new int[] { height, width };
		} else {
			return new int[] { width, height };
		}
	}

	public int[] getSize() {
		return new int[] { width, height };
	}
	
}
