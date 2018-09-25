package com.net128.test.proxy.proxytest;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class RestProxyTemplate extends RestTemplate {
    private String proxyHost;
    private int proxyPort;
    private String proxyUser;
    private String proxyPassword;
    private boolean trustAllSsl;

    public RestProxyTemplate() {
        super();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        setRequestFactory(factory);
    }
    public RestProxyTemplate(boolean trustAllSsl) {
        this();
        this.trustAllSsl=trustAllSsl;
    }
    public RestProxyTemplate(String proxyUrl, boolean trustAllSsl) {
        this(trustAllSsl);
        try {
            init(proxyUrl);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred initializing", e);
        }
    }

    private void init(String proxyUrl) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        if(proxyUrl != null) {
            parseUri(proxyUrl);
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(proxyHost, proxyPort),
                    new UsernamePasswordCredentials(proxyUser, proxyPassword));
            clientBuilder.useSystemProperties();
            clientBuilder.setProxy(new HttpHost(proxyHost, proxyPort));
            clientBuilder.setDefaultCredentialsProvider(credsProvider);
            clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
        }

        if(trustAllSsl) {
            SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, (certificate, authType) -> true)
                .build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
            clientBuilder.setSSLSocketFactory(csf);
            clientBuilder.setSSLHostnameVerifier(new NoopHostnameVerifier());
        }
        CloseableHttpClient client = clientBuilder.build();

        HttpComponentsClientHttpRequestFactory rf =
                (HttpComponentsClientHttpRequestFactory) getRequestFactory();
        rf.setHttpClient(client);
        setRequestFactory(rf);
    }

    //TODO paramterrize key and trust stores

//    SSLContext sslContext = SSLContextBuilder
//            .create()
//            .loadKeyMaterial(ResourceUtils.getFile("classpath:keystore.jks"), allPassword.toCharArray(), allPassword.toCharArray())
//            .loadTrustMaterial(ResourceUtils.getFile("classpath:truststore.jks"), allPassword.toCharArray())
//            .build();
//
//    HttpClient client = HttpClients.custom()
//            .setSSLContext(sslContext)
//            .build();
//
//        return builder
//                .requestFactory(new HttpComponentsClientHttpRequestFactory(client))
//            .build();

//    private void registerKeyStore(String keyStoreName) {
//        try {
//            ClassLoader classLoader = this.getClass().getClassLoader();
//            InputStream keyStoreInputStream = classLoader.getResourceAsStream(keyStoreName);
//            if (keyStoreInputStream == null) {
//                throw new FileNotFoundException("Could not find file named '" + keyStoreName + "' in the CLASSPATH");
//            }
//
//            //load the keystore
//            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
//            keystore.load(keyStoreInputStream, null);
//
//            //add to known keystore
//            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//            trustManagerFactory.init(keystore);
//
//            //default SSL connections are initialized with the keystore above
//            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
//            SSLContext sc = SSLContext.getInstance("SSL");
//            sc.init(null, trustManagers, null);
//            SSLContext.setDefault(sc);
//        } catch (IOException | GeneralSecurityException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private void parseUri(String uriString) {
        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
           throw new RuntimeException("Unable to parse: "+uriString, e);
        }
        String authority = uri.getAuthority();
        if (authority != null) {
            int pos = authority.lastIndexOf("@");

            if (pos>0) {
                String userPass=authority.substring(0, pos);
                String [] userInfo = userPass.split(":");
                this.proxyUser = userInfo[0];
                if(userInfo.length>1) {
                    this.proxyPassword=userInfo[1];
                }
            }
        }

        proxyHost = uri.getHost();
        proxyPort = uri.getPort();
    }
}