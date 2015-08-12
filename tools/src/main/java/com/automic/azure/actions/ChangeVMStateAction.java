package com.automic.azure.actions;

/**
 * 
 */

import static com.automic.azure.utility.CommonUtil.print;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.StandardLevel;

import com.automic.azure.constants.Constants;
import com.automic.azure.constants.ExceptionConstants;
import com.automic.azure.exceptions.AzureException;
import com.automic.azure.modal.RestartRequestModel;
import com.automic.azure.modal.ShutdownRequestModel;
import com.automic.azure.modal.StartRequestModel;
import com.automic.azure.utility.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * This class will Start, Restart, Shutdown the specified Virtual Machine on
 * Azure Cloud
 * @author  Anurag Upadhyay
 */
public class ChangeVMStateAction extends AbstractAction {

	private static final Logger LOGGER = LogManager
			.getLogger(ChangeVMStateAction.class);

	private static final String SERVICE_OPT = "servicename";
	private static final String SERVICE_DESC = "Azure cloud service name";
	private static final String DEPLOYMENT_OPT = "deploymentname";
	private static final String DEPLOYMENT_DESC = "Azure cloud deployment  name";
	private static final String ROLE_OPT = "rolename";
	private static final String ROLE_DESC = "Role name (VM name)";
	private static final String POST_SHUTDOWN_OPT = "postshutdown";
	private static final String POST_SHUTDOWN_DESC = "Optional. Specifies how the Virtual Machine should be shut down";
	private static final String VM_STATE_OPT = "vmoperation";
	private static final String VM_STATE_DESC = "Specifies the Virtual Machine operations like start, shutdown, restart";

	private String subscriptionId;
	private String serviceName;
	private String deploymentName;
	private String roleName;
	private String postShutdownAction;
	private String vmState;

	@Override
	protected void addOptions() {
		addOption(Constants.SUBSCRIPTION_ID, true, "Subscription ID", true);
		addOption(SERVICE_OPT, true, SERVICE_DESC, true);
		addOption(DEPLOYMENT_OPT, true, DEPLOYMENT_DESC, true);
		addOption(ROLE_OPT, true, ROLE_DESC, true);
		addOption(POST_SHUTDOWN_OPT, false, POST_SHUTDOWN_DESC, true);
		addOption(VM_STATE_OPT, true, VM_STATE_DESC, true);
	}
	
	/*@Override
	protected Options initializeOptions() {
		actionOptions.addOption(Option.builder(Constants.SUBSCRIPTION_ID)
				.required(true).hasArg().desc("Subscription ID").build());
		actionOptions.addOption(Option.builder(SERVICE_OPT).required(true)
				.hasArg().desc(SERVICE_DESC).build());
		actionOptions.addOption(Option.builder(DEPLOYMENT_OPT).required(true)
				.hasArg().desc(DEPLOYMENT_DESC).build());
		actionOptions.addOption(Option.builder(ROLE_OPT).required(true)
				.hasArg().desc(ROLE_DESC).build());
		actionOptions.addOption(Option.builder(POST_SHUTDOWN_OPT)
				.required(false).hasArg().desc(POST_SHUTDOWN_DESC).build());
		actionOptions.addOption(Option.builder(VM_STATE_OPT).required(true)
				.hasArg().desc(VM_STATE_DESC).build());

		return actionOptions;
	}
*/
	@Override
	protected void initialize() {		
		
		serviceName = getOptions().getOptionValue(SERVICE_OPT);
		deploymentName = getOptions().getOptionValue(DEPLOYMENT_OPT);
		roleName = getOptions().getOptionValue(ROLE_OPT);
		postShutdownAction = getOptions().getOptionValue(POST_SHUTDOWN_OPT);
		subscriptionId = getOptions().getOptionValue(Constants.SUBSCRIPTION_ID); 
		vmState = getOptions().getOptionValue(VM_STATE_OPT);
	}

	@Override
	protected void validateInputs() throws AzureException {
		if (!Validator.checkNotEmpty(subscriptionId)) {
			LOGGER.error(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
			throw new AzureException(ExceptionConstants.EMPTY_SUBSCRIPTION_ID);
		}
		if (!Validator.checkNotEmpty(serviceName)) {
			LOGGER.error(ExceptionConstants.EMPTY_SERVICE_NAME);
			throw new AzureException(ExceptionConstants.EMPTY_SERVICE_NAME);
		}
		if (!Validator.checkNotEmpty(deploymentName)) {
			LOGGER.error(ExceptionConstants.EMPTY_DEPLOYMENT_NAME);
			throw new AzureException(ExceptionConstants.EMPTY_DEPLOYMENT_NAME);
		}
		if (!Validator.checkNotEmpty(roleName)) {
			LOGGER.error(ExceptionConstants.EMPTY_ROLE_NAME);
			throw new AzureException(ExceptionConstants.EMPTY_ROLE_NAME);
		}
		if ("SHUTDOWN".equals(vmState.toUpperCase())
				&& !Validator.checkNotEmpty(postShutdownAction)) {
			LOGGER.error(ExceptionConstants.EMPTY_POSTSHUTDOWN_ACTION);
			throw new AzureException(
					ExceptionConstants.EMPTY_POSTSHUTDOWN_ACTION);
		}
		if (!Validator.checkNotEmpty(vmState)) {
			LOGGER.error(ExceptionConstants.EMPTY_VM_OPERATION_ACTION);
			throw new AzureException(
					ExceptionConstants.EMPTY_VM_OPERATION_ACTION);
		}
	}

	@Override
	protected ClientResponse executeSpecific(Client client)
			throws AzureException {
		ClientResponse response = null;
		WebResource webResource = client.resource(Constants.AZURE_MGMT_URL)
				.path(subscriptionId).path("services").path("hostedservices")
				.path(serviceName).path("deployments").path(deploymentName)
				.path("roleinstances").path(roleName).path("Operations");
		LOGGER.info("Calling url " + webResource.getURI());
		response = webResource
				.entity(getRequestBody(vmState), MediaType.APPLICATION_XML)
				.header(Constants.X_MS_VERSION, x_ms_version)
				.post(ClientResponse.class);
		return response;
	}

	/**
	 * This method return the request body object
	 * @param operation
	 * @return Object
	 */
	private Object getRequestBody(String vmState) {
		Object obj = null;
		switch (vmState.toUpperCase()) {
		case "START":
			obj = new StartRequestModel();
			break;
		case "SHUTDOWN":
			obj = new ShutdownRequestModel(postShutdownAction);		
			break;
		case "RESTART":
			obj = new RestartRequestModel();
			break;
		default:
			LOGGER.error(" No rquested operation found for [Start, Shutdown, Restart] virtual machine");
		}
		return obj;
	}

	/**
	 * {@inheritDoc ExecStartAction#prepareOutput(ClientResponse)} it will print
	 * request token id.
	 * 
	 */
	@Override
	protected void prepareOutput(ClientResponse response) throws AzureException {
		List<String> tokenid = response.getHeaders().get(
				Constants.REQUEST_TOKENID_KEY);
		print("UC4RB_AZR_REQUEST_ID  ::=" + tokenid.get(0), LOGGER,
				StandardLevel.INFO);

	}
}
