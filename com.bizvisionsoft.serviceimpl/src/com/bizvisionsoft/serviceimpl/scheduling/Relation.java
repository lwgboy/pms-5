package com.bizvisionsoft.serviceimpl.scheduling;

public class Relation {

	public static final int FTS = 0;

	public static final int STS = 1;

	public static final int STF = 2;

	public static final int FTF = 3;

	public int type;

	public float interval;

	public Relation(int type, float interval) {

		this.type = type;

		this.interval = interval;

	}

	public Relation() {

		this(FTS, 0);

	}

	@Override
	public String toString() {
		switch (type) {
		case FTS:
			return "FTS" + interval;
		case STS:
			return "STS" + interval;
		case STF:
			return "STF" + interval;
		case FTF:
			return "FTF" + interval;
		default:
			return "unknown";
		}
	}

}
