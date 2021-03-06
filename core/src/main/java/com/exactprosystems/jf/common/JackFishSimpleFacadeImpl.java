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
package com.exactprosystems.jf.common;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionsList;
import com.exactprosystems.jf.actions.app.ApplicationConnectTo;
import com.exactprosystems.jf.actions.app.ApplicationStart;
import com.exactprosystems.jf.actions.app.ApplicationStop;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.common.NullParameterException;
import com.exactprosystems.jf.common.report.ContextHelpBuilder;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.ConsoleDocumentFactory;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class JackFishSimpleFacadeImpl implements IJackFishSimpleFacade {

	private static final Parameter assertBool = new Parameter("assertBool", null);

	private final Logger logger = Logger.getLogger(this.getClass().getName() + "@" + Integer.toHexString(hashCode()));

	// allow to have only one instance of JF
	private static final JackFishSimpleFacadeImpl INSTANCE = new JackFishSimpleFacadeImpl();

	public static final synchronized IJackFishSimpleFacade getInstance() {
		return INSTANCE;
	}

	private final AtomicBoolean initialized = new AtomicBoolean(false);
	private Context ctx;
	private ReportBuilder report;

	private JackFishSimpleFacadeImpl() {
		// hide constructor
		try {
			this.report = new ContextHelpBuilder(new Date());
			this.report.itemStarted(new ActionItem()); // workaround
														// EmptyStackException
		} catch (IOException e) {
			// should never be here
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public synchronized void init(InputStream configStream) throws Exception {
		DocumentFactory factory = new ConsoleDocumentFactory(VerboseLevel.Errors);

		Configuration cfg = (Configuration) factory.createDocument(DocumentKind.CONFIGURATION, "config");

		factory.setConfiguration(cfg);

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(configStream))) {
			cfg.load(reader);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		if (!cfg.isValid()) {
			throw new RuntimeException(R.JF_SIMPLE_FACADE_INIT_EXCEPTION.get());
		}

		ctx = factory.createContext();

		initialized.set(true);
	}

	@Override
	public Object doAction(String connection, String actionName, Parameters params) throws Exception {
		if (!initialized.get()) {
			throw new IllegalStateException(R.JF_SIMPLE_FACADE_DO_ACTION_EXCEPTION.get());
		}
		if (actionName == null) {
			throw new NullParameterException(String.format(R.API_NULL_PARAMETER_EXCEPTION.get(), R.COMMON_ACTION_NAME.get()));
		}

		AbstractAction action = actionByName(actionName);
		action.initDefaultValues();
		action.correctParametersType(params);

		action.doAction(ctx, ctx.getEvaluator(), report, params, null, assertBool);

		if (action.getErrorKind() != null) {
			throw new RuntimeException(action.getErrorKind() + " - " + action.getReason());
		}

		return action.getOut();
	}

	private AbstractAction actionByName(String actionName) throws Exception {
		for (Class<?> type : ActionsList.actions) {
			if (type.getSimpleName().equals(actionName)) {
				return (AbstractAction) type.newInstance();
			}
		}
		throw new RuntimeException(String.format(R.JF_SIMPLE_FACADE_BY_NAME_EXCEPTION.get(), actionName));
	}

	@Override
	public String start(Parameters params) throws Exception {
		if (!initialized.get()) {
			throw new IllegalStateException(R.JF_SIMPLE_FACADE_START_EXCEPTION.get());
		}

		final String id = nextConnectionId();
		final ApplicationStart action = new ApplicationStart();
		action.initDefaultValues();
		action.correctParametersType(params);

		action.doAction(ctx, ctx.getEvaluator(), report, params, null, assertBool);

		if (action.getErrorKind() != null) {
			throw new RuntimeException(action.getErrorKind() + " - " + action.getReason());
		}

		final Object connection = action.getOut();

		ctx.getEvaluator().getLocals().set(id, connection);

		return id;
	}

	@Override
	public String connectTo(Parameters params) throws Exception {
		if (!initialized.get()) {
			throw new IllegalStateException(R.JF_SIMPLE_FACADE_CONNECT_TO_EXCEPTION.get());
		}

		final String id = nextConnectionId();
		final ApplicationConnectTo action = new ApplicationConnectTo();
		action.initDefaultValues();
		action.correctParametersType(params);

		action.doAction(ctx, ctx.getEvaluator(), report, params, null, assertBool);

		if (action.getErrorKind() != null) {
			throw new RuntimeException(action.getErrorKind() + " - " + action.getReason());
		}

		final Object connection = action.getOut();

		ctx.getEvaluator().getLocals().set(id, connection);

		return id;
	}

	@Override
	public void stop(String connectionId, Parameters params) throws Exception {
		if (!initialized.get()) {
			throw new IllegalStateException(R.JF_SIMPLE_FACADE_STOP_EXCEPTION.get());
		}

		final ApplicationStop action = new ApplicationStop();
		action.initDefaultValues();

		params.add("AppConnection", connectionId);

		action.correctParametersType(params);
		action.doAction(ctx, ctx.getEvaluator(), report, params, null, assertBool);

		ctx.getEvaluator().getLocals().delete(connectionId);
	}

	private String nextConnectionId() {
		Random rand = new Random();
		return 'x' + new BigInteger(130, rand).toString(32);
	}

}
