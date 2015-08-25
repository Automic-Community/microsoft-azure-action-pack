package com.automic.azure.config;

import com.automic.azure.exception.AzureException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

/**
 * This class is used to create HTTP Client using specified input parameters. Subsequently client can be used
 * to invoke HTTP operations on resources.
 */

public final class HttpClientConfig {

    private HttpClientConfig() {
    }

    public static Client getClient(String keyStore, String password, int connectionTimeOut, int readTimeOut)
            throws AzureException {        
        ClientConfig config = new DefaultClientConfig();

        config.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, connectionTimeOut);
        config.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, readTimeOut);

        if (keyStore != null && password != null) {
            AzureCertificatesManagement acm = new AzureCertificatesManagement(keyStore, password);

            HTTPSProperties props = new HTTPSProperties(acm.hostnameVerifier(), acm.getSslContext());
            config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, props);
        }

        return Client.create(config);
    }


}
