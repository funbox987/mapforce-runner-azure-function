////////////////////////////////////////////////////////////////////////
//
// SingleCharElement.java
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

import com.altova.text.ITextNode;
import java.io.IOException;

public class SingleCharElement extends StructureItem {

	public SingleCharElement (String name) {
		super(name, ITextNode.DataElement);
	}

	public boolean read (Parser.Context context) {
		Scanner scanner = context.getScanner();
		if( scanner.isAtEnd() )
			return false;

		char c = scanner.rawConsumeChar();
		//MSH-1 in HL7 MODE needs special treatment
		if( context.getParser().getEDIKind() == EDISettings.EDIStandard.EDIHL7
			&& isHL7SpecialField(context.getParticle().getName(), "-1"))
		{
			scanner.mServiceChars.setDataElementSeparator( c);
			scanner.mServiceChars.setRepetitionSeparator( '\0');
		}
		
		context.getGenerator().insertElement (context.getParticle().getName(), c + "", mNodeClass);
		return true;
	}

	public void write (Writer writer, ITextNode node, Particle particle) throws IOException {
		if(writer.getEDIKind() == EDISettings.EDIStandard.EDIHL7 && isHL7SpecialField(node.getName(), "-1"))
			writer.write( "" + writer.getServiceChars().getDataElementSeparator());
		else
		{
			if( node.getValue().length() > 0)
				writer.write( node.getValue().substring(0, 1) );
		}
	}
}
