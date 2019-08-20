package com.demo.fund.transfer.entity;

public class ErrorDetail {
	private String name;
	private String desc;
	
	public ErrorDetail() {
		
	}

	public ErrorDetail(String name, String desc) {
		super();
		this.name = name;
		this.desc = desc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public String toString() {
		return "ErrorDetail [name=" + name + ", desc=" + desc + "]";
	}
}
