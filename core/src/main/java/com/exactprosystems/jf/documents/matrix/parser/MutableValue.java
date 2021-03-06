/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser;

import com.exactprosystems.jf.api.app.Mutable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The class for stored any value.
 */
public class MutableValue<T> implements Mutable, Consumer<T>, MutableListener<T>
{
	private T                value          = null;
	private boolean          changed        = false;
	private BiConsumer<T, T> changeListener = null;

	public MutableValue()
	{
		this.changed = false;
	}

	public MutableValue(T value)
	{
		this();
		this.value = value;
	}

	/**
	 * copy constructor
	 */
	public MutableValue(MutableValue<T> mutableValue)
	{
		this();
		this.value = mutableValue.value;
		this.changed = mutableValue.changed;
	}
	
	//region interface Consumer

	/**
	 * Accept the passed value and call listener
	 * @param value the new value, which will stored into the instance
	 */
	@Override
	public void accept(T value)
	{
		this.changed = this.changed || !Objects.equals(this.value, value);
		this.callListener(value);
		this.value = value;
	}
	//endregion

	//region interface Supplier

	/**
	 * @return the current stored value
	 */
	@Override
	public T get()
	{
		return this.value;
	}
	//endregion

	//region interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.changed;
	}

	@Override
	public void saved()
	{
		this.changed = false;
	}
	//endregion

	//region interface MutableListener
	@Override
	public void setOnChangeListener(BiConsumer<T, T> listener)
	{
		this.changeListener = listener;
	}
	//endregion

	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.value);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof MutableValue))
		{
			return false;
		}
		MutableValue<?> other = (MutableValue<?>) obj;
		return Objects.equals(this.value, other.value);
	}

	@Override
	public String toString()
	{
		return this.value == null ? "" : this.value.toString();
	}

	//region public methods

	/**
	 * Force call the change listener
	 */
	public void fire()
	{
		this.callListener(this.value);
	}

	/**
	 * @return true, if stored value is null or string representation of the value is empty
	 */
	public boolean isNullOrEmpty()
	{
		return this.value == null || ("" + this.value).isEmpty(); 
	}
	//endregion

	private void callListener(T value)
	{
		Optional.ofNullable(this.changeListener)
				.ifPresent(listener -> listener.accept(this.value, value));
	}
}
