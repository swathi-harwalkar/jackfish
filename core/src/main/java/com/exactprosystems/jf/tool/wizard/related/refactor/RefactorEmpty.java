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

package com.exactprosystems.jf.tool.wizard.related.refactor;

import java.util.Collections;
import java.util.List;

import com.exactprosystems.jf.api.wizard.WizardCommand;

public class RefactorEmpty  extends Refactor
{
    private String message;
    
	public RefactorEmpty(String message)
	{
		this.message = message;
	}
	
	public List<WizardCommand> getCommands()
	{
		return Collections.emptyList();
	}
	
	@Override
	public String toString()
	{
		return this.message;
	}
}