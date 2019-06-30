package com.bizvisionsoft.service.exporter;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum BorderTypes {

	NIL, NONE, SINGLE, THICK, DOUBLE, DOTTED, DASHED, DOT_DASH, DOT_DOT_DASH, TRIPLE, THIN_THICK_SMALL_GAP, THICK_THIN_SMALL_GAP, THIN_THICK_THIN_SMALL_GAP, THIN_THICK_MEDIUM_GAP, THICK_THIN_MEDIUM_GAP, THIN_THICK_THIN_MEDIUM_GAP, THIN_THICK_LARGE_GAP, THICK_THIN_LARGE_GAP, THIN_THICK_THIN_LARGE_GAP, WAVE, DOUBLE_WAVE, DASH_SMALL_GAP, DASH_DOT_STROKED, THREE_D_EMBOSS, THREE_D_ENGRAVE, OUTSET, INSET;

	public static String[] names() {
		return Stream.of(values()).map(e -> e.name()).collect(Collectors.toList()).toArray(new String[0]);
	}

}
