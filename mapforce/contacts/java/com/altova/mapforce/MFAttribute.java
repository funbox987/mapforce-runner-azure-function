/**
 * MFAttribute.java
 *
 * This file was generated by MapForce 2012r2sp1.
 *
 * YOU SHOULD NOT MODIFY THIS FILE, BECAUSE IT WILL BE
 * OVERWRITTEN WHEN YOU RE-RUN CODE GENERATION.
 *
 * Refer to the MapForce Documentation for further details.
 * http://www.altova.com/mapforce
 */

package com.altova.mapforce;

import javax.xml.namespace.QName;

public class MFAttribute implements IMFNode 
{
	private QName qname;
	private String nodeName;
	IEnumerable children;
	
	public MFAttribute(String localName, String namespaceURI, String prefix, IEnumerable children)
	{
		this.qname = new QName(namespaceURI, localName, prefix);
		this.nodeName = prefix.equals("") ? localName : prefix + ":" + localName;
		this.children = children;
	}
	
	public MFAttribute(QName qname, IEnumerable children)
	{
		this.qname = qname;
		this.nodeName = qname.getPrefix().equals("") ? qname.getLocalPart() : qname.getPrefix() + ":" + qname.getLocalPart();
		this.children = children;
	}

	public MFAttribute(String nodename, IEnumerable children)
	{
		this.qname = new QName(nodename);
		this.nodeName = nodename;
		this.children = children;
	}
	
	public String getLocalName() 
	{
		return qname.getLocalPart();
	}

	public String getNamespaceURI() 
	{
		return qname.getNamespaceURI();
	}
	
	public String getPrefix()
	{
		return qname.getPrefix();
	}


	public String getNodeName()
	{
		return nodeName;
	}


	public int getNodeKind() 
	{
		return MFNodeKind_Attribute | MFNodeKind_Field;
	}

	public QName getQName() 
	{
		return qname;
	}

	public IEnumerable select(int mfQueryKind, Object query) 
	{
		switch (mfQueryKind)
		{
			case MFQueryKind_All:
			case MFQueryKind_AllChildren:
				return new MFNodeByKindFilter(children, MFNodeKind_Text);
			
			case MFQueryKind_AllAttributes:
			case MFQueryKind_AttributeByQName:
				return new MFEmptySequence();

			case MFQueryKind_ChildrenByQName:
				return new MFEmptySequence();

			case MFQueryKind_SelfByQName:
				if (qname.equals(query))
					return new MFSingletonSequence(this);
				else
					return new MFEmptySequence();

			default:
				throw new UnsupportedOperationException("Unsupported query type.");
		}
	}
	
	public String value() throws Exception
	{
		String s =  "";
				
		for (IEnumerator v = select(IMFNode.MFQueryKind_AllChildren, null).enumerator(); v.moveNext();)
		{
			Object o = v.current();
			if (o instanceof IMFNode)
				s += ((IMFNode) o ).value();
			else if (o instanceof javax.xml.namespace.QName)
				s += com.altova.CoreTypes.castToString((javax.xml.namespace.QName) o);
			else
				s += o.toString();
		}
		return s;
	}
	
	public javax.xml.namespace.QName qnameValue()
	{
		try
		{
			IEnumerator e = select(MFQueryKind_AllChildren, null).enumerator();
		
			if (!e.moveNext())
				throw new RuntimeException("Trying to convert NULL to QName.");
			
			javax.xml.namespace.QName q;
			
			if (e.current() instanceof IMFNode)
				q = ((IMFNode) e.current()).qnameValue();
			else
				q = (javax.xml.namespace.QName) e.current();
		
			if (e.moveNext())
				throw new RuntimeException("Trying to convert multiple values to QName.");
			
			return q;
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex.getMessage());
		}
	}
	
	public Object typedValue() throws Exception
	{
		return MFNode.collectTypedValue(select(IMFNode.MFQueryKind_AllChildren, null));
	}
}
