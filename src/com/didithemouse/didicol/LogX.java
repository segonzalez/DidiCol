package com.didithemouse.didicol;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.util.Log;


public class LogX {

	static SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyhhmmss");
	static Document doc = null;
	
	public static void cleanLogger()
	{
		doc = null;
	}
	
	public static void i(String tag, String msg)
	{
		if(!MochilaContents.LOGGING) return;
		MochilaContents mc = MochilaContents.getInstance();
		
		String filename = mc.getLogDirname() +"/" + mc.getKidNumber() +".xml";
		File file = new File(filename);
		
		if (doc == null)
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			try {
				db = dbf.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				Log.d("LOGX", "FAILED TRANSFORM (1)");
				e.printStackTrace();
				return;
			}
			if(file.exists())
			{
				try {
					doc = db.parse(file);
				} catch (Exception e) {
					Log.d("LOGX", "FAILED TRANSFORM (2)");
					e.printStackTrace();
					return;
				}
			}
			else
			{
				doc = db.newDocument();
				Element root = doc.createElement("logFile");
				root.setAttribute("kidNumber", "" + mc.getKidNumber());
				root.setAttribute("kidName", mc.getKidName());
				doc.appendChild(root);
			}
		}
		Element rootElement = doc.getDocumentElement();
		rootElement.normalize();
		
		Element log = doc.createElement("log");
		log.setAttribute("date", sdf.format(new Date()));
		log.setAttribute("lugar", tag);
		log.setAttribute("mensaje", msg);
		rootElement.appendChild(log);
		
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (Exception e) {	
			Log.d("LOGX", "FAILED TRANSFORM (3)");
			e.printStackTrace();
			return;
		}
		
		try{
			if(file.exists())
				file.delete();
			FileOutputStream out = new FileOutputStream(filename);
			Result output = new StreamResult(out);
			Source input = new DOMSource(doc);
			transformer.transform(input, output);
		}
		catch (TransformerException e){
			Log.d("LOGX", "FAILED TRANSFORM (4) Msg:"+e.getMessage() );
			return;
		}
		catch( Exception e){return;}

	}
}
