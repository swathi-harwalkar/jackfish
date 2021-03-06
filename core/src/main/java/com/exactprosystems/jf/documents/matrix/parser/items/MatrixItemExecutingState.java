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

package com.exactprosystems.jf.documents.matrix.parser.items;

/**
 * Executing state enum for a item
 * Use this enum for indicate process on a GUI
 */
public enum MatrixItemExecutingState
{
	None,
	/**
	 * Used for display, that a item executing was failed
 	 */
	Failed,
	/**
	 * Used for display, that a item executing was passed
	 */
	Passed,
	/**
	 * Used for display, that a item is executing now
	 */
	Executing
}
