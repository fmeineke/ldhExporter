package imise;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class JsonCacheAPI extends JsonAPI {
	// cache, all retrieved nodes are mapped to their id
	Map<String, JsonNode> jsonCache = new HashMap<>();
	final static Logger log = LoggerFactory.getLogger(JsonCacheAPI.class);

	public JsonCacheAPI(String url) throws HttpException {
		super (url);
	}

	public void listMap() {
		for (String id : jsonCache.keySet()) {
			JsonNode j = jsonCache.get(id);
			String name = j.at("/data/attributes/title").asText();
			System.out.println(id + ": " + name);			
		}
	}
	@Override
	public JsonNode getResource(String path) throws HttpException {
		if (jsonCache.containsKey(path))
			return jsonCache.get(path);
		
		JsonNode j = super.getResource(path);
		if (j != null) 
			jsonCache.put(path, j);
		return j;
	}

	@Override
	public JsonNode postResource(String path,JsonNode j) throws HttpException {
		JsonNode nodeNew = super.postResource(path, j);
		if (nodeNew != null)
			jsonCache.put(path, nodeNew);
		return nodeNew;
	}
//	@Override
//	public JsonNode patchResource(String path,JsonNode j) throws JsonAPIException {
//		JsonNode nodeNew = super.patchResource(path, j);
//		if (nodeNew != null)
//			jsonCache.put(path, nodeNew);
//		return nodeNew;
//	}
//	@Override
//	public boolean deleteResource(String path) throws JsonAPIException {
//		boolean ret = super.deleteResource(path);
//		if (ret) jsonCache.remove(path);
//		return ret;
//	}
	
}
