////////////////////////////////////////////////////////////////////////
//
// CommandProject.java
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

public class CommandProject extends CommandBlock {
	private int tabSize;
		
	public CommandProject(String name, int tabSize) {
		super(name);
		this.tabSize = tabSize;
	}
	
	public boolean readText(DocumentReader doc) {
		if (tabSize > 0) {
			DocumentReader expDoc = new DocumentReader(expandTabs(doc.getRange(), tabSize), doc.getOutputTree());
			return super.readText(expDoc);
		}
		else {
			return super.readText(doc);
		}
	}
	
	public boolean writeText(DocumentWriter doc) {
		if (hasNext())
			next.writeText(doc);
		return true;
	}
	
	public static String expandTabs(Range range, int tabSize) {
		StringBuffer result = new StringBuffer();
		int pos = 0;
		int pStart = range.start;
		for (int p = range.start; p != range.end; ++p) {
			if (range.charAt(p) == CR || range.charAt(p) == LF) {
				pos = 0;
			} else if (range.charAt(p) == TAB) {
				result.append(range.getContent().substring(pStart, p));
				for (int i = tabSize - (pos % tabSize); i > 0; --i)
					result.append(' ');
				pStart = p + 1;
				pos = 0;
			}
			else {
				pos++;
			}
		}
		result.append(range.getContent().substring(pStart, range.end));
		return result.toString();
	}
}
