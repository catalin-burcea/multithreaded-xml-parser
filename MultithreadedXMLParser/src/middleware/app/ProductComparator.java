package middleware.app;

import java.util.Comparator;

public class ProductComparator implements Comparator<Product> {

	@Override
	public int compare(Product product1, Product product2) {

		return product1.getCreationDate().getTime() < product2.getCreationDate().getTime() ? 1 :
			 product1.getCreationDate().getTime() > product2.getCreationDate().getTime() ? -1 : 
			 product1.getPrice() < product2.getPrice() ? 1 : 
			 product1.getPrice() > product2.getPrice() ? -1 : 0;
	}

}
