////////////////////////////////////////////////////////////////////////
//
// Segment.java
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

public class Segment extends StructureItem 	{
	public Segment (String name, String conditionPath, String conditionValue, Particle[] children) {
		super(name, ITextNode.Segment, conditionPath, conditionValue, children);
	}

	public boolean read (Parser.Context context) {
		Scanner scanner = context.getScanner();
		Scanner.State preserved = scanner.getCurrentState();

		// check if current segment starts here.
		if (!isSegmentStarting (context))
		{
			scanner.setCurrentState(preserved);
			if( context.getParticle().mMinOccurs > 0)
			{
				preserved = scanner.getCurrentState(); //reclone the state
				
				String sSeg = readSegmentTag( context );
				if( sSeg.length() > 0 )
				{
					if( !context.getParser().StandardSegments.contains( sSeg ) )
					{
						context.handleError(
							Parser.ErrorType.SegmentUnrecognized, 
							ErrorMessages.GetUnrecognizedSegmentMessage( sSeg),
							new ErrorPosition( preserved ),
							sSeg
						);
						return false;
					}
					else if( context.getParser().getCurrentMessageType() != null 
							&& !context.getParser().getMessage(context.getParser().getCurrentMessageType()).hasSegment( sSeg ) )
					{
						context.handleError(
							Parser.ErrorType.SegmentUnexpected,
							ErrorMessages.GetUnexpectedSegmentIDMessage( sSeg),
							new ErrorPosition( preserved ),
							sSeg
						);
						return false;
					}
				}
				scanner.setCurrentState(preserved);
			}
			return false;
		}
		
		context.mParser.resetDataElementPos(); //resets data element pos counter to 0
		context.mParser.resetComponentDataElementPos();

		switch( context.getParser().getEDIKind() )
		{
			case EDIFACT:
			{
				if( mName.equals("UNA") )
					return scanner.readUNA(); // read EDIFACT service string advice
			}
			break;
			
			case EDIX12:
			{
				if( mName.equals("ISA") )
				{
					// X12 ISA segment defines the data element separator here
					if (!scanner.readISASegmentStart())
						return false;
				}
				
				if( mName.equals("ST") )
				{
					//segments restarts
					context.getParser().resetCurrentSegmentPos();
					
					//increment transaction count
					context.getParser().incrementTransactionSetCount();
					
					// default that everything will be ok.
					context.getParser().setF717( 'A');
				}
				else if( mName.equals("GS") )
				{
					context.getParser().setF715( 'A');
				}
				else
				{
					//increase segment counter
					context.getParser().incrementCurrentSegmentPos();
				}
				
				if( mName.equals("LE") )
					context.getParser().setF447( null);
				
				if( mName.equals("SE") && ( context.getParser().getF717() == 'A' || context.getParser().getF717() == 'E' ) )
					context.getParser().incrementTransactionSetAccepted();
			}
			break;
		}
		
		// skip data element separator eventually following and do sanity checks
		if (scanner.isAtSeparator (ServiceChars.DataElementSeparator) ||
			context.getParser().getEDIKind() == EDISettings.EDIStandard.EDIFixed )
        {
			if( context.getParser().getEDIKind() != EDISettings.EDIStandard.EDIFixed )
			{
				//don't consume 1. MSH character this is the HL7 Field Separator
				if ( !(context.getParser().getEDIKind() == EDISettings.EDIStandard.EDIHL7 && isHL7SpecialSegment(mName)))
					scanner.rawConsumeChar();
			}
        }
		else if (!scanner.isAtSeparator (ServiceChars.SegmentTerminator))
			return false;		// invalid input character.

		context.getGenerator().enterElement (context.getParticle().getName(), mNodeClass);	// begin node construction

		context.getValidator().segment( this.mName);

		readChildren (context, ServiceChars.DataElementSeparator);

		if (mName.equals("ISA") &&
            context.getParser().getEDIKind() == EDISettings.EDIStandard.EDIX12)  // X12 ISA segment defines the segment terminator here
		{
			if (!scanner.readISASegmentEnd())
			{
				context.getGenerator().leaveElement (context.getParticle().getName());
				return false;
			}

			ITextNode fi15 = context.getGenerator().getCurrentNode().getChildren().getFirstNodeByName("FI15");
			if (fi15 != null  && fi15.getValue().length() != 0)
				context.getScanner().getServiceChars().setComponentSeparator(fi15.getValue().charAt(0));

			ITextNode fi65 = context.getGenerator().getCurrentNode().getChildren().getFirstNodeByName("FI65");
			if (fi65 != null && fi65.getValue().length() != 0)
			{
				context.getScanner().getServiceChars().setRepetitionSeparator(fi65.getValue().charAt(0));
				if (Character.isLetterOrDigit(context.getScanner().getServiceChars().getRepetitionSeparator()))
					context.getScanner().getServiceChars().setRepetitionSeparator('\0');
			}
		}

		if (!scanner.isAtSeparator(ServiceChars.SegmentTerminator))
		{
			Scanner.State beforeRead = scanner.getCurrentState();
			String sExtraContent = scanner.forwardToSegmentTerminator();
			if ( sExtraContent.length() > 0)
				context.handleError(
					Parser.ErrorType.ExtraData,
					ErrorMessages.GetExtraDataMessage(context.getParticle().getName(), sExtraContent),
					new ErrorPosition( beforeRead )
				);
			
			if ( scanner.isAtEnd())
				context.handleError(
					Parser.ErrorType.UnexpectedEndOfFile,
					ErrorMessages.GetUnexpectedEndOfFileMessage(),
					new ErrorPosition( beforeRead )
				);
		}

		scanner.rawConsumeChar();
		context.getGenerator().leaveElement (context.getParticle().getName());
		return true;
	}

	public void write (Writer writer, ITextNode node, Particle particle) throws IOException {
		// write out name and separator
		writer.write (mName);
		writer.getValidator().segment(mName);

		// even this could be omitted according to spec:
        if( !(writer.getEDIKind() == EDISettings.EDIStandard.EDIHL7 && isHL7SpecialSegment(mName)))
            writer.addSeparator (ServiceChars.DataElementSeparator);

		writeChildren (writer, node, ServiceChars.DataElementSeparator);
		// now no superfluous separators are left, therefore write the segment terminator.
		writer.clearPendingSeparators ();
		writer.addSeparator (ServiceChars.SegmentTerminator);

		if (writer.getNewlineAfterSegments())
			writer.write ("\r\n");
		else
			writer.writePendingSeparators(); // flushes out the segment terminator, so it won't be lost.
	}
}
