package com.bizvisionsoft.serviceimpl.commons;

public class NamedAccount {
	public String name;
	public String address;

	public NamedAccount(String name,String address) {
		this.name = name;
		this.address = address;
	}
	
	public NamedAccount(String address) {
		this.address = address;
	}
}