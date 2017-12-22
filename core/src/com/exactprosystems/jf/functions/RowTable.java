////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.functions;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.common.RowExpiredException;
import com.exactprosystems.jf.api.error.common.WrongExpressionException;

import java.util.*;
import java.util.stream.Collectors;


public class RowTable implements Map<String, Object>, Cloneable
{
	private       Map<Header, Object> currentRow;
	private final Table               table;
	private final int                 index;

	public RowTable(Table table, int index)
	{
		this.table = table;
		this.index = index;
		this.currentRow = null;
	}

	public int index()
	{
		return this.index;
	}

	@Override
	public String toString()
	{
		this.checkRow();
		return this.currentRow.toString();
	}

	public CopyRowTable copy(Set<String> names, boolean compareValues)
	{
		this.checkRow();
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		this.currentRow.forEach((key, value) -> {
			if (key != null && names.contains(key.name))
			{
				map.put(key.name, compareValues ? this.get(key.name) : Str.asString(value));
			}
		});
		return new CopyRowTable(map);
	}

	public CopyRowTable copy()
	{
		this.checkRow();
		return new CopyRowTable(this.asLinkedMap());
	}

	public static LinkedHashMap<String, Object> asLinkedMap(Map<Header, Object> map)
	{
		return map.entrySet()
				.stream()
				.collect(Collectors.toMap(e -> e.getKey().name, e -> Str.asString(e.getValue()), (k, v) -> k, LinkedHashMap::new));
	}

	//region Interface Map
	@Override
	public int size()
	{
		this.checkRow();
		return this.currentRow.size();
	}

	@Override
	public boolean isEmpty()
	{
		this.checkRow();
		return this.currentRow.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		this.checkRow();
		return this.keySet().contains(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		this.checkRow();
		return this.currentRow.containsValue(value);
	}

	@Override
	public Object get(Object key)
	{
		this.checkRow();
		Header header = this.table.headerByName(Str.asString(key));
		if (header == null)
		{
			throw new WrongExpressionException(String.format(R.ROW_TABLE_HEADER_NOT_FOUND.get(), key));
		}
		Object value = this.currentRow.get(header);
		value = this.table.convertCell(this.currentRow, header, value, null);
		if (value instanceof Exception)
		{
			Exception e = (Exception) value;
			throw new WrongExpressionException(e.getMessage());
		}
		return value;
	}

	@Override
	public Object put(String key, Object value)
	{
		this.checkRow();
		return this.table.setValue(index, key, value);
	}

	@Override
	public Object remove(Object key)
	{
		this.checkRow();
		return this.put(key == null ? null : key.toString(), null);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m)
	{
		this.checkRow();
		m.forEach(this::put);
	}

	@Override
	public void clear()
	{
		this.checkRow();
		this.currentRow.entrySet().forEach(entry -> entry.setValue(null));
	}

	@Override
	public Set<String> keySet()
	{
		this.checkRow();
		return this.currentRow.keySet().stream()
				.filter(Objects::nonNull)
				.map(header -> header.name)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Override
	public Collection<Object> values()
	{
		this.checkRow();
		return this.currentRow.values();
	}

	@Override
	public Set<Map.Entry<String, Object>> entrySet()
	{
		this.checkRow();
		return this.currentRow.entrySet().stream()
				.filter(entry -> entry.getKey() != null)
				.collect(Collectors.toMap(entry -> entry.getKey().name, Entry::getValue, (a, b) -> b, LinkedHashMap::new))
				.entrySet();
	}

	//endregion

	//region private methods
	private void checkRow() throws RowExpiredException
	{
		if (this.currentRow == null)
		{
			this.currentRow = this.table.getInner(this.index);
			return;
		}
		if (this.table.getInner(this.index) != this.currentRow)
		{
			throw new RowExpiredException("Expired");
		}
	}

	private LinkedHashMap<String, Object> asLinkedMap()
	{
		this.checkRow();
		return asLinkedMap(this.currentRow);
	}
	//endregion
}
