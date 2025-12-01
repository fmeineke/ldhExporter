package imise;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class JsonAPI {
	private String authorization;
	final String host; 	// scheme+domain part
	protected HttpClient httpClient;
	protected final ObjectMapper jsonMapper;
	protected final static Logger log = LoggerFactory.getLogger(JsonAPI.class);

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

	public JsonAPI(String host) throws HttpException {
		assert host != null;
		if (!isValidURL(host)) throw new HttpException(0,"Invalid Url");
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
	public JsonNode parse(String s) throws IOException {
		return jsonMapper.readTree(s);
	}
	public void setAuthorization(String token) {
		authorization = token;
	}
	public boolean hasAuthorization() {
		return authorization != null;
	}


	public void saveToFile(JsonNode j, File file) throws StreamWriteException, DatabindException, IOException {
		jsonMapper.writer(new DefaultPrettyPrinter()).writeValue(file, j);		
	}
	public String getResourceAsString(String id) throws HttpException {
		String url = host + "/" + id;
		Builder b = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.setHeader("Accept", "application/json");
		if (hasAuthorization()) 
			b.setHeader("Authorization",authorization);
		HttpRequest request = b.build();
		log.debug(request.toString());
		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == HttpURLConnection.HTTP_OK) {
				return response.body();
			}
			throw new HttpException(response.statusCode(),response.body());		
		} catch (IOException e) {
			throw new HttpException(0,e.getMessage());		
		} catch (InterruptedException e) {
			throw new HttpException(0,e.getMessage());		
		}
	}

	//	public String getResourceAsString(String id) throws Exception {
	//	    Builder b = HttpRequest.newBuilder()
	//	    		.uri(URI.create(host + "/" + id))
	//				.setHeader("Accept", "application/json");
	//		if (hasAuthorization()) 
	//			b.setHeader("Authorization",authorization);
	//		HttpRequest request = b.build();
	//		log.debug(request.toString());
	//		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	//		switch(response.statusCode()) { 
	//		case 200:
	//			break;			
	//		case 404:
	//		default:
	//			JsonNode responseJson = jsonMapper.readTree(response.body());
	//			throw new HttpException(404,"SEEK" + responseJson.at("/errors/0/detail").toString());
	//		}
	//		return response.body();
	//	}

	public JsonNode getResource(String path) throws HttpException {
		try {
			String s = getResourceAsString(path);
			// TODO Not proud of it.. but if json contains & json-to-xml would fail
			// 1.12.2025: found this u0026 for first time; remarkable 4-fold quoting necessary
			s = s.replaceAll("&|\\\\u0026", "&amp;");
			return jsonMapper.readTree(s);
		} catch(HttpException e) {
			throw e;
		} catch (JsonMappingException e) {
			throw new HttpException(0,e.getMessage());
		} catch (JsonProcessingException e) {
			throw new HttpException(0,e.getMessage());
		} 
	}

	public JsonNode postResource(String path, JsonNode j) throws HttpException {
		log.debug(host + "/" + path);
		log.debug(j.toPrettyString());
		HttpRequest request = HttpRequest.newBuilder()
				.method("POST",BodyPublishers.ofString(j.toString()))
				.uri(URI.create(host + "/" + path))
				.setHeader("Authorization", authorization)
				.setHeader("Accept", "application/json")
				.setHeader("Content-Type", "application/json")
				.build();

		try {
			HttpResponse<String>response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == HttpURLConnection.HTTP_OK || response.statusCode() == HttpURLConnection.HTTP_CREATED) {
				return jsonMapper.readTree(response.body());
			}
			throw new HttpException(response.statusCode(),response.body());		
		} catch (IOException e) {
			throw new HttpException(0,e.getMessage());		
		} catch (InterruptedException e) {
			throw new HttpException(0,e.getMessage());		
		}
	}
	public void getAccessToken(String clientId, String clientSecret,String tokenUrl) throws IOException, InterruptedException, HttpException  {
		if (!isValidURL(tokenUrl))
			throw new HttpException(0,"Invalid token Url");
		HttpRequest request = HttpRequest.newBuilder()
				.POST(BodyPublishers.ofString(
						"grant_type=client_credentials" + 
								"&client_id=" + clientId + 
								"&client_secret=" + clientSecret)) 
				.uri(URI.create(tokenUrl))
				.setHeader("Content-Type", "application/x-www-form-urlencoded")
				.build();    
		HttpResponse<String>response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		log.debug(""+response.statusCode());
		log.debug(response.body().toString());
		JsonNode json = jsonMapper.readTree(response.body());
		if (response.statusCode() != HttpURLConnection.HTTP_OK ) 
			throw new HttpException(response.statusCode(), json.get("error_description").asText());
		authorization = "Bearer " + json.get("access_token").asText();		
	}
		
	static boolean isValidURL(String url)  {
		try {
			new URL(url).toURI();
			return true;
		} catch (MalformedURLException e) {
			return false;
		} catch (URISyntaxException e) {
			return false;
		}
	}	


	//	public JsonNode patchResource(String id,JsonNode j) throws JsonAPIException {
	//	    HttpRequest request = HttpRequest.newBuilder()
	//				.method("PATCH",BodyPublishers.ofString(j.toString()))
	//				.uri(URI.create(host + "/" + id))
	//				.setHeader("Authorization", authorization)
	//				.setHeader("Accept", "application/json")
	//				.setHeader("Content-Type", "application/json")
	//				.build();
	//		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	//		JsonNode responseJson = null;
	//		if (response.statusCode() == 200) {
	//			responseJson = jsonMapper.readTree(response.body());
	//		} else {
	//			log.error("Status code: "+response.statusCode());			
	//		}
	//		return responseJson;
	//	}

	public void deleteResource(String id) throws HttpException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(host + "/" + id))
				.setHeader("Authorization", authorization)
				.setHeader("Accept", "application/json")
				.setHeader("Content-Type", "application/json")
				.DELETE()
				.build();
		try {
			HttpResponse<String>response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == HttpURLConnection.HTTP_OK ) return;
			throw new HttpException(response.statusCode(),response.body());		
		} catch (IOException e) {
			throw new HttpException(0,e.getMessage());		
		} catch (InterruptedException e) {
			throw new HttpException(0,e.getMessage());		
		}
	}
}
