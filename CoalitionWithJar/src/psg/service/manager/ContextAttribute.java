/*

* Copyright (c) 2007, National University of Singapore (NUS)
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without 
* modification, are permitted provided that the following conditions 
* are met:
*
*   * Redistributions of source code must retain the above copyright notice, 
*     this list of conditions,the authors and the following disclaimer.
*   * Redistributions in binary form must reproduce the above copyright notice,
*     this list of conditions,the authors and the following disclaimer in
*     the documentation and/or other materials provided with the distribution.
*   * Neither the name of the university nor the names of its 
*     contributors may be used to endorse or promote products derived from 
*     this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
* POSSIBILITY OF SUCH DAMAGE.
* 
* Author: Wenwei Xue (dcsxw@nus.edu.sg)
*
*/
package psg.service.manager;

import java.util.*;

/**
 * This class represents a context attribute in a domain and its current value in 
 * a simulated physical space.
 */
public class ContextAttribute {
	// data
	private final String DEFAULT_TYPE = "String"; // default data type of attribute
	private String name;	// name of the context attribute
	private String value;	// current value of the attribute	
	private String type;	// data type of the attribute
	
	// constructor
	public ContextAttribute(String name) {
		this.name = name.toLowerCase();
		this.type = DEFAULT_TYPE;
		this.value = null;
	}
	public ContextAttribute(String name, String value) {
		this.name = name.toLowerCase();
		this.type = DEFAULT_TYPE;
		this.value = value;
	}
	public ContextAttribute(String name, String type, String value) {
		this.name = name.toLowerCase();
		this.type = type;
		this.value = value;
	}
	// methods
	// accessors
	public String getName() {
		return this.name;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getValue() {
		return this.value;
	}
	
	// mutator
	public void setName(String name) {
		this.name = name.toLowerCase();
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	// verfier
	public boolean checkType(String type) {
		return this.type.equals(type);
	}
	
	public boolean filterValue(String op, String val) {
		int mark;
		if (op.equals(">"))
			mark = 0;
		else if (op.equals("<"))
			mark = 1;
		else if (op.equals(">="))
			mark = 2;
		else if (op.equals("<="))
			mark = 3;
		else if (op.equals("==") || op.equals("="))
			mark = 4;
		else if (op.equals("<>")||op.equals("!="))
			mark = 5;
		else {
//			System.err.println("Invalid comparsion operator: " + op);
			return false;
		}
		
//		System.out.println("[ContextAttribute.filterValue].mark: " + mark);
//		System.out.println("[ContextAttribute.filterValue].value: " + value);
//		System.out.println("[ContextAttribute.filterValue].value: " + val);
		
		switch (mark) {
		case 0: if (value.compareTo(val)>0) return true; break;
		case 1: if (value.compareTo(val)<0) return true; break;
		case 2: if (value.compareTo(val)>=0) return true; break;
		case 3: if (value.compareTo(val)<=0) return true; break;
		case 4: if (value.compareTo(val)==0) return true; break;
		case 5: if (value.compareTo(val)!=0) return true; break;
		default: return false;
		}
//		System.out.println("[ContextAttribute.filterValue].return: " + false);
		return false;
	}
	
}  // class