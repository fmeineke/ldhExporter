package imise;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServletValidate extends HttpServlet {
	private static final long serialVersionUID = 4311758397612594122L;
	protected final static String JSON = "application/json; charset=utf-8";
	final static String HTML = "text/html; charset=utf-8";
	final static String XML = "application/xml; charset=utf-8";
	final static Logger log = LoggerFactory.getLogger(LDHExport.class);


	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JsonNode json;
		try {
			if (request.getMethod().equals("GET") || request.getPathInfo() != null) {
				json = ServletLdhExport.fetchFromPath(request.getPathInfo());
			} else {
				String message = request.getParameter("message"); // textarea content
				if (message.isEmpty()) {
					json = ServletLdhExport.fetch(request.getInputStream());
				} else {
					json = ServletLdhExport.fetchFromString(message);
				}
			}
			XslPipeline xp = LDHExport.xp;
			Source source = xp.prepareJson(json);
			ByteArrayOutputStream data = new ByteArrayOutputStream();
			if (json.has("data")) {		// assume SEEK Format
				xp.pipeToCsh(source, data);
				json = ServletValidate.validate(new ByteArrayInputStream(data.toByteArray()));
			} else { // assume CSH Format
				validate(json);
			}

			response.setContentType(JSON);
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().print(json.toString());
		} catch (HttpException e) {
			response.sendError(e.getCode(), e.getMessage());
		} catch (IOException | TransformerException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}


	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String url = request.getParameter("url");
		if (url != null && !url.isEmpty() && url.matches("ldh.[-.a-z]*.uni-leipzig.de")) {
			System.setProperty("LDH_SOURCE","https://"+url);
			log.info("reading from " + System.getProperty("LDH_SOURCE"));
		}
		doPost(request,response);
	}
	public static void validate(InputStream jsonStream,OutputStream os) throws IOException, HttpException {
		JsonNode json = validate(new ObjectMapper().readTree(jsonStream));
		try (PrintWriter p = new PrintWriter(os)) {
			p.write(json.toPrettyString());
		}
	}

	/**
	 * * Validate the JSON data from the input stream.
	 *
	 * @param jsonStream InputStream containing JSON data
	 * @return JsonNode with validation results
	 * @throws IOException   if there is an error reading the stream or processing
	 *                       the JSON
	 * @throws HttpException if validation fails
	 */
	public static JsonNode validate(InputStream jsonStream) throws IOException, HttpException {
		return validate(new ObjectMapper().readTree(jsonStream));
	}

	private static final ObjectMapper mapper = new ObjectMapper();
	public static JsonNode sortJsonRecursively(JsonNode node) {
		if (node.isObject()) {
			ObjectNode objectNode = (ObjectNode) node;

			// Collect fields into a TreeMap (sorted by key)
			TreeMap<String, JsonNode> sortedMap = new TreeMap<>();
			Iterator<String> fieldNames = objectNode.fieldNames();
			while (fieldNames.hasNext()) {
				String fieldName = fieldNames.next();
				sortedMap.put(fieldName, sortJsonRecursively(objectNode.get(fieldName)));
			}

			// Build new ObjectNode with sorted keys
			ObjectNode sortedNode = mapper.createObjectNode();
			for (String key : sortedMap.keySet()) {
				sortedNode.set(key, sortedMap.get(key));
			}
			return sortedNode;

		} else if (node.isArray()) {
			ArrayNode arrayNode = mapper.createArrayNode();
			for (JsonNode element : node) {
				arrayNode.add(sortJsonRecursively(element));
			}
			return arrayNode;

		} else {
			// Primitive value (string, number, boolean, null)
			return node;
		}
	}
	/**
	 * Validate the JSON data.
	 *
	 * @param json JsonNode containing JSON data
	 * @return JsonNode with validation results
	 * @throws IOException   if there is an error processing the JSON
	 */
	public static JsonNode validate(JsonNode json) throws IOException  {
		// create an instance of the JsonSchemaFactory using version flag
		//		LDHExport.log.debug(json.toPrettyString());
		JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance( SpecVersion.VersionFlag.V202012 );

		//		json = sortJsonRecursively(json);
		// Convenience: If MDS is contained in resource - remove this wrapper
		JsonNode resourceNode = json;
		if (json.has("resource")) {
			resourceNode = json.get("resource");
		}
		// store the JSON data in InputStream
		// download from https://health-study-hub.de/api/MDS_latest_full.json
		String MDSschema =  "MDS_V3_3_1_full_LDH.json";
		/*
		 * ,
                "provenance in 1211 gelöscht
		 */
		try (InputStream schemaStream = ServletLdhExport.class.getClassLoader().getResourceAsStream(MDSschema )) {
			// get schema from the schemaStream and store it into JsonSchema
			JsonSchema schema = schemaFactory.getSchema(schemaStream);
			// create set of validation message and store result in it
			Set<ValidationMessage> validationResult = schema.validate( resourceNode );
			ArrayNode err = ((ObjectNode) json).putArray("validation_error");
			for ( ValidationMessage m : validationResult) {
				err.add(m.getMessage());
			}
			log.info("Validated using " + MDSschema);
			return sortJsonRecursively(json);

		}
	}
}