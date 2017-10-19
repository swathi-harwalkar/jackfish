////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.clone;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.CredentialBean;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.ProgressMonitor;

import java.io.File;

public class GitClone
{
	private static final Logger logger = Logger.getLogger(GitClone.class);

	private GitCloneController controller;
	private String locationToCloningConfig = null;
	private Task<Void> task;
	private CredentialBean credentialBean;

	public GitClone(CredentialBean bean)
	{
		this.credentialBean = bean;
		this.controller = Common.loadController(this.getClass());
		this.controller.init(this);
	}

	public String display()
	{
		this.controller.show();
		return locationToCloningConfig;
	}

	public void cancel()
	{
		if (this.task == null || !this.task.isRunning())
		{
			this.controller.hide();
		}
		if (this.task != null && this.task.isRunning())
		{
			this.task.cancel();
			this.task = null;
		}
	}

	void cloneProject(String projectLocation, String uri, String projectName, boolean openProject, ProgressMonitor monitor)
	{
		this.locationToCloningConfig = null;
		this.controller.setDisable(true);
		File projectFolder = new File(projectLocation + File.separator + projectName);
		this.task = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				DialogsHelper.showInfo("Start cloning");
				GitUtil.gitClone(uri, projectFolder, credentialBean, monitor);
				return null;
			}
		};
		this.task.setOnSucceeded(e -> {
			this.controller.setDisable(false);
			DialogsHelper.showSuccess("Successful cloning repo " + uri);
			if (openProject)
			{
				locationToCloningConfig = projectFolder.getAbsolutePath();
			}
			this.controller.hide();
		});
		this.task.setOnCancelled(e -> {
			DialogsHelper.showInfo("Task canceled by user");
			this.controller.setDisable(false);
		});
		this.task.setOnFailed(e -> {
			Throwable exception = e.getSource().getException();
			logger.error(exception.getMessage(), exception);
			DialogsHelper.showError("Error on cloning repository " + exception.getMessage());
			this.controller.setDisable(false);
		});
		new Thread(this.task).start();
	}
}
