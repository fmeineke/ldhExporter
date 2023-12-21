package imise;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

public class CshAPI extends JsonCacheAPI {
	String accessToken;

	public CshAPI(String url) {
		super(url);
	}
	public static void validate(InputStream jsonStream,OutputStream os) throws Exception {  

		// create instance of the ObjectMapper class  
		ObjectMapper objectMapper = new ObjectMapper();  
		PrintStream out = new PrintStream(os);

		// create an instance of the JsonSchemaFactory using version flag  
		JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance( SpecVersion.VersionFlag.V202012 );  

		// store the JSON data in InputStream  
		try (InputStream schemaStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "MDS-import.json" )) {

			// read data from the stream and store it into JsonNode  
			JsonNode json = objectMapper.readTree(jsonStream);  

			// get schema from the schemaStream and store it into JsonSchema  
			JsonSchema schema = schemaFactory.getSchema(schemaStream);  

			// create set of validation message and store result in it  
			Set<ValidationMessage> validationResult = schema.validate( json );  

			// show the validation errors   
			if (validationResult.isEmpty()) {  

				// show custom message if there is no validation error   
				out.println( "There are no validation errors" );  

			} else {  

				// show all the validation error  
				out.println("<html>");
				out.println("<h1>CSH Validation Results</h1>");
				out.println("<ul>");
				
				validationResult.forEach(vm -> out.println("<li>"+vm.getMessage()+"</li>"));  
				out.println("</ul></html>");
			}  
		}
	}  			
	public void setClientId(String tokenUrl, String clientId, String clientSecret) throws Exception {
		HttpRequest request = HttpRequest.newBuilder()
				.POST(BodyPublishers.ofString(
						"grant_type=client_credentials" + 
								"&client_id=" + clientId + 
								"&client_secret=" + clientSecret)) 
				.uri(URI.create(tokenUrl))
				.setHeader("Content-Type", "application/x-www-form-urlencoded")
				.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != HttpURLConnection.HTTP_OK) { 
			log.error(response.body());
			throw new Exception("Error in getting access token");
		} 
		JsonNode json = jsonMapper.readTree(response.body());
		accessToken = "Bearer " + json.get("access_token").asText();		
	}
	/**
	 * List all user resources
	 * @throws Exception
	 */
	public void list() throws Exception {
		Builder b = HttpRequest.newBuilder().uri(URI.create(host + "/user/"));
		if (accessToken != null) b.setHeader("Authorization",accessToken);		
		HttpRequest request = b.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		JsonNode json = jsonMapper.readTree(response.body());
		if (json.getNodeType() != JsonNodeType.OBJECT) {
			log.debug("empty list");
		} else for (JsonNode r : json.get("data")) {
			System.out.println(r.toPrettyString());	
			System.out.println(r.at("/resource/resource_identifier") 
					+ ":" + r.at("/resource/resource_titles").get(0).get("text"));
		}
	}


}
