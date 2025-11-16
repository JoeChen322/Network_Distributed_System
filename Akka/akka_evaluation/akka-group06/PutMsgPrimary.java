package com.lab.evaluation25;

public class PutMsgPrimary {

	private String name;
	private String email;

	public PutMsgPrimary(String name, String email) {
		this.name = name;
		this.email = email;
	}

    public PutMsgPrimary(PutMsg putMsg) {
        this.name = putMsg.getName();
        this.email = putMsg.getEmail();
    }

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

}
