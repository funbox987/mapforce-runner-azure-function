////////////////////////////////////////////////////////////////////////
//
// EDIX12Settings.java
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

public class EDIX12Settings extends EDISettings {

	private String m_InterchangeControlVersionNumber = "05012";

	private boolean m_RequestAcknowledgement = true;

	public EDIX12Settings()
	{
		super.mEDIStandard = EDIStandard.EDIX12;
	}		
	
	public String getInterchangeControlVersionNumber() {
		return m_InterchangeControlVersionNumber;
	}

	public void setInterchangeControlVersionNumber(String rhs) {
		m_InterchangeControlVersionNumber = rhs;
	}

	public boolean getRequestAcknowledgement() {
		return m_RequestAcknowledgement;
	}

	public void setRequestAcknowledgement(boolean rhs) {
		m_RequestAcknowledgement = rhs;
	}
}
