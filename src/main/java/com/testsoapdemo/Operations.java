package com.testsoapdemo;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public class Operations {
	@WebMethod
	public int add(int a,int b) {
		return a+b;
		
	}

	@WebMethod
	public int sub(int a,int b) {
		return a-b;
		
	}
}