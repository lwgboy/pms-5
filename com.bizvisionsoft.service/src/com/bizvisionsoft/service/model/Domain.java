package com.bizvisionsoft.service.model;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection("domain")
public class Domain {

	@ReadValue
	@WriteValue
	public String _id;

	@ReadValue
	@WriteValue
	public boolean activated;

	@ReadValue
	@WriteValue
	public String databaseUser;

	@ReadValue
	@WriteValue
	public String databasePassword;

	@ReadValue
	@WriteValue
	public boolean databaseSSLConnection;

	@ReadValue
	@WriteValue
	public String rootPath;

	@ReadValue
	@WriteValue
	public String site;

	@ReadValue
	@WriteValue
	public String enterpriseName;

	@ReadValue
	@WriteValue
	public String enterpriseEmailAddress;

}
