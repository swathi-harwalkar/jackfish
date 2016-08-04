////////////////////////////////////////////////////////////////////////////////
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.functions;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


public class RowTable implements Map<String, Object>
{
	public RowTable(Map<Header, Object> map)
	{
		if (map == null)
		{
			throw new NullPointerException("map");
		}
		
		this.source = map;
	}

	public RowTable()
	{
		this(new LinkedHashMap<Header, Object>());
	}
	



	@Override
	public int size()
	{
		return this.source.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.source.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return keySet().contains(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return this.source.containsValue(value);
	}

	@Override
	public Object get(Object key)
	{
		for (Map.Entry<Header, Object> entry : this.source.entrySet())
		{
			if (entry.getKey().name.equals(key))
			{
				return entry.getValue();
			}
		}
		
		return null;
	}

	@Override
	public Object put(String key, Object value)
	{
		for (Map.Entry<Header, Object> entry : this.source.entrySet())
		{
			if (entry.getKey().name.equals(key))
			{
				Object res = entry.getValue();
				entry.setValue(value);
				return res;
			}
		}

		return null;
	}

	@Override
	public Object remove(Object key)
	{
		return put(key == null ? null : key.toString(), null);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m)
	{
		for (Map.Entry<? extends String, ? extends Object> entry : m.entrySet())
		{
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void clear()
	{
		for (Map.Entry<Header, Object> entry : this.source.entrySet())
		{
			entry.setValue(null);
		}
	}

	@Override
	public Set<String> keySet()
	{
		Set<String> res = new LinkedHashSet<>();
		for (Header key : this.source.keySet())
		{
			res.add(key.name);
		}
		return res;
	}

	@Override
	public Collection<Object> values()
	{
		return this.source.values();
	}

	@Override
	public Set<Map.Entry<String, Object>> entrySet()
	{
		Map<String, Object> res = new LinkedHashMap<>();
		for (Map.Entry<Header, Object> entry : this.source.entrySet())
		{
			res.put(entry.getKey().name, entry.getValue());
		}
		return res.entrySet();
	}

	private Map<Header, Object> source;
}
