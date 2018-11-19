package com.bizvisionsoft.serviceimpl.renderer;


public class ColorTheme {

	public static BruiColor[] deepColor = new BruiColor[] { BruiColor.Indigo_900, BruiColor.Teal_900,
			BruiColor.cyan_900, BruiColor.deep_Purple_900, BruiColor.Red_900, BruiColor.Pink_900,
			BruiColor.Purple_900 };

	public enum BruiColor {

		Blue_Grey_100(0xcf, 0xd8, 0xdc),

		Blue_Grey_300(0x90, 0xa4, 0xae),

		Blue_Grey_500(0x60, 0x7d, 0x8b),

		Blue_Grey_600(0x54, 0x6e, 0x7a),

		Blue_Grey_700(0x45, 0x5a, 0x64),

		Blue_Grey_800(0x37, 0x47, 0x4f),

		Blue_Grey_900(0x26, 0x32, 0x38),
		
		Indigo_500(0x3f, 0x51, 0xb5),

		Indigo_900(0x1a, 0x23, 0x73),

		Indigo_700(0x30, 0x3f, 0x9f),

		Teal_50(0xe2, 0xf2, 0xf1), 

		Teal_500(0x00, 0x96, 0x88),

		Teal_900(0x00, 0x4d, 0x40),

		Teal_700(0x00, 0x79, 0x6b),

		Grey_1000(0, 0, 0),

		Grey_900(0x21, 0x21, 0x21),

		Grey_600(0x75, 0x75, 0x75),

		Grey_500(0x9e, 0x9e, 0x9e),

		Grey_400(0xbd, 0xbd, 0xbd),

		Grey_200(0xee, 0xee, 0xee),

		Grey_100(0xf5, 0xf5, 0xf5),

		Grey_50(0xfa, 0xfa, 0xfa),

		Red_900(0xb0, 0x12, 0x0a),

		Red_50(0xfd, 0xe0, 0xdc),

		Red_400(0xe8, 0x4e, 0x40),

		Pink_900(0x88, 0x0e, 0x4f),

		Purple_900(0x4a, 0x14, 0x8c),

		deep_Purple_900(0x31, 0x1b, 0x92),

		cyan_900(0x00, 0x60, 0x64),

		light_blue_500(0x03, 0xa9, 0xf4),

		brown_900(0x3e, 0x27, 0x23),

		orange_50(0xff, 0xb8, 0x00),
		
		white(0xff, 0xff, 0xff),


		;
		

		private int[] rgb;

		private BruiColor(int r, int g, int b) {
			rgb = new int[] {r, g, b};
		}

		@Override
		public String toString() {
			return getHtmlColor(rgb);
		}

		public int[] getRgb() {
			return rgb;
		}

	}


	public static String getHtmlColor(int[] rgb) {
		return "#" + hex(rgb[0]) + hex(rgb[1]) + hex(rgb[2]);
	}
	

	public static String getHTMLDarkColor(Object seed) {
		return getHtmlColor(ColorTheme.deepColor[seed.hashCode() % ColorTheme.deepColor.length].getRgb());
	}

	private static String hex(int color) {
		String i = Integer.toHexString(color);
		if (i.length() == 1) {
			return "0" + i;
		} else {
			return i;
		}
	}

}
