package com.bizvisionsoft.service.model;

import java.util.Optional;

import org.bson.types.ObjectId;

import com.bizvisionsoft.annotations.md.mongocodex.Exclude;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.Label;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.ProductService;
import com.bizvisionsoft.service.ServicesLoader;

@PersistenceCollection("product")
public class Product {

	@ReadValue
	@WriteValue
	private ObjectId _id;

	@ReadValue
	@WriteValue
	private String id;

	@ReadValue
	@WriteValue
	private String name;

	@ReadValue
	@WriteValue
	private String series;

	@ReadValue
	@WriteValue
	private String sellPoint;

	@ReadValue
	@WriteValue
	private String position;

	@ReadValue
	@WriteValue
	private ObjectId project_id;

	@ReadValue(ReadValue.TYPE)
	@Exclude
	private String typeName = "²úÆ·";

	@ReadValue("benchmarking")
	private Product readBenchmarking() {
		return Optional.ofNullable(benchmarking_id).map(_id -> ServicesLoader.get(ProductService.class).get(_id))
				.orElse(null);
	}
	
	@WriteValue("benchmarking")
	private void writeBenchMarking(Product product) {
		benchmarking_id = Optional.ofNullable(product).map(p->p._id).orElse(null);
	}

	private ObjectId benchmarking_id;

	public Product setProject_id(ObjectId project_id) {
		this.project_id = project_id;
		return this;
	}

	@Override
	@Label
	public String toString() {
		return name + " [" + id + "]";
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
