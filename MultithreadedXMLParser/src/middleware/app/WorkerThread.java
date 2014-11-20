package middleware.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class WorkerThread implements Runnable {

	private File ordersFile;

	public WorkerThread(File ordersFile) {
		this.ordersFile = ordersFile;
	}

	@Override
	public void run() {
		this.parseXML(this.ordersFile);
	}

	private void parseXML(File ordersFile) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			String orderNumber = this.getOrderNumber(ordersFile.getName());
			InputStream inputStream = null;
			boolean isUnlocked = false;
			while(!isUnlocked) { // A file can be hold by another process like 'paste'
				try{
					inputStream = new FileInputStream(ordersFile);
					isUnlocked = true;
				}catch (FileNotFoundException e) {
					Thread.sleep(1000);
				}
			}
			
			InputStreamReader inputReader = new InputStreamReader(inputStream, "UTF-8");
			InputSource inputSource = new InputSource(inputReader);
			inputSource.setEncoding("UTF-8");
			XMLHandler parser = new XMLHandler();
			parser.orderNumber = orderNumber;
			saxParser.parse(inputSource, parser);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getOrderNumber(String fileName) {
		return fileName.replace("orders", "").replace(".xml", "");
	}

}