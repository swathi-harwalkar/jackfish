package com.exactprosystems.jf.app;

import com.sun.jna.Library;

public interface JnaDriver extends Library {
	//region util methods
	@Deprecated
	String lastError();

	int lastErrorNumber();

	@Deprecated
	String methodTime();

	@Deprecated
	String uiAutomationTime();
	String getFrameworkId();
	void maxTimeout(int timeout);
	void setPluginInfo(String pluginInfo);

	void createLogger(String logLevel);
	//endregion

	//region application methods
	int connect(String title, int height, int width, int pid, int controlKind, int timeout);
	int run(String exec, String workDir, String param);
	void stop(boolean needKill);
	void refresh();
	String title();
	//endregion

	//region find methods
	String listAll(String ownerId, int controlKindId, String uid, String xpath, String clazz, String name, String title, String text, boolean many);
	int findAllForLocator(int[] arr, int len, String ownerId, int controlKindId, String uid, String xpath, String clazz, String name, String title, String text, boolean many);
	int findAll(int[] arr, int len, String elementId, int scopeId, int propertyId, String value);
	//endregion

	String elementAttribute(String elementId, int partId);
	String getList(String elementId);
	void sendKey(String key);

	void upAndDown(String key, boolean isDown);
	void mouse(String elementId, int actionId, int x, int y);

	void dragNdrop(int x1, int y1, int x2, int y2);

	/**
	 * if @param c == -1 -> arg is null;
	 * if @param c == 0 -> arg is array of string with separator %
	 * if @param c == 1 -> arg is array of int with separator %
	 * if @param c == 2 -> arg is array of double with separator %
	 * if @param c == 3 -> arg is array of WindowVisualState with separator %
	 */
	String doPatternCall(String elementId, int patternId, String method, String arg, int c);
	void setText(String elementId, String text);
	String getProperty(String elementId, int propertyId);
	int getPatterns(int[] arr, int len, String elementId);
	int getImage(int[] arr, int len, String id);

	void clearCache();

	//region table methods
	String getValueTableCell(String elementId, int column, int row);
	void mouseTableCell(String elementId, int column, int row, int mouseAction);
	void textTableCell(String elementId, int column, int row, String text);

	String getRowByCondition(String tableId, boolean useNumericHeader, String condition, String columns);

	String getRowIndexes(String tableId, boolean useNumericHeader, String condition, String columns);
	String getRowByIndex(String tableId, boolean useNumericHeader, int index);
	String getTable(String tableId, boolean useNumericHeader);
	int getTableSize(String tableId);

    String elementIsEnabled(String idString);
    String elementIsVisible(String idString);
    //endregion

}