package middleware.app;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLHandler extends DefaultHandler {

	private boolean descriptionTag = false;
	private boolean gtinTag = false;
	private boolean priceTag = false;
	private boolean supplierTag = false;
	private Date orderDate;
	private int orderId;
	public String orderNumber;

	private HashMap<String, ArrayList<Product>> suppliers;
	private Product product;

	public void startDocument() throws SAXException {

		super.startDocument();
		this.suppliers = new HashMap<>();
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (qName.equalsIgnoreCase("order")) {
			for (int i = 0; i < attributes.getLength(); i++) {
				
				if(attributes.getLocalName(i).equalsIgnoreCase("created")) {
					String text = attributes.getValue(i);
					Timestamp ts = Timestamp.valueOf(text.replace("T", " "));
					this.orderDate = new Date(ts.getTime());
				}
				
				if(attributes.getLocalName(i).equals("ID")) {
					this.orderId = Integer.parseInt(attributes.getValue(i));
					
				}
			}
		}
		
		if(qName.equalsIgnoreCase("product")) {
			this.product = new Product();
		}

		if(qName.equalsIgnoreCase("price")) {
			for (int i = 0; i < attributes.getLength(); i++) {
				if(attributes.getLocalName(i).equalsIgnoreCase("currency")) {
					this.product.setPriceCurrency(attributes.getValue(i));
				}
			}
		}
		
		if (qName.equalsIgnoreCase("description")) {
			this.descriptionTag = true;
		}

		if (qName.equalsIgnoreCase("gtin")) {
			this.gtinTag = true;
		}

		if (qName.equalsIgnoreCase("price")) {
			this.priceTag = true;
		}

		if (qName.equalsIgnoreCase("supplier")) {
			this.supplierTag = true;
		}

	}

	public void characters(char ch[], int start, int length) throws SAXException {

		if (this.descriptionTag) {
			this.product.setDescription(new String(ch, start, length));
			this.descriptionTag = false;
		}

		if (this.gtinTag) {
			this.product.setGtin(new String(ch, start, length));
			this.gtinTag = false;
		}

		if (this.priceTag) {
			double price = Double.parseDouble(new String(ch, start, length));
			this.product.setPrice(price);
			this.priceTag = false;
		}

		if (this.supplierTag) {
			this.product.setSupplier(new String(ch, start, length));
			this.supplierTag = false;
		}

	}

	public void endElement(String uri, String localName, String qName) throws SAXException {

		if(qName.equalsIgnoreCase("product")) {
			this.product.setOrderId(this.orderId);
			this.product.setCreationDate(this.orderDate);
			ArrayList<Product> products = new ArrayList<Product>();
			if(this.suppliers.containsKey(this.product.getSupplier())) {
				products = this.suppliers.get(this.product.getSupplier());
			}
			products.add(this.product);
			this.suppliers.put(this.product.getSupplier(), products);
		}
	}
	
	public void endDocument() {
		for (Map.Entry<String, ArrayList<Product>> entry : this.suppliers.entrySet()) {
		    String supplierName = entry.getKey();
		    ArrayList<Product> products = entry.getValue();
		    Collections.sort(products, new ProductComparator());
		    PrintWriter writer;
			try {
				String newFile = "suppliers\\"+supplierName+this.orderNumber+".xml";
				writer = new PrintWriter(newFile);
				writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				writer.println("<products>");
				for(Product product : products) {
					writer.println("<product>");
					writer.println("<description>"+product.getDescription()+"</description>");
					writer.println("<gtin>"+product.getGtin()+"</gtin>");
					writer.println("<price currency=\""+product.getPriceCurrency()+"\">"+product.getPrice()+"</price>");
					writer.println("<orderid>"+product.getOrderId()+"</orderid>");
					writer.println("</product>");
				}
				writer.println("</products>");
			    writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

}