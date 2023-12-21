package imise;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class JsonAPI {
	private String authorization;
	final String host; 	// scheme+domain part
	HttpClient httpClient;
	final ObjectMapper jsonMapper;
	final static Logger log = LoggerFactory.getLogger(JsonAPI.class);

	/**
	 * New TrustManager; just too ignore certificate ssl certificates validation
	 */
	private static final TrustManager MOCK_TRUST_MANAGER = new X509ExtendedTrustManager() {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
		@Override
		public X509Certificate[] getAcceptedIssuers() {
		       return new java.security.cert.X509Certificate[0];
		}
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
				throws CertificateException {}
		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
				throws CertificateException {}
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
				throws CertificateException {}
		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
				throws CertificateException {}
		};
	
	public JsonAPI(String host) {
		SSLContext sslContext;
		httpClient=null;
		try {
			sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, new TrustManager[]{MOCK_TRUST_MANAGER}, new SecureRandom());
			httpClient = HttpClient.newBuilder().sslContext(sslContext).build();
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // OR TLS
		//httpClient = HttpClient.newBuilder().build();
		// DeserializationFeature necessary as swagger is not 100% comaptible with
		// FAIRDOM
		jsonMapper = JsonMapper.builder().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build();
		this.host = host;
	}
	public void setAuthorization(String token) {
		authorization = token;
	}
	
	
	public void saveToFile(JsonNode j, File file) throws StreamWriteException, DatabindException, IOException {
		jsonMapper.writer(new DefaultPrettyPrinter()).writeValue(file, j);		
	}
	public JsonNode getResource(String path) throws Exception {
	    Builder b = HttpRequest.newBuilder()
	    		.uri(URI.create(host + "/" + path))
				.setHeader("Accept", "application/vnd.api+json");
		if (authorization != null) 
			b.setHeader("Authorization",authorization);
		HttpRequest request = b.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		
		JsonNode responseJson = null;
		switch(response.statusCode()) { 
		case 200:
			responseJson = jsonMapper.readTree(response.body());
			break;			
		case 404:
		default:
			responseJson = jsonMapper.readTree(response.body());
			throw new HttpException(404,"SEEK" + responseJson.at("/errors/0/detail").toString());
		}
		return responseJson;
	}
	public JsonNode postResource(String path, JsonNode j) throws Exception {
	 log.debug(host + "/" + path);
		HttpRequest request = HttpRequest.newBuilder()
				.method("POST",BodyPublishers.ofString(j.toString()))
				.uri(URI.create(host + "/" + path))
				.setHeader("Authorization", authorization)
				.setHeader("Accept", "application/json")
				.setHeader("Content-Type", "application/json")
				.build();
			    
	    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		JsonNode responseJson = null;
		if (response.statusCode() == 200 || response.statusCode() == 201) {
			responseJson = jsonMapper.readTree(response.body());
		} else {
			log.error("Status code: "+response.statusCode());
			log.error("Body: "+response.body());
		}
		return responseJson;
	}
	public JsonNode patchResource(String id,JsonNode j) throws Exception {
	    HttpRequest request = HttpRequest.newBuilder()
				.method("PATCH",BodyPublishers.ofString(j.toString()))
				.uri(URI.create(host + "/" + id))
				.setHeader("Authorization", authorization)
				.setHeader("Accept", "application/json")
				.setHeader("Content-Type", "application/json")
				.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		JsonNode responseJson = null;
		if (response.statusCode() == 200) {
			responseJson = jsonMapper.readTree(response.body());
		} else {
			log.error("Status code: "+response.statusCode());			
		}
		return responseJson;
	}

	public boolean deleteResource(String id) throws Exception {
	    HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(host + "/" + id))
				.setHeader("Authorization", authorization)
				.setHeader("Accept", "application/json")
				.setHeader("Content-Type", "application/json")
				.DELETE()
				.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			log.error("Status code: "+response.statusCode());			
			JsonNode responseJson = jsonMapper.readTree(response.body());
			log.error("Body: "+responseJson.toPrettyString());
			return false;
		}
		return true;
	}
	
	protected String getResourceAsString(String id) throws Exception {
	    Builder b = HttpRequest.newBuilder()
	    		.uri(URI.create(host + "/" + id))
				.setHeader("Accept", "application/json");
		if (authorization != null) 
			b.setHeader("Authorization",authorization);
		HttpRequest request = b.build();
		log.debug(request.toString());
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

	
}
