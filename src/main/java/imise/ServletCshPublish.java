package imise;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServletCshPublish extends HttpServlet {
	private static final long serialVersionUID = 4311758397612594122L;
	protected final static String JSON = "application/json; charset=utf-8";
	final static String HTML = "text/html; charset=utf-8";
	final static String XML = "application/xml; charset=utf-8";
	boolean doValidate=false;
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JsonNode jsonSeek;
		JsonNode jsonCsh;
		try {
			if (request.getPathInfo() != null)
				jsonSeek = ServletLdhExport.fetchFromPath(request.getPathInfo());
			else
				jsonSeek = ServletLdhExport.fetch(request.getInputStream());
			
			((ObjectNode)jsonSeek.at("/data/attributes/extended_attributes/attribute_map")).put("Resource_identifier_Project", "3009");
			
			XslPipeline xp = LDHExport.xp;
			ByteArrayOutputStream data = new ByteArrayOutputStream();
			Source source = xp.prepareJson(jsonSeek);
			xp.pipeToCsh(source, data);
			jsonCsh = ServletValidate.validate(new ByteArrayInputStream(data.toByteArray()));
			
			
		
			
			if (jsonCsh.at("/validation_error").size() > 0) {
//				((ObjectNode) jsonCsh).remove("resource");
				((ObjectNode) jsonCsh).set("sender", jsonSeek.get("sender"));
				response.getWriter().print(jsonCsh.toPrettyString());
				response.setContentType(JSON);
				response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);		
				LDHExport.log.debug(jsonSeek.toPrettyString());
				LDHExport.log.debug(jsonCsh.toPrettyString());
			} else {			
				JsonNode jsonResponse;
				JsonAPI csh = new JsonAPI(System.getProperty("CSH_URL"));		
				csh.getAccessToken(System.getProperty("CLIENT_ID"),System.getProperty("CLIENT_SECRET"),System.getProperty("CSH_TOKEN_URL"));
				
				jsonResponse = csh.postResource("api/resource/", jsonCsh);
				LDHExport.log.debug(jsonResponse.toPrettyString());

				String cshIdentifier = jsonResponse.at("/resource/identifier").asText();
				
				
				LDHExport.log.info("Created " + cshIdentifier);
				response.getWriter().print(jsonResponse.toString());
				response.setContentType(JSON);
				response.setStatus(HttpServletResponse.SC_OK);
			}
		} catch (HttpException e) {
			response.sendError(e.getCode(), e.getMessage());
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonHshError = mapper.readTree(e.getMessage());
			LDHExport.log.info(jsonHshError.toPrettyString());
		} catch (IOException | InterruptedException | TransformerException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
}