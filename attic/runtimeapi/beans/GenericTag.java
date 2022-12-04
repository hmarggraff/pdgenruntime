package org.pdgen.runtimeapi.beans;

import java.util.List;
import java.util.Properties;

public class GenericTag
{
	public String tag;
	public Properties attributes;
	public List<Object> content;

	public GenericTag(String tag)
	{
		this.tag = tag;
	}

	public GenericTag(String tag, Properties attributes)
	{
		this.tag = tag;
		this.attributes = attributes;
	}

	public GenericTag(String tag, Properties attributes, List<Object> content)
	{
		this.tag = tag;
		this.attributes = attributes;
		this.content = content;
	}

	public String getProperty(String s)
	{
		return attributes.getProperty(s);
	}

}
