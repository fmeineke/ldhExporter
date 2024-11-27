package imise;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * http://localhost:8083/ctg/NCT02007772?format=ctg
 * http://localhost:8083/ctg/NCT01234428?format=ctg
 * http://localhost:8083/export/projects/2?format=prettyxml
 * http://localhost:8083/ctg/NCT02007772?format=seekxml
 * http://localhost:8083/ctg/NCT02007772?format=seek
 */
public class ServletLdhImportCTG extends HttpServlet {
	private static final long serialVersionUID = 4311758397612594122L;
	final static String JSON = "application/json; charset=utf-8";
	final static String HTML = "text/html; charset=utf-8";
	final static String XML = "application/xml; charset=utf-8";
	final static Logger log = LoggerFactory.getLogger(LDHExport.class);

	XslPipeline xp = LDHExport.xp;
	
	JsonMapper jsonMapper = JsonMapper.builder().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.build();

	void process(String format, HttpServletResponse response, JsonNode json) throws HttpException {
		if (format == null) format = "ctg";
		ByteArrayOutputStream data = new ByteArrayOutputStream();

		try {
			switch (format) {
			case "ctg": {
				data.write(json.toString().getBytes());
				response.setContentType(JSON);
				break;
			}
			case "ctgxml": {
	            XmlMapper xmlMapper = new XmlMapper();
	            xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true );	            
				xmlMapper.writeValue(data, json);
				response.setContentType(XML);
				break;
			}
			case "seekxml": {
				XmlMapper xmlMapper = new XmlMapper();
	            String xml = xmlMapper.writeValueAsString(json);
	            xp.pipeCtgLdh(new StreamSource(new java.io.StringReader(xml)), data);	            
				response.setContentType(XML);
				break;		
			}
			case "seek": {
				XmlMapper xmlMapper = new XmlMapper();	            
				String xml = xmlMapper.writeValueAsString(json);
	            xp.pipeCtgLdh(new StreamSource(new java.io.StringReader(xml)), data);
          	            
	            json = xmlMapper.readTree(data.toString("UTF-8"));
	            json = removeEmptyFields((ObjectNode) json);
	            json = resolveType(json);
	            data.reset();
				data.write(json.toString().getBytes());
	            response.setContentType(JSON);
				break;
			}
			}
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().print(data.toString("UTF-8"));

		}
		catch(IOException | TransformerException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} 
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			JsonNode json = new ObjectMapper().readTree(request.getInputStream());
			if (json.isEmpty())
				throw new HttpException(0,"empty body / no json content found");
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
		if (pathInfo == null || pathInfo.length()==1) throw new HttpException(0,"missing id");
		return fetch(pathInfo.substring(1));
	}
	public static JsonNode fetch(String id) throws HttpException {
		String ldhUrl = "https://clinicaltrials.gov/api/v2/studies";
		JsonAPI api = new JsonAPI(ldhUrl);
		String url = id;
		return api.getResource(url);
	}
	
	public static JsonNode fetch(InputStream is) throws IOException {
		return new ObjectMapper().readTree(is);
	}

	/**
     * Removes empty fields from the given JSON object node.
     * @param an object node
     * @return the object node with empty fields removed
     * @author <a href="https://technicaldifficulties.io/2018/04/26/using-jackson-to-remove-empty-json-fields">technicaldifficulties</a>
     *  
     */
    public static ObjectNode removeEmptyFields(final ObjectNode jsonNode) {
        ObjectNode ret = new ObjectMapper().createObjectNode();
        Iterator<Entry<String, JsonNode>> iter = jsonNode.fields();

        while (iter.hasNext()) {
            Entry<String, JsonNode> entry = iter.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();

            if (value instanceof ObjectNode) {
                Map<String, ObjectNode> map = new HashMap<String, ObjectNode>();
                map.put(key, removeEmptyFields((ObjectNode)value));
                ret.setAll(map);
            }
            else if (value instanceof ArrayNode) {
                ret.set(key, removeEmptyFields((ArrayNode)value));
            }
            else if (value.asText() != null && !value.asText().isEmpty()) {
                ret.set(key, value);
            }
        }

        return ret;
    }


    /**
     * Removes empty fields from the given JSON array node.
     * @param an array node
     * @return the array node with empty fields removed
     * Source: https://technicaldifficulties.io/2018/04/26/using-jackson-to-remove-empty-json-fields/
     */
    public static ArrayNode removeEmptyFields(ArrayNode array) {
        ArrayNode ret = new ObjectMapper().createArrayNode();
        Iterator<JsonNode> iter = array.elements();

        while (iter.hasNext()) {
            JsonNode value = iter.next();

            if (value instanceof ArrayNode) {
                ret.add(removeEmptyFields((ArrayNode)(value)));
            }
            else if (value instanceof ObjectNode) {
                ret.add(removeEmptyFields((ObjectNode)(value)));
            }
            else if (value != null && !value.textValue().isEmpty()){
                ret.add(value);
            }
        }

        return ret;
    }
    /**
     * Guesses type (Boolean / long) from value - somewhat risky
     * @param jsonNode
     * @return
     * @see <a href="https://stackoverflow.com/questions/50689110/how-to-configure-jackson-xmlmapper-to-use-data-types-from-xml-schema">Stackoverflow</a> 
     */
    public static JsonNode resolveType(JsonNode jsonNode) {
        if (jsonNode instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            Iterator<Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Entry<String, JsonNode> next = fields.next();
                next.setValue(resolveType(next.getValue()));
            }
        } else if (jsonNode instanceof TextNode) {
            TextNode textNode = (TextNode) jsonNode;
            String value = textNode.textValue();
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                jsonNode = BooleanNode.valueOf(Boolean.valueOf(value));
            } else if (StringUtils.isNumeric(value)) {
                jsonNode = LongNode.valueOf(Long.valueOf(value));
            }
        }
        return jsonNode;
    }
}