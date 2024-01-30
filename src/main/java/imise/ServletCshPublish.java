package imise;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServletCshPublish extends HttpServlet {
	private static final long serialVersionUID = 4311758397612594122L;
	protected final static String JSON = "application/json; charset=utf-8";
	final static String HTML = "text/html; charset=utf-8";
	final static String XML = "application/xml; charset=utf-8";

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JsonNode json;
		try {
			json = ServletLdhExport.fetchFromPath(request.getPathInfo());		
			XslPipeline xp = LDHExport.xp;
			ByteArrayOutputStream data = new ByteArrayOutputStream();
			Source source = xp.prepareJson(json);
			xp.pipeToCsh(source, data);
			json = ServletValidate.validate(new ByteArrayInputStream(data.toByteArray()));			

			if (!json.has("validation_errors")) {
				JsonAPI csh = new JsonAPI(System.getProperty("CSH_URL"));		
				csh.getAccessToken(System.getProperty("CLIENT_ID"),System.getProperty("CLIENT_SECRET"),System.getProperty("CSH_TOKEN_URL"));
				JsonNode jsonResponse = csh.postResource("api/resource/", json);
				response.getWriter().print(jsonResponse.toString());
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				response.getWriter().print(json.get("validation_errors").toString());
				response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);				
			}
		} catch (HttpException e) {
			response.sendError(e.getCode(), e.getMessage());
		} catch (IOException | InterruptedException | TransformerException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
}