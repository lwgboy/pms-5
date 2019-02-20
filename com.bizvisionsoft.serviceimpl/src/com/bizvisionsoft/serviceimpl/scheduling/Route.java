package com.bizvisionsoft.serviceimpl.scheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Route {

	public final List<Relation> relations;

	public final Task end1;

	public final Task end2;

	public final boolean dashed;

	public float LAG = -1;
	
	Route(Task end1, Task end2, boolean dashed, Relation... relations) {

		this.end1 = end1;

		this.end2 = end2;

		this.relations = new ArrayList<Relation>();
		this.relations.addAll(Arrays.asList(relations));

		this.dashed = dashed;
	}

	public Route(Task end1, Task end2, Relation... relations) {

		this(end1, end2, false, relations);
	}

	public Route(Task end1, Task end2, Relation relation) {

		this(end1, end2, false, relation);
	}

	public Route(Task end1, Task end2) {

		this(end1, end2, false, new Relation());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end1 == null) ? 0 : end1.hashCode());
		result = prime * result + ((end2 == null) ? 0 : end2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Route other = (Route) obj;
		if (end1 == null) {
			if (other.end1 != null)
				return false;
		} else if (!end1.equals(other.end1))
			return false;
		if (end2 == null) {
			if (other.end2 != null)
				return false;
		} else if (!end2.equals(other.end2))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Route [" + end1 + "-->" + end2 + "]";
	}

	public void updateLAG(float lag) {
		if (LAG == -1 || lag < LAG) {
			LAG = lag;
		}
	}
	
}
