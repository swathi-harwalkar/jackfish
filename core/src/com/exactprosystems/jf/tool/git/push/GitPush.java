/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.git.push;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.CredentialBean;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class GitPush
{
	private static final Logger logger = Logger.getLogger(GitPush.class);
	private final CredentialBean credential;

	private Main model;
	private GitPushController controller;

	private Service<Void> service;

	public GitPush(Main model) throws Exception
	{
		this.model = model;
		this.controller = Common.loadController(this.getClass().getResource("GitPush.fxml"));
		this.controller.init(this);
		this.credential = this.model.getCredential();
		this.controller.displayUnpushingCommits(GitUtil.gitUnpushingCommits(this.credential));
		this.controller.displayCurrentBranch(GitUtil.currentBranch(this.credential));
		String remoteBranch = GitUtil.getRemoteBranch(this.credential, GitUtil.currentBranch(credential));
		List<String> list = GitUtil.getBranches(this.credential)
				.stream()
				.filter(b -> !b.isLocal())
				.map(GitUtil.Branch::getSimpleName)
				.collect(Collectors.toList());
		this.controller.displayRemoteBranch(list, remoteBranch);
	}

	public void display()
	{
		this.controller.show();
	}

	public void close() throws Exception
	{
		if (this.service == null || !this.service.isRunning())
		{
			this.controller.hide();
		}
		if (this.service != null && this.service.isRunning())
		{
			this.service.cancel();
			this.service = null;
		}
	}

	void push(String remoteBranchName)
	{
		this.controller.setDisable(true);
		CredentialBean credential = model.getCredential();
		this.service = new Service<Void>()
		{
			@Override
			protected Task<Void> createTask()
			{
				return new Task<Void>()
				{
					@Override
					protected Void call() throws Exception
					{
						DialogsHelper.showInfo(R.GIT_PUSH_START.get());
						GitUtil.gitPush(credential, remoteBranchName);
						return null;
					}
				};
			}
		};
		service.start();
		service.setOnSucceeded(e -> {
			this.controller.setDisable(false);
			DialogsHelper.showSuccess(R.GIT_PUSH_SUCCESS.get());
			this.controller.hide();
		});
		service.setOnCancelled(e -> {
			DialogsHelper.showInfo(R.GIT_PUSH_CANCELED_BY_USER.get());
			this.controller.setDisable(false);
		});
		service.setOnFailed(e -> {
			Throwable exception = e.getSource().getException();
			logger.error(exception.getMessage(), exception);
			DialogsHelper.showError(String.format(R.GIT_PUSH_ERROR_ON_PUSHING.get(), exception.getMessage()));
			this.controller.setDisable(false);
		});
	}
}
