package cn.com.xiaofabo.scia.aiawardcheck.fileprocessor;

import java.math.BigInteger;

public class DocWriter {
	/// Size of A4 paper
	public static final BigInteger PAGE_A4_WIDTH = BigInteger.valueOf(11900L);
	public static final BigInteger PAGE_A4_HEIGHT = BigInteger.valueOf(16840L);

	/// Font size
	public static final int CN_FONT_SIZE_XIAO_YI = 24;
	public static final int CN_FONT_SIZE_ER = 22;
	public static final int CN_FONT_SIZE_XIAO_ER = 18;
	public static final int CN_FONT_SIZE_SAN = 16;
	public static final int CN_FONT_SIZE_XIAO_SAN = 15;
	public static final int CN_FONT_SIZE_SI = 14;
	public static final int CN_FONT_SIZE_XIAO_SI = 12;
	// public static final int CN_FONT_SIZE_WU = 10.5;
	public static final int CN_FONT_SIZE_XIAO_WU = 9;

	/// Font
	public static final String FONT_FAMILY_TIME_NEW_ROMAN = "Times New Roman";
	public static final String FONT_FAMILY_SONG = "宋体";
	public static final String FONT_FAMILY_FANGSONG = "仿宋_GB2312";
	public static final String FONT_FAMILY_HEITI = "黑体";
	public static final String FONT_FAMILY_KAITI = "楷体";
	
	/// Page margin
	public static final BigInteger PAGE_MARGIN_TOP = BigInteger.valueOf(2153L);
	public static final BigInteger PAGE_MARGIN_BOTTOM = BigInteger.valueOf(2493L);
	public static final BigInteger PAGE_MARGIN_LEFT = BigInteger.valueOf(1796L);
	public static final BigInteger PAGE_MARGIN_RIGHT = BigInteger.valueOf(1796L);
	public static final BigInteger PAGE_MARGIN_HEADER = BigInteger.valueOf(850L);
	public static final BigInteger PAGE_MARGIN_FOOTER = BigInteger.valueOf(2153L);

	/// Page settings
	public static final BigInteger PAGE_NUMBER_START = BigInteger.valueOf(0L);
	public static final BigInteger TEXT_LINE_SPACING = BigInteger.valueOf(500L);

	public static final BigInteger TABLE_KEY_WIDTH = BigInteger.valueOf(4111L);    /// ~4.0cm
	public static final BigInteger TABLE_VALUE_WIDTH = BigInteger.valueOf(4111L);  /// ~10.5cm

	/// Default font size
	public static final BigInteger DEFAULT_FONT_SIZE_HALF_16 = BigInteger.valueOf(32L);
	public static final BigInteger DEFAULT_FONT_SIZE_HALF_9 = BigInteger.valueOf(18L);
	
	public DocWriter() {
		
	}
}
