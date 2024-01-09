package imise;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ExportHandler implements HttpHandler {
	static XslPipeline xp;
	final static String JSON="application/json; charset=utf-8";
	final static String HTML="text/html; charset=utf-8";
	final static String XML="application/xml; charset=utf-8";

	public ExportHandler() {
		xp = new XslPipeline();
	}
	static public Map<String, String> queryToMap(String query) {
		if(query == null) {
			return null;
		}
		Map<String, String> result = new HashMap<>();
		for (String param : query.split("&")) {
			String[] entry = param.split("=");
			if (entry.length > 1) {
				result.put(entry[0], entry[1]);
			}else{
				result.put(entry[0], "");
			}
		}
		return result;
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
	public void error(HttpExchange t,int code, String msg) throws UnsupportedEncodingException, IOException {
		t.getResponseHeaders().set("Content-Type", HTML);
		t.sendResponseHeaders(code,0);
		OutputStream os = t.getResponseBody();
		msg = "<html><h1>" + code + "</h1><p>" + msg + "</p></html>";
		os.write(msg.getBytes());
		os.close();
	}

	OutputStream okStream(HttpExchange t,String contentType) throws IOException {
		t.getResponseHeaders().set("Content-Type", contentType);
		t.sendResponseHeaders(HttpURLConnection.HTTP_OK,0);	
		return t.getResponseBody();
	}
	//		}
	@Override
	public void handle(HttpExchange t) throws IOException {
		try {
			String query = t.getRequestURI().getQuery();			
			if (query == null) {
				byte[] data = getClass().getClassLoader().getResourceAsStream("index.html").readAllBytes();
				String str = new String(data, StandardCharsets.UTF_8);
				str = str.replaceAll("\\$\\{LDH_EXP\\}", Main.service);
				str = str.replaceAll("\\$\\{LDH_SOURCE\\}", Main.source);
				okStream(t,HTML).write(str.getBytes());
			} else {
				Map<String, String> params = queryToMap(query);

				// mainly for debug - reload / recompile stylesheets
				String compile = params.get("compile");			
				if (compile != null) {
					xp = new XslPipeline();
				}
				String format = params.get("format");
				if (format == null)  format = "csh";

				String url = Main.source;
				if (url == null) {
					throw new HttpException(HttpURLConnection.HTTP_BAD_REQUEST, "missing url");
				}
				if (!isValidURL(url)) {
					throw new HttpException(HttpURLConnection.HTTP_BAD_REQUEST, "invalid url");
				}
				JsonNode json;
				switch (t.getRequestMethod()) {
				case "GET": 
					String id = params.get("id");
					if (id == null) {						
						throw new HttpException(HttpURLConnection.HTTP_BAD_REQUEST, "missing id");					
					} 
					if (!id.matches("(investigations|studies)/[0-9]{1,10}")) {
						throw new HttpException(HttpURLConnection.HTTP_BAD_REQUEST, "invalid id");
					} 
					id = id + ".json";
					JsonAPI sa = new JsonAPI(url);
					json = sa.getResource(id);
					break;
				case "POST":
					try {
						json = new ObjectMapper().readTree(t.getRequestBody());	
						if (json.isEmpty())
							throw new HttpException(HttpURLConnection.HTTP_BAD_REQUEST, "empty body / no json content found");
					} catch(IOException e) {
						throw new HttpException(HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
					}
					break;
				default: 
					throw new HttpException(HttpURLConnection.HTTP_BAD_METHOD, "unsupported method");
				}
				Source s = xp.prepareJson(json);	

				switch(format) {
				case "seek":
					okStream(t,JSON).write(new ObjectMapper().writeValueAsBytes(json));;
					break;
				case "seekxml":
					xp.pipeToSeekXml(s, okStream(t,XML));
					break;
				case "xml":
					xp.pipeToXml(s, okStream(t,XML));
					break;
				case "cshxml":
					xp.pipeToCshXml(s, okStream(t,XML));
					break;
				case "csh":
					xp.pipeToCsh(s, okStream(t,JSON));
					break;
				case "fhir":
					xp.pipeToFhir(s, okStream(t,JSON));
					break;
				case "cshval":
					ByteArrayOutputStream data = new ByteArrayOutputStream();
					xp.pipeToCsh(s, data);
					CshAPI.validate(new ByteArrayInputStream(data.toByteArray()),okStream(t,HTML));					
					break;
				case "val":
					CshAPI.validate(json,okStream(t,HTML));					
					break;
				default:
					throw new HttpException(HttpURLConnection.HTTP_NOT_IMPLEMENTED, "format not implemented");
				}
			}
		} catch (HttpException e) {
			error(t,e.code,e.getMessage());
		} catch (Exception e) {
			error(t,HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage());
		} 
		t.getResponseBody().close();
	} 
}