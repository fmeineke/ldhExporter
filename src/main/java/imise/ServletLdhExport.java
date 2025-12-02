package imise;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServletLdhExport extends HttpServlet {
	private static final long serialVersionUID = 4311758397612594122L;
	final static String JSON = "application/json; charset=utf-8";
	final static String HTML = "text/html; charset=utf-8";
	final static String XML = "application/xml; charset=utf-8";
	final static Logger log = LoggerFactory.getLogger(LDHExport.class);

	XslPipeline xp = LDHExport.xp;

	JsonMapper jsonMapper = JsonMapper.builder().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.build();

	void process(String format, HttpServletResponse response, JsonNode json) throws HttpException {
		Source source = xp.prepareJson(json);
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		if (format == null) {
			format = "csh";
		}

		try {
			switch (format) {
			case "seek":
				try (PrintWriter p = new PrintWriter(data)) {
					p.write(json.toPrettyString());
				}
				response.setContentType(JSON);
				break;
			case "seekxml":
				xp.pipeToSeekXml(source, data);
				response.setContentType(XML);
				break;
			case "prettyxml":
				XmlMapper xmlMapper = new XmlMapper();
				xmlMapper.configure( ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true );
				response.setContentType(XML);
				response.setStatus(HttpServletResponse.SC_OK);
				xmlMapper.writeValue(response.getWriter(), json);
				return;
				//break;
			case "xml":
				xp.pipeToXml(source,data);
				response.setContentType(XML);
				break;
			case "cshxml":
				xp.pipeToCshXml(source, data);
				response.setContentType(XML);
				break;
			case "fhir":
				xp.pipeToFhir(source, data);
				response.setContentType(XML);
				break;
			case "csh":
				xp.pipeToCsh(source, data);
				response.setContentType(JSON);
				break;
			}
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().print(data.toString("UTF-8"));
		}
		catch(TransformerException | IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			JsonNode json = new ObjectMapper().readTree(request.getInputStream());
			if (json.isEmpty()) {
				throw new HttpException(0,"empty body / no json content found");
			}
			process(request.getParameter("format"),response,json);
		} catch (HttpException e) {
			response.sendError(e.getCode(), e.getMessage());
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			JsonNode json = fetchFromPath(request.getPathInfo());
			process(request.getParameter("format"),response,json);
		} catch (HttpException e) {
			response.sendError(e.getCode(),e.getMessage());
		}
	}

	public static JsonNode fetchFromPath(String pathInfo) throws HttpException {
		if (pathInfo == null || pathInfo.length()==1) {
			throw new HttpException(0,"missing id");
		}
		return fetch(pathInfo.substring(1));
	}
	public static JsonNode fetch(String id) throws HttpException {
		String ldhUrl = System.getProperty("LDH_SOURCE");
		JsonAPI api = new JsonAPI(ldhUrl);
		// id is complete pathInfo like "projects/23"
		return api.getResource(id);
	}

	public static JsonNode fetch(InputStream is) throws IOException {
		return new ObjectMapper().readTree(is);
	}
	public static JsonNode fetchFromString(String s) throws IOException {
		return new ObjectMapper().readTree(s);
	}


}