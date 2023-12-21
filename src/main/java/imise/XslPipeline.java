package imise;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class XslPipeline {
	Templates xslJson2SaxonXml;
	Templates xslSeek2Csh;
	Templates xslXml2Json;
	Templates xslCsh2Xml;
	SAXTransformerFactory stf;
	final static Logger log = LoggerFactory.getLogger(XslPipeline.class);
	boolean compiled = false;
	
	public void init() throws TransformerConfigurationException {		
		stf = (SAXTransformerFactory)TransformerFactory.newInstance();
		xslJson2SaxonXml = stf.newTemplates(new StreamSource(
				getClass().getClassLoader().getResourceAsStream("xsl/json2SaxonXml.xsl")));
		xslSeek2Csh = stf.newTemplates(new StreamSource(
				getClass().getClassLoader().getResourceAsStream("xsl/transform.xsl")));
		xslXml2Json = stf.newTemplates(new StreamSource(
				getClass().getClassLoader().getResourceAsStream("xsl/xml2json.xsl")));
		xslCsh2Xml = stf.newTemplates(new StreamSource(
				getClass().getClassLoader().getResourceAsStream("xsl/pretty-xml.xsl")));
		compiled = true;
		log.info("compiled stylesheets");
	}
	void pipeToSaxon(Source input,OutputStream out) throws TransformerException {
		if (!compiled) init();
		TransformerHandler th1 = stf.newTransformerHandler(xslJson2SaxonXml);
		th1.setResult(new StreamResult(out));
		Transformer t = stf.newTransformer();
		t.transform(input, new SAXResult(th1));
		
	}
	/**
	 * 
	 * @param input XML Source
	 * @param out OutputStream, receiving json
	 * @throws TransformerException
	 */	
	void pipeToCsh(Source input,OutputStream out) throws TransformerException {
		if (!compiled) init();
		TransformerHandler th1 = stf.newTransformerHandler(xslJson2SaxonXml);
		TransformerHandler th2 = stf.newTransformerHandler(xslSeek2Csh);
		TransformerHandler th3 = stf.newTransformerHandler(xslXml2Json);
		th1.setResult(new SAXResult(th2));
		th2.setResult(new SAXResult(th3));
		th3.setResult(new StreamResult(out));
		Transformer t = stf.newTransformer();
		t.transform(input, new SAXResult(th1));
		
	}

	void pipeToCshXml(Source input,OutputStream out) throws TransformerException {
		if (!compiled) init();
		TransformerHandler th1 = stf.newTransformerHandler(xslJson2SaxonXml);
		TransformerHandler th2 = stf.newTransformerHandler(xslSeek2Csh);
		th1.setResult(new SAXResult(th2));
		th2.setResult(new StreamResult(out));
		Transformer t = stf.newTransformer();
		t.transform(input, new SAXResult(th1));
		
	}
/**
	 * 
	 * @param input XML Source
	 * @param out OutputStream, receiving xml
	 * @throws TransformerException
	 */	
	void pipeToXml(Source input,OutputStream out) throws TransformerException {
		if (!compiled) init();
		TransformerHandler th1 = stf.newTransformerHandler(xslJson2SaxonXml);
		TransformerHandler th2 = stf.newTransformerHandler(xslSeek2Csh);
		TransformerHandler th4 = stf.newTransformerHandler(xslCsh2Xml);
		th1.setResult(new SAXResult(th2));
		th2.setResult(new SAXResult(th4));
		th4.setResult(new StreamResult(out));
		Transformer t = stf.newTransformer();
		t.transform(input, new SAXResult(th1));
	}
	// Maybe include
	// https://nodedirector.bigsister.ch/refdoc/classorg_1_1json_1_1XML.html		
	// static String org.json.XML.escape	(	String 	string	)	
	String prepareJson(String json) {		
		return "<data>" + json.replace("<", "&lt;") + "</data>";
	}

	Source prepareJson(InputStreamReader jsonStream) throws IOException {
		StringBuilder sb  = new StringBuilder();
		char[] buffer = new char[1024];
		for (int numRead; (numRead = jsonStream.read(buffer, 0, buffer.length)) > 0; ) {
			sb.append(buffer,0,numRead);
		}
		return new StreamSource(prepareJson(sb.toString()));
	}

	Source prepareJson(JsonNode json) {
		return new StreamSource(new StringReader(prepareJson(json.toString())));
	}
}