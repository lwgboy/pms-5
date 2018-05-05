package com.bizvisionsoft.service.model;

import com.bizvisionsoft.annotations.md.mongocodex.Persistence;
import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;

@PersistenceCollection(value = "user")
public class UserPassword {

	@ReadValue
	@WriteValue
	@Persistence
	private String password;

	@WriteValue("password2")
	private void setPassword2(String password) {
		if (this.password != null && !this.password.isEmpty() && !password.equals(this.password))
			throw new RuntimeException("两次输入的密码不一致。");
	}


}
