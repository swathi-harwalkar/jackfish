////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.config;

import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.app.IApplicationPool;
import com.exactprosystems.jf.api.app.IGuiDictionary;
import com.exactprosystems.jf.api.client.IClientsPool;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.DateTime;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.service.IServicesPool;
import com.exactprosystems.jf.app.ApplicationPool;
import com.exactprosystems.jf.client.ClientsPool;
import com.exactprosystems.jf.common.ConsoleUpdateLibsListener;
import com.exactprosystems.jf.common.MutableString;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.UpdateLibsListener;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.MvelEvaluator;
import com.exactprosystems.jf.common.evaluator.SystemVars;
import com.exactprosystems.jf.common.parser.Matrix;
import com.exactprosystems.jf.common.parser.items.MatrixItem;
import com.exactprosystems.jf.common.parser.items.MatrixRoot;
import com.exactprosystems.jf.common.parser.items.MutableArrayList;
import com.exactprosystems.jf.common.parser.items.SubCase;
import com.exactprosystems.jf.common.parser.listeners.DummyRunnerListener;
import com.exactprosystems.jf.common.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.common.parser.listeners.MatrixListener;
import com.exactprosystems.jf.common.parser.listeners.RunnerListener;
import com.exactprosystems.jf.common.report.HTMLReportFactory;
import com.exactprosystems.jf.common.report.ReportFactory;
import com.exactprosystems.jf.common.xml.schema.Xsd;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.service.ServicePool;
import com.exactprosystems.jf.sql.DataBasePool;
import com.exactprosystems.jf.tool.AbstractDocument;
import com.exactprosystems.jf.tool.main.DocumentKind;
import com.exactprosystems.jf.tool.main.Main;
import org.apache.log4j.Logger;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)

@DocumentInfo(
		newName = "NewConfiguration", 
		extentioin = "xml", 
		description = "Jackfish configuration"
)
public class Configuration extends AbstractDocument
{
	public static final String	SEPARATOR			= ",";

	public static final String 	projectName 		= "JackFish";
	public static final String 	varExt 				= ".ini";
	public static final String 	dictExt 			= ".xml";
	public static final String 	matrixExt 			= ".jf";
	public static final String 	matrixFilter		= "*.jf";
	public static final char  	matrixDelimiter		= ';';
	public static final String 	unicodeDelimiter	= String.valueOf("\\\\u" + Integer.toHexString(Configuration.matrixDelimiter | 0x10000).substring(1));

	public static final String time					= "time";
	public static final String date					= "date";
	public static final String dateTime				= "dateTime";
	public static final String formats				= "formats";
	public static final String imports 				= "import";
	public static final String reports 				= "reports";
	public static final String vars 				= "vars";
	public static final String matrix 				= "matrix";
	public static final String appDict 				= "appDict";
	public static final String clientDict			= "clientDict";
	public static final String library 				= "library";
	public static final String userVars 			= "userVars";
	public static final String git 					= "git";

	public static final String entryName			= "name";
	
	public static final String sqlEntry				= "sqlEntry";
	public static final String sqlJar				= "sqlJar";
	public static final String sqlConnection 		= "sqlConnection";
	
	public static final String clientEntry			= "clientEntry";
	public static final String clientDescription 	= "clientDescription";
	public static final String clientJar			= "clientJar";
	public static final String clientDictionary		= "clientDictionary";
	public static final String clientLimit			= "clientLimit";

	public static final String serviceEntry			= "serviceEntry";
	public static final String serviceDescription 	= "serviceDescription";
	public static final String serviceJar			= "serviceJar";

	public static final String appEntry				= "appEntry";
	public static final String appDescription 		= "appDescription";
	public static final String appDicPath			= "appDicPath"; 
	public static final String appJar				= "appJar";
	public static final String appWorkDir			= "appWorkDir";
	public static final String appStartPort			= "appStartPort";
	
	public static final String parametersEntry		= "parameters";
	public static final String parametersKey		= "key";
	public static final String parametersValue		= "value";

	//------------------------------------------------------------------------------------------------------------------
	// new technology
	//------------------------------------------------------------------------------------------------------------------
	
	
	@XmlElement(name = time)
	protected MutableString timeValue;
	
	@XmlElement(name = date)
	protected MutableString dateValue;
	
	@XmlElement(name = dateTime)
	protected MutableString dateTimeValue;
	
	@XmlElement(name = formats)
	protected MutableArrayList<MutableString> formatsValue;

	@XmlElement(name = reports)
	protected MutableString reportsValue;
	
	@XmlElement(name = git)
	protected MutableString gitValue;

	@XmlElement(name = imports)
	protected MutableArrayList<MutableString> importsValue;

	@XmlElement(name = vars)
	protected MutableString varsValue;
	
	@XmlElement(name = userVars)
	protected MutableArrayList<MutableString> userVarsValue;

	@XmlElement(name = matrix)
	protected MutableArrayList<MutableString> matricesValue;
	
	@XmlElement(name = appDict)
	protected MutableArrayList<MutableString> appDictionariesValue;

	@XmlElement(name = clientDict)
	protected MutableArrayList<MutableString> clientDictionariesValue;

	@XmlElement(name = library)
	protected MutableArrayList<MutableString> librariesValue;
	
	@XmlElement(name = sqlEntry)
	public MutableArrayList<SqlEntry> sqlEntriesValue;

	@XmlElement(name = clientEntry)
	public MutableArrayList<ClientEntry> clientEntriesValue;

	@XmlElement(name = serviceEntry)
	public MutableArrayList<ServiceEntry> serviceEntriesValue;

	@XmlElement(name = appEntry)
	public MutableArrayList<AppEntry> appEntriesValue;

	public Configuration(String fileName, Settings settings)
	{
		super(fileName, null);

		this.settings 					= settings;
		this.changed 					= false;
		
		this.timeValue					= new MutableString();
		this.dateValue					= new MutableString();
		this.dateTimeValue				= new MutableString();
		this.formatsValue				= new MutableArrayList<MutableString>();
		this.reportsValue				= new MutableString();
		this.gitValue					= new MutableString();
		this.sqlEntriesValue 			= new MutableArrayList<SqlEntry>();
		this.clientEntriesValue			= new MutableArrayList<ClientEntry>();
		this.serviceEntriesValue		= new MutableArrayList<ServiceEntry>();
		this.appEntriesValue 			= new MutableArrayList<AppEntry>();
		this.importsValue				= new MutableArrayList<MutableString>();
		this.varsValue					= new MutableString();
		this.userVarsValue				= new MutableArrayList<MutableString>();
		this.matricesValue				= new MutableArrayList<MutableString>();
		this.appDictionariesValue		= new MutableArrayList<MutableString>();
		this.clientDictionariesValue	= new MutableArrayList<MutableString>();
		this.librariesValue				= new MutableArrayList<MutableString>();

		this.globals 					= new HashMap<String, Object>();
		this.clients 					= new ClientsPool(this);
		this.services 					= new ServicePool(this);
		this.applications 				= new ApplicationPool(this);
		this.databases 					= new DataBasePool(this);

		this.libs 						= new HashMap<String, Matrix>();
	}

	public Configuration()
	{
		this("unknown", new Settings());
	}
	
	public MutableString getTime()
	{
		return this.timeValue;
	}

	public MutableString getDate()
	{
		return this.dateValue;
	}

	public MutableString getDateTime()
	{
		return this.dateTimeValue;
	}

	public MutableString getReports()
	{
		return this.reportsValue;
	}

	public MutableString getGit()
	{
		return this.gitValue;
	}

	public MutableString getVars()
	{
		return this.varsValue;
	}

	public MutableArrayList<MutableString> getAppDictionariesValue()
	{
		return appDictionariesValue;
	}

	public MutableArrayList<MutableString> getClientDictionariesValue()
	{
		return clientDictionariesValue;
	}

	public IClientsPool getClientPool()
	{
		return this.clients;
	}

	public IApplicationPool getApplicationPool()
	{
		return this.applications;
	}

	public IServicesPool getServicesPool()
	{
		return this.services;
	}

	public DataBasePool getDataBasesPool()
	{
		return this.databases;
	}

	public Settings getSettings()
	{
		return this.settings;
	}
	
	public void storeGlobal(String name, Object value)
	{
		synchronized (this.globals)
		{
			this.globals.put(name, value);
		}
	}

	public Object restoreGlobal(String name)
	{
		synchronized (this.globals)
		{
			return this.globals.get(name);
		}
	}
	
	public void dictionaryChanged(String name, IGuiDictionary dictionary)
	{
		for (AppEntry entry : this.appEntriesValue)
		{
			
			try
			{
				String dicName = entry.get(appDicPath);
				String id = entry.get(entryName);
				
				if (dicName != null && dicName.equals(name))
				{
					IApplicationFactory factory = getApplicationPool().loadApplicationFactory(id);
					factory.init(dictionary);
				}
			}
			catch (Exception e)
			{ 
				// nothing to do
			}
		}
	}
	
	public void matrixChanged(String name, Matrix matrix)
	{
		refreshLibs();
	}

	public AbstractEvaluator createEvaluator() throws Exception
	{
		String evaluatorClassName = evaluatorValue;
		if (Str.IsNullOrEmpty(evaluatorClassName))
		{
			throw new Exception("Empty evaluator class name.");
		}
		
		AbstractEvaluator evaluator	= objectFromClassName(evaluatorClassName, AbstractEvaluator.class);
		evaluator.addImports(toStringList(this.importsValue));
		setUserVariablesFromMask(this.varsValue.get(), evaluator);
		for (MutableString userVars : this.userVarsValue)
		{
			setUserVariablesFromMask(userVars.get(), evaluator);
		}
		
		evaluator.reset();
		
		return evaluator;
	}

	public Context createContext(IMatrixListener matrixListener, PrintStream out) throws Exception
	{
		return new Context(matrixListener, out, this);
	}
	
	public void refreshLibs()
	{
		IMatrixListener checker = new MatrixListener();
		this.libs.clear();
		if (this.librariesValue == null)
		{
			return;
		}

		for (MutableString folder : this.librariesValue)
		{
			File folderFile = new File(folder.get());
			if (folderFile.exists() && folderFile.isDirectory())
			{
				File[] libFiles = folderFile.listFiles((dir, name) -> name != null && name.endsWith(matrixExt));
				
				for (File libFile : libFiles)
				{
					try (Reader reader = new FileReader(libFile))
					{
						Matrix matrix = new Matrix(libFile.getName(), this, checker);
						if (!checker.isOk())
						{
							logger.error("Library load error: [" + libFile.getName() + "] " + checker.getExceptionMessage());
							continue;
						}
						matrix.load(reader);
						for (String ns : matrix.nameSpaces())
						{
							this.libs.put(ns, matrix);
						}
					}
					catch (Exception e)
					{
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
	}

	
    //------------------------------------------------------------------------------------------------------------------
    // interface Document
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public void load(Reader reader) throws Exception
    {
    	super.load(reader);
    	try
    	{
			this.valid = false;
	
	        jaxbContextClasses[0] = this.getClass();
	        JAXBContext jaxbContext = JAXBContext.newInstance(jaxbContextClasses);
	
	
	        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	        Source schemaFile = new StreamSource(Xsd.class.getResourceAsStream("Configuration.xsd"));
	        Schema schema = schemaFactory.newSchema(schemaFile);
	
	
	        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
	
	        unmarshaller.setSchema(schema);
	        unmarshaller.setEventHandler(new ValidationEventHandler()
	        {
	            @Override
	            public boolean handleEvent(ValidationEvent event)
	            {
	                System.out.println("Error in configuration : " + event);
	
	                return false;
	            }
	        });
	
	        Configuration config = (Configuration) unmarshaller.unmarshal(reader);
	        setAll(config);
	        
			this.reportFactoryObj		= objectFromClassName(reportFactoryValue, ReportFactory.class);

			DateTime.setFormats(this.timeValue.get(), this.dateValue.get(), this.dateTimeValue.get());
			Converter.setFormats(toStringList(this.formatsValue));
			refreshLibs();

			this.valid = true;
    	}
		catch (UnmarshalException e)
		{
			throw new Exception(e.getCause().getMessage(), e.getCause());
		}
	}

    @Override
    public boolean canClose() throws Exception 
    {
    	boolean res = true;
    	
		synchronized (this.subordinates)
		{
			for (Document doc : this.subordinates)
			{
				res = res && doc.canClose();
			}
		}
		return res;
    }

    @Override
	public void close(Settings settings) throws Exception
	{
		super.close(settings);

		Set<Document> copy;
		synchronized (this.subordinates)
		{
			copy = new HashSet<>();
			copy.addAll(this.subordinates);
		}
		
		// save list of all opened documents ...
		this.settings.removeAll(Main.MAIN_NS, Main.OPENED);
		this.settings.saveIfNeeded();

		for (Document doc : copy)
		{
			try
			{
				DocumentKind kind = DocumentKind.byDocument(doc);
				if (doc.hasName())
				{
					this.settings.setValue(Main.MAIN_NS, Main.OPENED, doc.getName(), kind.toString());
				}
				doc.close(this.settings);
			}
			catch (Exception e)
			{
				logger.error(e.getMessage());
			}
		}
		this.settings.saveIfNeeded();

		this.services.stopAllServices();
		this.applications.stopAllApplications();
    }
    
    @Override
    public void save(String fileName) throws Exception
    {
    	super.save(fileName);
    	
        try(OutputStream os = new FileOutputStream(new File(fileName)))
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(jaxbContextClasses);

            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(this, os);

    		refreshLibs();

			saved();
        }
        catch (FileNotFoundException e)
        {
            logger.error(String.format("File '%s' is not found.",fileName));
            throw e;
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // interface Mutable
    //------------------------------------------------------------------------------------------------------------------
	@Override
	public boolean isChanged()
	{
		if (this.changed)
		{
			return true;
		}
		
		return this.timeValue.isChanged()
				|| this.dateValue.isChanged()
				|| this.dateTimeValue.isChanged()
				|| this.formatsValue.isChanged()
				|| this.gitValue.isChanged()
				|| this.reportsValue.isChanged()
				|| this.sqlEntriesValue.isChanged()
				|| this.clientEntriesValue.isChanged()
				|| this.serviceEntriesValue.isChanged()
				|| this.appEntriesValue.isChanged()
				|| this.importsValue.isChanged()
				|| this.varsValue.isChanged()
				|| this.userVarsValue.isChanged()
				|| this.matricesValue.isChanged()
				|| this.appDictionariesValue.isChanged()
				|| this.clientDictionariesValue.isChanged()
				|| this.librariesValue.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		
		this.changed = false;
		this.timeValue.saved();
		this.dateValue.saved();
		this.dateTimeValue.saved();
		this.formatsValue.saved();
		this.gitValue.saved();
		this.reportsValue.saved();
		this.appEntriesValue.saved();
		this.clientEntriesValue.saved();
		this.serviceEntriesValue.saved();
		this.sqlEntriesValue.saved();
		this.importsValue.saved();
		this.varsValue.saved();
		this.userVarsValue.saved();
		this.matricesValue.saved();
		this.appDictionariesValue.saved();
		this.clientDictionariesValue.saved();
		this.librariesValue.saved();
	}

	//------------------------------------------------------------------------------------------------------------------
	public SubCase referenceToSubcase(String name, MatrixItem item)
	{
		MatrixItem ref = item.findParent(MatrixRoot.class).find(true, SubCase.class, name);

		if (ref != null && ref instanceof SubCase)
		{
			return (SubCase) ref;
		}
		if (name == null)
		{
			return null;
		}
		String[] parts = name.split("\\.");
		if (parts.length < 2)
		{
			return null;
		}
		String ns = parts[0];
		String id = parts[1];

		Matrix matrix = this.libs.get(ns);
		if (matrix == null)
		{
			matrix = getLib(ns);

			if (matrix == null)
			{
				return null;
			}
			try
			{
				matrix = matrix.clone();
			}
			catch (CloneNotSupportedException e)
			{
				logger.error(e.getMessage(), e);
			}
			this.libs.put(ns, matrix);
		}

		return (SubCase) matrix.getRoot().find(true, SubCase.class, id);
	}

	public SqlEntry getSqlEntry(String name) throws Exception
	{
		return getEntry(name, this.sqlEntriesValue);
	}

	public List<SqlEntry> getSqlEntries()
	{
		return this.sqlEntriesValue;
	}

	public ClientEntry getClientEntry(String name) throws Exception
	{
		return getEntry(name, this.clientEntriesValue);
	}

	public List<ClientEntry> getClientEntries()
	{
		return this.clientEntriesValue;
	}

	public ServiceEntry getServiceEntry(String name) throws Exception
	{
		return getEntry(name, this.serviceEntriesValue);
	}

	public List<ServiceEntry> getServiceEntries()
	{
		return this.serviceEntriesValue;
	}

	public AppEntry getAppEntry(String name) throws Exception
	{
		return getEntry(name, this.appEntriesValue);
	}

	
	public RunnerListener getRunnerListener()
	{
		return this.runnerListener;
	}
	
	public List<AppEntry> getAppEntries()
	{
		return this.appEntriesValue;
	}

	public final ReportFactory getReportFactory()
	{
		return this.reportFactoryObj;
	}

	public final Collection<String> getClients() throws Exception
	{
		return this.clientEntriesValue.stream().map(entry -> entry.toString()).collect(Collectors.toList());
	}

	public final Collection<String> getServices() throws Exception
	{
		return this.serviceEntriesValue.stream().map(entry -> entry.toString()).collect(Collectors.toList());
	}

	public final Collection<String> getApplications() throws Exception
	{
		return this.appEntriesValue.stream().map(entry -> entry.toString()).collect(Collectors.toList());
	}

	public Matrix getLib(String name)
	{
		return this.libs.get(name);
	}
	
	public Map<String, Matrix> getLibs()
	{
		return this.libs;
	}

	public final boolean isValid()
	{
		return this.valid;
	}
    
	public final void register(Document doc)
	{
		synchronized (this.subordinates)
		{
			this.subordinates.add(doc);
		}
	}

	public final void unregister(Document doc)
	{
		synchronized (this.subordinates)
		{
			this.subordinates.remove(doc);
		}
	}

	public Map<String, Object> getStoreMap()
	{
		return new LinkedHashMap<>(this.globals);
	}

	public void storeMap(Map<String, Object> map)
	{
		this.globals = map;
	}
	
	public static List<String> toStringList(MutableArrayList<MutableString> str)
	{
		return str.stream().map(MutableString::get).collect(Collectors.toList());
	}

	
	@SuppressWarnings("unchecked")
	protected <T extends Entry> T getEntry(String name, List<T> entries) throws Exception
	{
		if (entries == null) 
		{
			return null;
		}
		for (Entry entry : entries)
		{
			if (entry.toString().equals(name))
			{
				return (T)entry;
			}
		}
		return null;
	}


	
	@SuppressWarnings("unchecked")
	private <T> T objectFromClassName(String name, Class<T> baseType) 	throws Exception
	{
		try
		{
			Class<?> type = null;
			try
			{
				type = Class.forName(name);
			} 
			catch (ClassNotFoundException e)
			{
				type = Class.forName(baseType.getPackage().getName() + "." + name);
			}
			
			if (!baseType.isAssignableFrom(type))
			{
				throw new Exception("class '" + name + "' is not assignable from " + baseType.getName());
			}
	
			return (T)type.newInstance();
		}
		catch (Exception e)
		{
			logger.error(String.format("objectFromClassName(%s, %s)", name, baseType));
			throw e;
		}
	}

	private void setUserVariablesFromMask(String userVariablesFileName, AbstractEvaluator evaluator)  throws Exception
	{
		if (Str.IsNullOrEmpty(userVariablesFileName))
		{
			return;
		}
		
		final File file = new File(userVariablesFileName);
		if (file.exists())
		{
			try (Reader reader = new FileReader(file))
			{
				SystemVars vars = new SystemVars(userVariablesFileName, this);
				vars.load(reader);
				vars.injectVariables(evaluator);
			}
		}
	}

	@Deprecated
	static Object get(Class<?> clazz, Object object, String name) throws Exception
	{
		Field[] fields = clazz.getDeclaredFields();
		
		for (Field field : fields)
		{
			XmlElement attr = field.getAnnotation(XmlElement.class);
			if (attr == null)
			{
				continue;
			}
			if (attr.name().equals(name))
			{
				return field.get(object);
			}
		}
		
		if (clazz.getSuperclass() != null )
		{
			return get(clazz.getSuperclass(), object, name);
		}
		
		return null;
	}
    
    private void setAll(Configuration config)
	{
		this.timeValue.set(config.timeValue);
		this.dateValue.set(config.dateValue);
		this.dateTimeValue.set(config.dateTimeValue);
		this.formatsValue.from(config.formatsValue);
		this.gitValue.set(config.gitValue);
		this.reportsValue.set(config.reportsValue);
		this.appEntriesValue.from(config.appEntriesValue);
		this.clientEntriesValue.from(config.clientEntriesValue);
		this.serviceEntriesValue.from(config.serviceEntriesValue);
		this.sqlEntriesValue.from(config.sqlEntriesValue);
		this.importsValue.from(config.importsValue);
		this.varsValue.set(config.varsValue);
		this.userVarsValue.from(config.userVarsValue);
		this.matricesValue.from(config.matricesValue);
		this.appDictionariesValue.from(config.appDictionariesValue);
		this.clientDictionariesValue.from(config.clientDictionariesValue);
		this.librariesValue.from(config.librariesValue);
		
		this.changed = false;
	}

	private static final Class<?>[] jaxbContextClasses = 
		{ 
			Configuration.class,
			SqlEntry.class,
			ClientEntry.class,
			ServiceEntry.class,
			AppEntry.class,
		};

	protected String 				reportFactoryValue = HTMLReportFactory.class.getSimpleName();
	protected String 				evaluatorValue = MvelEvaluator.class.getSimpleName();
	protected Map<File, Long> 		timestampMap 	= new HashMap<>();
	protected UpdateLibsListener 	listener		= new ConsoleUpdateLibsListener();
	protected RunnerListener 		runnerListener 	= new DummyRunnerListener();
	protected boolean 				changed;
	protected ReportFactory			reportFactoryObj;
	protected Map<String, Matrix>	libs;
	protected Map<String, Object>	globals;
	protected Settings 				settings;

	protected ClientsPool			clients;
	protected ServicePool			services;
	protected ApplicationPool		applications;
	protected DataBasePool			databases;

	protected final Set<Document> 	subordinates = new HashSet<Document>();
	
	protected boolean valid = false;

	private static final Logger logger = Logger.getLogger(Configuration.class);
}