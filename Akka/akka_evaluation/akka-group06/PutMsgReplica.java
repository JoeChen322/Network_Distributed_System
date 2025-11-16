package com.lab.evaluation25;

public class PutMsgReplica {

	private String name;
	private String email;

	public PutMsgReplica(String name, String email) {
		this.name = name;
		this.email = email;
	}

    public PutMsgReplica(PutMsg putMsg) {
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
