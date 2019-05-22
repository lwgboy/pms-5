package com.bizvisionsoft.service.model;

import org.bson.Document;

import com.bizvisionsoft.annotations.md.mongocodex.PersistenceCollection;
import com.bizvisionsoft.annotations.md.service.ReadValue;
import com.bizvisionsoft.annotations.md.service.WriteValue;
import com.bizvisionsoft.service.CommonService;
import com.bizvisionsoft.service.ServicesLoader;
import com.bizvisionsoft.service.tools.Check;

@PersistenceCollection(value = "user")
public class UserPassword {

	@ReadValue
	@WriteValue
	public String password;

	public boolean changePSW = false;

	@WriteValue("password2")
	public void setPassword2(String password) {
		if (this.password != null && !this.password.isEmpty() && !password.equals(this.password))
			throw new RuntimeException("������������벻һ�¡�");
		checkPassword();
	}

	private void checkPassword() {
		Document setting = ServicesLoader.get(CommonService.class).getSetting("�����û�����Ҫ��");
		if (setting == null || password == null)
			return;
		if (!password.matches(setting.getString("passwordRequest")))
			throw new RuntimeException(Check.option(setting.getString("desc")).orElse("����������Ҫ��"));
	}

}
