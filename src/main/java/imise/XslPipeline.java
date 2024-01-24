package imise;

import java.io.IOException;
import java.io.InputStream;
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
	Templates xslJson2Xml;
	Templates xslLdh2Csh;
	Templates xslXml2Json;
	Templates xslCsh2Xml;
	Templates xslToFhir;
	SAXTransformerFactory stf;
	final static Logger log = LoggerFactory.getLogger(XslPipeline.class);
	boolean compiled = false;

	
	InputStream loadResource(String name) {
		InputStream is=null;
		is = getClass().getClassLoader().getResourceAsStream(name);
		/*
		try {
			
//			is = new FileInputStream("C:\\Users\\Frank\\git\\ldhExporter\\src\\main\\resources\\" + name);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		log.info(name);
		
		if (is == null )log.debug("failed to load " + name);
		return is;
	}
	public void init() throws TransformerConfigurationException {
		try {
			stf = (SAXTransformerFactory) TransformerFactory.newInstance();
			log.debug("compiling xslJson2Xml");
			xslJson2Xml = stf.newTemplates(new StreamSource(loadResource("xsl/json2xml.xsl")));
			log.debug("compiling xslLdh2Csh");
			xslLdh2Csh = stf.newTemplates(new StreamSource(loadResource("xsl/ldh2csh.xsl")));
			log.debug("compiling xslXml2Json");
			xslXml2Json = stf.newTemplates(new StreamSource(loadResource("xsl/xml2json.xsl")));
			log.debug("compiling xslCsh2Xml");
			xslCsh2Xml = stf.newTemplates(new StreamSource(loadResource("xsl/pretty-xml.xsl")));
			log.debug("compiling xslToFhir");
			xslToFhir = stf.newTemplates(new StreamSource(loadResource("xsl/toFhir.xsl")));
		} catch (TransformerConfigurationException e) {
			log.debug(e.getMessageAndLocation());
		}
		compiled = true;
		log.info("compiled stylesheets");
	}

	void pipeToSeekXml(Source input, OutputStream out) throws TransformerException {
		if (!compiled)
			init();
		TransformerHandler th1 = stf.newTransformerHandler(xslJson2Xml);
		th1.setResult(new StreamResult(out));
		Transformer t = stf.newTransformer();
		t.transform(input, new SAXResult(th1));

	}

	/**
	 * 
	 * @param input XML Source
	 * @param out   OutputStream, receiving json
	 * @throws TransformerException
	 */
	void pipeToCsh(Source input, OutputStream out) throws TransformerException {
		if (!compiled)
			init();
		TransformerHandler th1 = stf.newTransformerHandler(xslJson2Xml);
		TransformerHandler th2 = stf.newTransformerHandler(xslLdh2Csh);
		TransformerHandler th3 = stf.newTransformerHandler(xslXml2Json);
		th1.setResult(new SAXResult(th2));
		th2.setResult(new SAXResult(th3));
		th3.setResult(new StreamResult(out));
		Transformer t = stf.newTransformer();
		t.transform(input, new SAXResult(th1));

	}

	void pipeToCshXml(Source input, OutputStream out) throws TransformerException {
		if (!compiled)
			init();
		TransformerHandler th1 = stf.newTransformerHandler(xslJson2Xml);
		TransformerHandler th2 = stf.newTransformerHandler(xslLdh2Csh);
		th1.setResult(new SAXResult(th2));
		th2.setResult(new StreamResult(out));
		Transformer t = stf.newTransformer();
		t.transform(input, new SAXResult(th1));

	}

	/**
	 * 
	 * @param input XML Source
	 * @param out   OutputStream, receiving xml
	 * @throws TransformerException
	 */
	void pipeToXml(Source input, OutputStream out) throws TransformerException {
		if (!compiled)
			init();
		TransformerHandler th1 = stf.newTransformerHandler(xslJson2Xml);
		TransformerHandler th2 = stf.newTransformerHandler(xslLdh2Csh);
		TransformerHandler th4 = stf.newTransformerHandler(xslCsh2Xml);
		th1.setResult(new SAXResult(th2));
		th2.setResult(new SAXResult(th4));
		th4.setResult(new StreamResult(out));
		Transformer t = stf.newTransformer();
		t.transform(input, new SAXResult(th1));
	}

	void pipeToFhir(Source input, OutputStream out) throws TransformerException {
		if (!compiled)
			init();
		TransformerHandler th1 = stf.newTransformerHandler(xslJson2Xml);
		TransformerHandler th2 = stf.newTransformerHandler(xslLdh2Csh);
		TransformerHandler th3 = stf.newTransformerHandler(xslCsh2Xml);
		TransformerHandler th4 = stf.newTransformerHandler(xslToFhir);
		th1.setResult(new SAXResult(th2));
		th2.setResult(new SAXResult(th3));
		th3.setResult(new SAXResult(th4));
		th4.setResult(new StreamResult(out));
		Transformer t = stf.newTransformer();
		t.transform(input, new SAXResult(th1));
	}

	// Maybe include
	// https://nodedirector.bigsister.ch/refdoc/classorg_1_1json_1_1XML.html
	// static String org.json.XML.escape ( String string )
	String prepareJson(String json) {
		return "<data>" + json.replace("<", "&lt;") + "</data>";
	}

	Source prepareJson(InputStreamReader jsonStream) throws IOException {
		StringBuilder sb = new StringBuilder();
		char[] buffer = new char[1024];
		for (int numRead; (numRead = jsonStream.read(buffer, 0, buffer.length)) > 0;) {
			sb.append(buffer, 0, numRead);
		}
		return new StreamSource(prepareJson(sb.toString()));
	}

	Source prepareJson(JsonNode json) {
		return new StreamSource(new StringReader(prepareJson(json.toString())));
	}
}
