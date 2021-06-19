////////////////////////////////////////////////////////////////////////
//
// DocumentWriter.java
//
// This file was generated by MapForce 2012r2sp1.
//
// YOU SHOULD NOT MODIFY THIS FILE, BECAUSE IT WILL BE
// OVERWRITTEN WHEN YOU RE-RUN CODE GENERATION.
//
// Refer to the MapForce Documentation for further details.
// http://www.altova.com/mapforce
//
////////////////////////////////////////////////////////////////////////

package com.altova.text.flex;

import com.altova.text.*;

public class DocumentWriter implements Appender {
	private ITextNode current;
	private StringBuffer content;
	
	public DocumentWriter(ITextNode tree, StringBuffer buff) {
		this.content = buff;
		this.current = tree;
	}
	
	public ITextNode getCurrentNode() {
		return current;
	}
	
	public void appendText(String text) {
		content.append(text);
	}
	
	public void appendText(StringBuffer text) {
		content.append(text.toString());
	}
}
