package com.automic.azure.constants;

/**
 * Class contains all the constants used in Azure java application.
 * 
 */
public final class Constants {   
    
    /**
     * https string constant
     */
    public static final String HTTPS = "https";
    
    /**
     * "Unknown Error" string constant
     */
    public static final String UNKNOWN_ERROR = "Unknown Error";
    
    /**
     * int constant for IO Buffer used to buffer the data.
     */
    public static final int IO_BUFFER_SIZE = 4 * 1024;
    
    public static final String AZURE_BASE_URL = "https://management.core.windows.net";
    
    public static final String X_MS_VERSION = "x-ms-version";
    
    public static final String X_MS_VERSION_VALUE = "2013-11-01";
    public static final String REQUEST_TOKENID_KEY = "x-ms-request-id";
    
    public static final String HELP = "h";

	public static final String PASSWORD = "pwd";

	public static final String KEYSTORE_LOCATION = "ksl";

	public static final String SUBSCRIPTION_ID = "sid";

	public static final String CONNECTION_TIMEOUT = "cto";

	public static final String READ_TIMEOUT = "rto";

	public static final String ACTION = "act";

	public static final String OUTPUT_FILE = "ofl";
	
	public static final String SERVICE_NAME = "ser";
	public static final String DEPLOYMENT_NAME = "dep";
	public static final String ROLE_NAME = "rol";
	public static final String OPERATIONTYPE="ShutdownRoleOperation";
	public static final String SERVICES_PATH = "services";
	public static final String HOSTEDSERVICES_PATH ="hostedservices";
	public static final String DEPLOYMENTS_PATH ="deployments";
	public static final String ROLEINSTANCES_PATH = "roleinstances";
	public static final String OPERATIONS_PATH = "Operations";
	public static final String EMPTY = "";
	public static final String STOPPED = "Stopped";
	

    private Constants() {
    }

}
