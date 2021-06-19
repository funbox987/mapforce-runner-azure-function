////////////////////////////////////////////////////////////////////////
//
// SubComposite.java
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

package com.altova.text.edi;

import java.io.IOException;
import com.altova.text.ITextNode;
import com.altova.text.Generator;

public class SubComposite extends StructureItem {
	public SubComposite (String name, Particle[] children) {
		super (name, ITextNode.SubComposite, children);
	}

	public boolean read (Parser.Context context) {
		context.getScanner().moveToNextSignificantChar();
		if (context.getScanner().isAtAnySeparator() &&
			!context.getScanner().isAtSeparator(ServiceChars.SubComponentSeparator))
				return false;


		Generator generator = context.getGenerator();
		generator.enterElement (context.getParticle().getName(), mNodeClass);
		if (!readChildren (context, ServiceChars.SubComponentSeparator))
		{
			generator.leaveElement (context.getParticle().getName());
			return false;
		}
		generator.leaveElement (context.getParticle().getName());
		return true;
	}

	public void write (Writer writer, ITextNode node, Particle particle)  throws IOException {
		writeChildren (writer, node, ServiceChars.SubComponentSeparator);
	}
}
