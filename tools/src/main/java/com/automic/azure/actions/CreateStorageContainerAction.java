/**
 * 
 */
package com.automic.azure.actions;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ContainerAccess;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exception.AzureException;
import com.automic.azure.model.AzureStorageAccount;
import com.automic.azure.model.AzureStorageErrorResponse;
import com.automic.azure.services.AzureStorageAuthenticationService;
import com.automic.azure.util.CommonUtil;
import com.automic.azure.util.ConsoleWriter;
import com.automic.azure.util.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Action class to create a Container in Azure Storage
 *
 */
public class CreateStorageContainerAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(CreateStorageContainerAction.class);

    /**
     * storage acc from account name and access key
     */
    private AzureStorageAccount storageAccount;

    /**
     * Storage Authentication Service
     */
    private AzureStorageAuthenticationService authenticationService;

    /**
     * Storage container name
     */
    private String containerName;

    /**
     * Container access
     */
    private ContainerAccess containerAccess;

    /**
	 * 
	 */
    public CreateStorageContainerAction() {
        addOption("storage", true, "Storage Account Name");
        addOption("accesskey", true, "Primary Access Key");
        addOption("containername", true, "Storage Container Name");
        addOption("containeraccess", true, "Access level of Storage Container");

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.automic.azure.actions.AbstractAction#initialize()
     */
    @Override
    protected void initialize() {
        // storage acc from account name and access key
        this.storageAccount = new AzureStorageAccount(getOptionValue("storage"), getOptionValue("accesskey"));
        // container Name
        this.containerName = getOptionValue("containername");
        // access level of container
        if (getOptionValue("containeraccess") != null) {
            this.containerAccess = ContainerAccess.valueOf(getOptionValue("containeraccess"));
        }

        // authentication service
        this.authenticationService = new AzureStorageAuthenticationService(storageAccount, false);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.automic.azure.actions.AbstractAction#validateInputs()
     */
    @Override
    protected void validateInputs() throws AzureException {

        // validate storage name
        if (!Validator.checkNotEmpty(storageAccount.getAccountName())) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_ACC_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_STORAGE_ACC_NAME);
        }

        // validate storage access key
        if (!Validator.checkNotEmpty(storageAccount.getPrimaryAccessKey())) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_ACCESS_KEY);
            throw new AzureException(ExceptionConstants.EMPTY_STORAGE_ACCESS_KEY);
        }

        // validate storage container name
        if (!Validator.checkNotEmpty(this.containerName)) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_CONTAINER_NAME);
            throw new AzureException(ExceptionConstants.EMPTY_STORAGE_CONTAINER_NAME);
        }

        // validate storage container access
        if (!Validator.checkNotNull(this.containerAccess)) {
            LOGGER.error(ExceptionConstants.EMPTY_STORAGE_CONTAINER_ACCESS);
            throw new AzureException(ExceptionConstants.EMPTY_STORAGE_CONTAINER_ACCESS);
        }
    }

    /**
     * {@inheritDoc com.automic.azure.actions.AbstractAction#executeSpecific }. Method makes PUT request to
     * https://myaccount.blob.core.windows.net/mycontainer?restype=container
     * 
     * 
     */
    @Override
    protected ClientResponse executeSpecific(Client storageHttpClient) throws AzureException {

        // get URL
        WebResource resource = storageHttpClient.resource(this.storageAccount.blobURL()).path(containerName);

        this.authenticationService.addCommonHttpHeaders("VERB", "PUT");
        this.authenticationService.addCommonHttpHeaders("Content-Type", "text/plain");
        // header for container access
        if (containerAccess != null && !ContainerAccess.PRIVATE.equals(containerAccess)) {
            this.authenticationService.addStorageHttpHeaders("x-ms-blob-public-access", containerAccess.getValue());
        }
        // add storage HTTP headers
        this.authenticationService.addStorageHttpHeaders("x-ms-version", getOptionValue("xmsversion"));
        this.authenticationService.addStorageHttpHeaders("x-ms-date", CommonUtil.getCurrentUTCDateForStorageService());
        // add query parameters
        this.authenticationService.addQueryParameter("restype", "container");
        // update URI
        String clientURIForSignature = "/" + this.storageAccount.getAccountName() + "/" + containerName;
        this.authenticationService.setURIforSignature(clientURIForSignature);

        // set query parameters
        Map<String, String> queryParameters = this.authenticationService.getQueryParameters();
        for (String headerKey : queryParameters.keySet()) {
            resource = resource.queryParam(headerKey, queryParameters.get(headerKey));
        }

        WebResource.Builder builder = resource.getRequestBuilder();
        // set storage headers
        Map<String, String> storageHttpHeaders = this.authenticationService.getStorageHttpHeaders();
        // calculate Authorization header
        storageHttpHeaders.put("Authorization", this.authenticationService.createAuthorizationHeader());
        for (String headerKey : storageHttpHeaders.keySet()) {
            builder = builder.header(headerKey, storageHttpHeaders.get(headerKey));
        }

        LOGGER.info("Calling URL:" + resource.getURI());
        // call the create container service and return response
        return builder.put(ClientResponse.class, Strings.EMPTY);

    }

    /**
     * 
     * {@inheritDoc com.automic.azure.actions.AbstractAction#prepareOutput} Method publishes the request id returned by
     * the REST api
     * 
     */
    @Override
    protected void prepareOutput(ClientResponse response) throws AzureException {
        List<String> tokenid = response.getHeaders().get(Constants.REQUEST_TOKENID_KEY);
        ConsoleWriter.writeln("UC4RB_AZR_REQUEST_ID  ::=" + tokenid.get(0));
    }

    /**
     * Method overrides validateResponse of {@link AbstractAction} as Error response is of a different namespace than
     * the one returned in abstract class
     */
    @Override
    protected void validateResponse(ClientResponse response) throws AzureException {
        LOGGER.info("Response code for action " + response.getStatus());
        if (!(response.getStatus() >= BEGIN_HTTP_CODE && response.getStatus() < END_HTTP_CODE)) {
            AzureStorageErrorResponse error = response.getEntity(AzureStorageErrorResponse.class);
            StringBuilder responseBuilder = new StringBuilder("Azure Response: ");
            responseBuilder.append("Error Code: [");
            responseBuilder.append(error.getCode()).append("]");
            if (Validator.checkNotEmpty(error.getMessage())) {
                responseBuilder.append(" Message: ").append(error.getMessage());
            }
            throw new AzureException(responseBuilder.toString());
        }
    }
    
    

}
