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

import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.Locales;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.matrix.parser.ScreenshotKind;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.MutableArrayList;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.newconfig.CompareEnum;
import com.exactprosystems.jf.tool.settings.Theme;
import com.exactprosystems.jf.tool.wizard.WizardSettings;
import javafx.scene.input.KeyCombination;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XmlRootElement(name = "settings")
@XmlAccessorType(XmlAccessType.NONE)
public class Settings
{
	private static final Logger logger = Logger.getLogger(Settings.class);
	private static Comparator<SettingsValue> comparator = Comparator.comparingLong(sv -> sv.getTime().getTime());

	public static final String SETTINGS_PATH	= ".settings.xml";

	public static final String	FONT			= "Font";
	public static final String	SETTINGS 		= "Main";
	public static final String	LOGS_NAME		= "Logs";
	public static final String	SHORTCUTS_NAME	= "Shortcuts";
	public static final String	MATRIX_COLORS	= "MatrixColors";
	public static final String	GIT				= "Git";
	public static final String	MATRIX_NAME		= "Matrix";
	public static final String	WIZARD_NAME		= "Wizard";

	//region Shortcuts
	//other shortcuts
	public static final String SHOW_ALL_TABS	= "ShowAllTabs";
	public static final String SEARCH			= "Search";

	//document shortcuts
	public static final String SAVE_DOCUMENT	= "SaveDocument";
	public static final String SAVE_DOCUMENT_AS	= "SaveDocumentAs";
	public static final String UNDO				= "Undo";
	public static final String REDO				= "Redo";

	//matrix navigation shortcuts
	public static final String ADD_ITEMS		= "Add";
	public static final String ALL_PARAMETERS	= "AllParameters";
	public static final String BREAK_POINT		= "BreakPoint";
	public static final String ADD_PARAMETER	= "AddParameter";
	public static final String HELP				= "Help";
	public static final String GO_TO_LINE		= "GoToLine";
	public static final String SHOW_ALL			= "ShowAll";
	public static final String DELETE_ITEM		= "DeleteItem";
	public static final String COPY_ITEMS		= "CopyItems";
	public static final String CUT_ITEMS		= "CutItems";
	public static final String PASTE_ITEMS		= "PasteItems";

	//matrix actions shortcuts
	public static final String START_MATRIX		= "StartMatrix";
	public static final String STOP_MATRIX		= "StopMatrix";
	public static final String PAUSE_MATRIX		= "PauseMatrix";
	public static final String STEP_MATRIX		= "StepMatrix";
	public static final String SHOW_RESULT		= "ShowResult";
	public static final String SHOW_WATCH		= "ShowWatch";
	public static final String TRACING			= "Tracing";
	public static final String FIND_ON_MATRIX	= "FindOnMatrix";
	//endregion

	//region Main
	public static final String	MAX_LAST_COUNT = "maxFilesCount";
	public static final String	TIME_NOTIFICATION = "timeNotification";
	public static final String	THEME = "theme";
	public static final String	USE_EXTERNAL_REPORT_VIEWER = "useExternalReportViewer";
	public static final String	USE_FULL_SCREEN	= "useFullScreen";
	public static final String	USE_FULLSCREEN_XPATH = "useFullScreenXpath";
	public static final String	COPYRIGHT = "copyright";
	public static final String	LANGUAGE = "language";
	//endregion

	//region Logs
	public static final String	ALL = "ALL";
	public static final String	DEBUG = "DEBUG";
	public static final String	ERROR = "ERROR";
	public static final String	FATAL = "FATAL";
	public static final String	INFO = "INFO";
	public static final String	TRACE = "TRACE";
	public static final String	WARN = "WARN";
	//endregion

	//region Git
	//git
	public static final String GIT_SSH_IDENTITY	= "gitSshIdentity";
	public static final String GIT_KNOWN_HOST	= "gitKnownHost";
	//endregion

	//region Matrix
	public static final String MATRIX_DEFAULT_SCREENSHOT = "matrixDefaultScreenshot";
	public static final String MATRIX_POPUPS             = "matrixPopups";
	public static final String MATRIX_FOLD_ITEMS         = "foldNewItems";
	public static final String MATRIX_OPEN_REPORT_AFTER_FINISHED = "matrixOpenReportAfterFinished";
	//endregion

	public static final String	THRESHOLD			= "threshold";
	public static final String	MAX					= "_MAX";
	public static final String	MIN					= "_MIN";

	public static final String 	OPENED 				= "OPENED";
	public static final String 	MAIN_NS 			= "MAIN";
	public static final String	MATRIX_TOOLBAR		= "MATRIX_TOOLBAR";

	//region Search
	public static final String TEXT = "searchText";
	public static final String MASK = "fileMask";
	//endregion

	private static final Class<?>[] jaxbContextClasses = { Settings.class, SettingsValue.class };

	static
	{
		if (!new File(SETTINGS_PATH).exists())
		{
			try
			{
				defaultSettings().save(SETTINGS_PATH);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public static final String GLOBAL_NS	= "GLOBAL";
	public static final String APPLICATION	= "APP_";
	public static final String SERVICE 		= "SRV_";
	public static final String CLIENT 		= "CLN_";
	public static final String SQL			= "SQL_";
	public static final String WATCHER		= "WATCHER";

	public static final String CONFIG_DIALOG = "CONFIGURATION";
	public static final String CONFIG_COMPARATOR = "COMPARATOR";

	@XmlRootElement(name = "value")
	@XmlAccessorType(XmlAccessType.NONE)
	public static class SettingsValue implements Mutable
	{
		@XmlAttribute
		private String ns;

		@XmlAttribute
		private String dialog;

		@XmlAttribute
		private String key;

		@XmlAttribute
		private Date time;

		@XmlValue
		private String value;

		@XmlTransient
		private boolean changed;

		public SettingsValue()
		{
			this(null, null, null);
		}

		public SettingsValue(String ns, String dialog, String key)
		{
			this.changed = false;
			this.ns = ns;
			this.dialog = dialog;
			this.key = key;
			this.time = new Date();
		}

		public String getNs()
		{
			return this.ns;
		}

		public String getDialog()
		{
			return this.dialog;
		}

		public String getKey()
		{
			return this.key;
		}

		public Date getTime()
		{
			return this.time;
		}

		public String getValue()
		{
			return this.value;
		}

		public void setValue(String value)
		{
			this.changed = true;
			this.value = value;
		}

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

		@Override
		public String toString()
		{
			return "{" + this.ns + ":" + this.dialog + ":" + this.key + "=" + this.value + "}";
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || this.getClass() != o.getClass())
				return false;

			SettingsValue that = (SettingsValue) o;

			if (!this.ns.equals(that.ns))
				return false;
			if (!this.dialog.equals(that.dialog))
				return false;
			return this.key.equals(that.key);

		}

		@Override
		public int hashCode()
		{
			int result = this.ns.hashCode();
			result = 31 * result + this.dialog.hashCode();
			result = 31 * result + this.key.hashCode();
			return result;
		}
	}

	@XmlElement(name = "value")
	protected MutableArrayList<SettingsValue> values;

	@XmlTransient
	private String fileName;

	public Settings()
	{
		this.values = new MutableArrayList<>();
	}

	/**
	 * Load a settings from passed file name. If loading was failed, will return {@link Settings#defaultSettings()}
	 * @param fileName file, from which will load settings
	 * @return If loading was failed, will return {@link Settings#defaultSettings()}. Otherwise will return loaded settings
	 */
	public static Settings load(String fileName)
	{
		File file = new File(fileName);
		Settings settings;
		Settings defaultSettings = defaultSettings();
		if (file.exists())
		{
			try(Reader reader = CommonHelper.readerFromFile(file))
			{
				JAXBContext jaxbContext = JAXBContext.newInstance(jaxbContextClasses);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				unmarshaller.setEventHandler(event -> {
					logger.error("Error in settings : " + event);
					return false;
				});
				settings = (Settings) unmarshaller.unmarshal(reader);
			}
			catch (Exception e)
			{
				settings = defaultSettings;
			}
		}
		else
		{
			settings = defaultSettings;
		}
		Settings finalSettings = settings;
		//add all default settings for loaded settings
		defaultSettings.values.stream()
				.filter(sv -> !finalSettings.values.contains(sv))
				.forEach(finalSettings.values::add);
		finalSettings.fileName = fileName;
		return finalSettings;
	}

	//region default settings
	private static Settings DEFAULT_SETTINGS;

	/**
	 * Create default settings.
	 * @return default settings.
	 */
	public static Settings defaultSettings()
	{
		if (DEFAULT_SETTINGS == null)
		{
			DEFAULT_SETTINGS = new Settings();
			DEFAULT_SETTINGS.setMapValues(GLOBAL_NS, CONFIG_DIALOG, mapOf(
					CONFIG_COMPARATOR, CompareEnum.ALPHABET_0_1.name()
			));
			DEFAULT_SETTINGS.setMapValues(GLOBAL_NS, SETTINGS, mapOf(
					MAX_LAST_COUNT,"10",
					TIME_NOTIFICATION,"5",
					THEME, Theme.WHITE.name(),
					USE_FULL_SCREEN, Boolean.FALSE.toString(),
					USE_EXTERNAL_REPORT_VIEWER, Boolean.FALSE.toString(),
					USE_FULLSCREEN_XPATH, Boolean.FALSE.toString(),
					LANGUAGE, Locales.ENGLISH.name(),
					COPYRIGHT,"",
					FONT, "System$13"
			));

			DEFAULT_SETTINGS.setMapValues(GLOBAL_NS, SHORTCUTS_NAME, mapOf(
					//Document
					SAVE_DOCUMENT,"Ctrl+S",
					SAVE_DOCUMENT_AS,"Shift+Ctrl+S",
					UNDO,"Ctrl+Z",
					REDO,"Shift+Ctrl+Z",

					//Matrix navigation
					ADD_ITEMS, "Insert",
					ALL_PARAMETERS, "Ctrl+D",
					COPY_ITEMS, "Ctrl+C",
					CUT_ITEMS, "Ctrl+X",
					PASTE_ITEMS, "Ctrl+V",
					HELP,"Ctrl+F1",
					DELETE_ITEM,"Delete",
					SHOW_ALL,"Ctrl+Q",
					GO_TO_LINE,"Ctrl+G",
					ADD_PARAMETER, "Ctrl+P",
					BREAK_POINT,"Ctrl+B",

					//Matrix actions
					SHOW_WATCH, "F2",
					TRACING, "Ctrl+F3",
					START_MATRIX, "Ctrl+F4",
					STOP_MATRIX, "Ctrl+F5",
					PAUSE_MATRIX, "F6",
					STEP_MATRIX, "F7",
					SHOW_RESULT, "F8",
					FIND_ON_MATRIX,"Ctrl+F",

					//Other
					SHOW_ALL_TABS,"Ctrl+E",
					SEARCH, "Ctrl+Shift+F"
			));

			DEFAULT_SETTINGS.setMapValues(GLOBAL_NS, LOGS_NAME, mapOf(
					ALL, "0x000000ff",
					DEBUG, "0x334db3ff",
					ERROR, "0xcc3333ff",
					FATAL, "0xcc3333ff",
					INFO, "0x336633ff",
					TRACE, "0x8066ccff",
					WARN, "0xe64d4dff"
			));

			DEFAULT_SETTINGS.setMapValues(GLOBAL_NS, MATRIX_COLORS, mapOf(
					ActionGroups.App.name(), "rgba(118,145,39,1.0)",
					ActionGroups.Matrix.name(), "rgba(85,183,183,1.0)",
					ActionGroups.GUI.name(), "rgba(82,168,100,1.0)",
					ActionGroups.Messages.name(), "rgba(231,116,52,1.0)",
					ActionGroups.Tables.name(), "rgba(209,73,73,1.0)",
					ActionGroups.Text.name(), "rgba(84,174,227,1.0)",
					ActionGroups.Clients.name(), "rgba(170,142,206,1.0)",
					ActionGroups.Services.name(), "rgba(101,177,170,1.0)",
					ActionGroups.SQL.name(), "rgba(211,52,114,1.0)",
					ActionGroups.System.name(), "rgba(237,173,52,1.0)",
					ActionGroups.Report.name(), "rgba(73,149,182,1.0)",
					ActionGroups.XML.name(), "rgba(201,138,205,1.0)",

					Tokens.Assert.name(), "rgba(205,80,122,1.0)",
					Tokens.NameSpace.name(), "rgba(87,149,27,1.0)",
					Tokens.TestCase.name(), "rgba(80,158,228,1.0)",
					Tokens.RawTable.name(), "rgba(209,73,73,1.0)",
					Tokens.RawMessage.name(), "rgba(231,116,52,1.0)",
					Tokens.SubCase.name(), "rgba(81,159,226,1.0)",
					Tokens.RawText.name(), "rgba(84,174,227,1.0)",
					Tokens.Step.name(), "rgba(81,159,226,1.0)",
					Tokens.Let.name(), "rgba(76,131,76,1.0)"
			));


			DEFAULT_SETTINGS.setMapValues(GLOBAL_NS, MATRIX_NAME, mapOf(
					MATRIX_DEFAULT_SCREENSHOT, ScreenshotKind.Never.name(),
					MATRIX_POPUPS, Boolean.FALSE.toString(),
					MATRIX_FOLD_ITEMS, Boolean.FALSE.toString(),
					MATRIX_OPEN_REPORT_AFTER_FINISHED, Boolean.FALSE.toString()
			));

			DEFAULT_SETTINGS.setMapValues(GLOBAL_NS, GIT, mapOf(
					GIT_KNOWN_HOST, "",
					GIT_SSH_IDENTITY, ""
			));

			DEFAULT_SETTINGS.setMapValues(GLOBAL_NS, WIZARD_NAME, mapOf(
					WizardSettings.Kind.TYPE.name()+MAX, "1",
					WizardSettings.Kind.TYPE.name()+MIN, "0",

					WizardSettings.Kind.PATH.name()+MAX, "1",
					WizardSettings.Kind.PATH.name()+MIN, "0",

					WizardSettings.Kind.SIZE.name()+MAX, "1",
					WizardSettings.Kind.SIZE.name()+MIN, "0",

					WizardSettings.Kind.POSITION.name()+MAX, "1",
					WizardSettings.Kind.POSITION.name()+MIN, "0",

					WizardSettings.Kind.ATTR.name()+MAX, "1",
					WizardSettings.Kind.ATTR.name()+MIN, "0",

					THRESHOLD, "0.6"
			));
		}
		return DEFAULT_SETTINGS;
	}
	//endregion

	/**
	 * Save settings to the passed file
	 * @param fileName file, which use for saving the settings
	 * @throws Exception file not found or the character encoding is not supported.
	 */
	public synchronized void save(String fileName) throws Exception
	{
		try (Writer writer = CommonHelper.writerToFileName(fileName))
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(jaxbContextClasses);

			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(this, writer);

			this.fileName = fileName;
			this.values.saved();
		}
	}

	/**
	 * Save the settings, if they were changed
	 * @throws Exception if saving was failed
	 */
	public synchronized void saveIfNeeded() throws Exception
	{
		if (this.values.isChanged())
		{
			this.save();
		}
	}

	/**
	 * Save settings. If the loaded file is null, will use default settings path {@link Settings#SETTINGS_PATH}
	 * @throws Exception if saving was failed
	 */
	private void save() throws Exception
	{
		if (this.fileName != null)
		{
			this.save(this.fileName);
		}
		else
		{
			this.save(SETTINGS_PATH);
		}
	}

	/**
	 * @return all namespaces from settings
	 */
	public synchronized Set<String> getNamespaces()
	{
		return this.values.stream()
				.map(SettingsValue::getNs)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	/**
	 * Return map values from the settings for the passed namespace, dialog and names.
	 * @param ns namespace, used for getting values
	 * @param dialog dialog, used for getting values
	 * @param names array of names, which need be returned
	 * @return map values for the passed namespace, dialog and names.
	 */
	public Map<String, String> getMapValues(String ns, String dialog, String[] names)
	{
		Map<String, String> res = new HashMap<>();
		for (String s : names)
		{
			res.put(s, null);
		}

		this.getValues(ns, dialog).forEach(value -> res.put(value.getKey(), value.getValue()));
		return res;
	}

	/**
	 * Return list of values from settings by passed namespace and dialog
	 * @param ns namespace, which used for getting values
	 * @param dialog dialog, which used for getting values
	 * @return list of {@link SettingsValue} for passed namespace and dialog
	 *
	 * @see SettingsValue
	 */
	public synchronized List<SettingsValue> getValues(String ns, String dialog)
	{
		return this.values.stream()
				.filter(value -> Str.areEqual(ns, value.getNs()) && Str.areEqual(dialog, value.getDialog()))
				.collect(Collectors.toList());
	}

	/**
	 * Return the settings value for the passed namespace, dialog and key.<br>
	 * If setting with name from passed namespace and dialog not found, will return default setting for the passed namespace and dialog
	 * @param ns namespace, which used for getting setting
	 * @param dialog dialog, which used for getting setting
	 * @param key key, which used for getting settings
	 * @return setting value by passed arguments.
	 *
	 * @throws IllegalArgumentException if default value for passed namespace and dialog not found.
	 */
	public SettingsValue getValueOrDefault(String ns, String dialog, String key)
	{
		SettingsValue result = this.getValue(ns, dialog, key);
		if (result == null)
		{
			result = defaultSettings().getValue(ns, dialog, key);
		}
		return Optional.ofNullable(result)
				.orElseThrow(() -> new IllegalArgumentException(String.format(R.SETTINGS_GET_DEFAULT_VALUE_EXCEPTION.get(), key)));
	}

	/**
	 * Return the settings value for the passed namespace, dialog and key.<br>
	 * If setting with name from passed namespace and dialog not found, will return null
	 * @param ns namespace, which used for getting setting
	 * @param dialog dialog, which used for getting setting
	 * @param key key, which used for getting settings
	 * @return setting value by passed arguments.
	 */
	public synchronized SettingsValue getValue(String ns, String dialog, String key)
	{
		return this.values.stream()
				.filter(value -> Str.areEqual(ns, value.getNs()) && Str.areEqual(dialog, value.getDialog()) && Str.areEqual(key, value.getKey()))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Set value for the passed namespace, dialog and key <br>
	 * If the settings value more than max, the youngest settings will removed.
	 * If the setting by passed namespace, dialog and key will no found, will create new settingValue and stored into the settings.
	 * 
	 * @param ns namespace, which will used for found setting
	 * @param dialog dialog, which will used for found setting
	 * @param key key, which will used for found settings
	 * @param max max count of settings.
	 * @param newValue value, which will be setting to found setting.
	 */
	public synchronized void setValue(String ns, String dialog, String key, int max, String newValue)
	{
		List<SettingsValue> list = this.getValues(ns, dialog);
		list.sort(comparator);
		for (SettingsValue value : list)
		{
			if (value.getKey() != null && value.getKey().equals(key))
			{
				value.setValue(newValue);
				return;
			}
		}
		while (list.size() >= max)
		{
			list.remove(list.size() - 1);
		}
		this.removeAll(ns, dialog);
		this.values.addAll(list);

		SettingsValue settingsValue = new SettingsValue(ns, dialog, key);
		settingsValue.setValue(newValue);

		this.values.add(settingsValue);
	}

	/**
	 * Set value for the passed namespace, dialog and key <br>
	 * If the setting by passed namespace, dialog and key will no found, will create new settingValue and stored into the settings.
	 *
	 * @param ns namespace, which will used for found setting
	 * @param dialog dialog, which will used for found setting
	 * @param key key, which will used for found settings
	 * @param newValue value, which will be setting to found setting.
	 */
	public synchronized void setValue(String ns, String dialog, String key, String newValue)
	{
		for (SettingsValue value : this.values)
		{
			if (Str.areEqual(ns, value.getNs())
					&& Str.areEqual(dialog, value.getDialog())
					&& Str.areEqual(key, value.getKey()))
			{
				value.setValue(newValue);
				return;
			}
		}

		SettingsValue value = new SettingsValue(ns, dialog, key);
		value.setValue(newValue);

		this.values.add(value);
	}

	/**
	 * Set map values to the settings. <br>
	 * For each entry from map, will execute method {@link Settings#setValue(String, String, String, String)},
	 * where 3rd parameter is map key and 4rd parameter is map value.
	 * 
	 * @see Settings#setValue(String, String, String, String) 
	 */
	public void setMapValues(String ns, String dialog, Map<String, String> values)
	{
		values.forEach((key, value) -> this.setValue(ns, dialog, key, value));
	}

	/**
	 * Remove all settings value from the settings, which matches passed namespace and dialog
	 * @param ns namespace, which used for found setting value, which will removed
	 * @param dialog dialog, which used for found setting value, which will removed
	 */
	public synchronized void removeAll(String ns, String dialog)
	{
		this.values = this.values.stream()
				.filter(value -> !(Str.areEqual(ns, value.getNs()) && Str.areEqual(dialog, value.getDialog())))
				.collect(Collectors.toCollection(MutableArrayList::new));
	}

	/**
	 * Remove settings value from the settings, which matches passed namespace, dialog and key
	 * @param ns namespace, which used for found setting value, which will removed
	 * @param dialog dialog, which used for found setting value, which will removed
	 * @param key key, which used for found setting value, which will removed
	 */
	public synchronized void remove(String ns, String dialog, String key)
	{
		this.values = this.values.stream()
				.filter(value -> !(Str.areEqual(ns, value.getNs()) && Str.areEqual(dialog, value.getDialog()) && Str.areEqual(key, value.getKey())))
				.collect(Collectors.toCollection(MutableArrayList::new));
	}

	private static Map<String, String> mapOf(String... args)
	{
		Map<String, String> map = new LinkedHashMap<>();
		Iterator<String> iterator = Arrays.asList(args).iterator();
		while (iterator.hasNext())
		{
			String key = iterator.next();
			if (iterator.hasNext())
			{
				String value = iterator.next();
				map.put(key, value);
			}
			else
			{
				break;
			}
		}
		return map;
	}

	/**
	 * Remove all from the settings
	 */
	public synchronized void clear()
	{
		this.values.clear();
	}

	public synchronized List<KeyCombination> getRemovedShortcuts()
	{
		return Stream.of(
				SAVE_DOCUMENT,
				ADD_ITEMS,
				ALL_PARAMETERS,
				BREAK_POINT,
				ADD_PARAMETER,
				HELP,
				GO_TO_LINE,
				SHOW_ALL,
				DELETE_ITEM,
				COPY_ITEMS,
				CUT_ITEMS,
				PASTE_ITEMS,
				START_MATRIX,
				STOP_MATRIX,
				PAUSE_MATRIX,
				STEP_MATRIX,
				SHOW_RESULT,
				SHOW_WATCH,
				TRACING,
				FIND_ON_MATRIX,
				SEARCH
		)
				.map(s -> Common.getShortcut(this, s))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
}
