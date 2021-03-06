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
package com.exactprosystems.jf.tool.git.pull;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.git.CredentialBean;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.git.merge.editor.MergeEditor;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.eclipse.jgit.lib.ProgressMonitor;

import java.util.List;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.tool.Common.logger;

public class GitPull
{
	private final Main model;

	private Service<List<GitPullBean>> service;
	private GitPullController controller;

	public GitPull(Main model) throws Exception
	{
		this.model = model;
		this.controller = Common.loadController(this.getClass().getResource("GitPull.fxml"));
		this.controller.init(this, this.model.getCredential());
		List<String> list = GitUtil.getBranches(this.model.getCredential())
				.stream()
				.filter(b -> !b.isLocal())
				.map(GitUtil.Branch::getSimpleName)
				.collect(Collectors.toList());
		String remoteBranch = GitUtil.getRemoteBranch(this.model.getCredential(), GitUtil.currentBranch(this.model.getCredential()));
		this.controller.displayBranches(list, remoteBranch);
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

	public void pull(ProgressMonitor monitor, String remoteBranchName) throws Exception
	{
		this.controller.startPulling();
		CredentialBean credential = this.model.getCredential();
		this.service = new Service<List<GitPullBean>>()
		{
			@Override
			protected Task<List<GitPullBean>> createTask()
			{
				return new Task<List<GitPullBean>>()
				{
					@Override
					protected List<GitPullBean> call() throws Exception
					{
						DialogsHelper.showInfo(R.GIT_PULL_START.get());
						return GitUtil.gitPull(credential, monitor, remoteBranchName);
					}
				};
			}
		};
		service.start();
		service.setOnSucceeded(e -> Common.tryCatch(() -> {
			DialogsHelper.showSuccess(R.GIT_PULL_SUCCESS.get());
			this.displayFiles(((List<GitPullBean>) e.getSource().getValue()));
			this.controller.endPulling(R.GIT_PULL_DONE.get());
		}, R.GIT_PULL_ERROR_ON_DISPLAY.get()));
		service.setOnCancelled(e -> {
			DialogsHelper.showInfo(R.GIT_PULL_CANCELED_BY_USER.get());
			this.controller.endPulling("");
		});
		service.setOnFailed(e -> {
			Throwable exception = e.getSource().getException();
			logger.error(exception.getMessage(), exception);
			DialogsHelper.showError(R.GIT_PULL_ERROR_ON_PULLING.get() + exception.getMessage());
			this.controller.endPulling(R.GIT_PULL_ERROR_ON_PULLING.get());
		});
	}

	public void display()
	{
		this.controller.show();
	}

	public void merge(GitPullBean item) throws Exception
	{
		MergeEditor mergeEditor = new MergeEditor(this.model, item.getFileName());
		mergeEditor.display();
		if (mergeEditor.isSuccessful())
		{
			item.resolve();
		}
	}

	private void displayFiles(List<GitPullBean> list) throws Exception
	{
		if (list.stream().anyMatch(GitPullBean::isNeedMerge))
		{
			List<String> strings = this.model.getMergeFiles();
			list.stream().filter(b -> strings.contains(b.getFileName())).forEach(GitPullBean::resolve);
		}
		this.controller.displayFiles(list);
	}
}
