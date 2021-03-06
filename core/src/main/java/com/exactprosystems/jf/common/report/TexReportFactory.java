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

package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.common.documentation.TexReportBuilder;

import java.io.IOException;
import java.util.Date;

public class TexReportFactory extends ReportFactory
{
	@Override
	public ReportBuilder createReportBuilder(String outputPath, String matrixName, Date currentTime) throws IOException
	{
		ReportBuilder result = new TexReportBuilder(outputPath, matrixName, currentTime);
		result.init(new FileReportWriter(result.generateReportName(outputPath, matrixName, ReportBuilder.SUFFIX, currentTime)));
		return result;
	}
}
