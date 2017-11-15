////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.tab;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public abstract class FileWatcher implements AutoCloseable
{
	private File file;
	private Timer timer;
	private long timeStamp;
	private boolean changed = false;
	
	public FileWatcher()
	{
		this.timeStamp = 0;
	}

	@Override
	public final void close()
	{
		if (this.timer != null)
		{
			this.timer.cancel();
			this.timer = null;
		}
	}
	
	public void saved(String fileName)
	{
		this.changed = false;
		if (this.timer != null)
		{
			this.timer.cancel();
		}
		if (fileName != null)
		{
			this.file = new File(fileName);
		}
		if (this.file != null)
		{
			this.timeStamp = this.file.lastModified();
			createTimerTask();
		}
	}
	
	public boolean isChanged()
	{
		return this.changed;
	}

	public abstract void onChanged();
	
	private void createTimerTask()
	{
		this.timer = new Timer();
		TimerTask timerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				if (file == null)
				{
					return;
				}
				
				long lastModified = file.lastModified();
				if (timeStamp != lastModified)
				{
					changed = true;
					onChanged();
				}
			}
		};
		this.timer.schedule(timerTask, new Date(), 1000);
	}

}
