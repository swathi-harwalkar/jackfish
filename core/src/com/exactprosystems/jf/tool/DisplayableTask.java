////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.Optional;
import java.util.function.Consumer;

public class DisplayableTask<T> extends Service<T>
{
	private final ITask<T> mainTask;
	private Consumer<T> onSuccess;
	private Consumer<Throwable> onFailed;

	public interface ITask<T>
	{
		T apply() throws Exception;
	}

	public DisplayableTask(ITask<T> task)
	{
		this.mainTask = task;
	}

	public DisplayableTask onSuccess(Consumer<T> onSucceed)
	{
		this.onSuccess = onSucceed;
		return this;
	}

	public DisplayableTask onFailded(Consumer<Throwable> onFailed)
	{
		this.onFailed = onFailed;
		return this;
	}

	public Optional<Consumer<T>> getOnSuccess()
	{
		return Optional.ofNullable(this.onSuccess);
	}

	public Optional<Consumer<Throwable>> getOnFail()
	{
		return Optional.ofNullable(this.onFailed);
	}

	@Override
	protected Task<T> createTask()
	{
		return new Task<T>()
		{
			@Override
			protected T call() throws Exception
			{
				return mainTask.apply();
			}
		};
	}
}
