
public class DNSTester {
	public static void main(String args[]) throws Exception{
		String[][] tt = {
	             {"@8.8.8.8", "mcgill.ca"},
	             {"@8.8.8.8", "google.com"},
	             {"-ns", "@8.8.8.8", "mcgill.ca"},
	             {"-mx", "@8.8.8.8", "mcgill.ca"},
	             {"-mx", "@8.8.8.8", "youtube.com"},
	             {"-mx", "@8.8.8.8", "google.com"},
	            {"@8.8.8.8", "fazzzzzzzzzbook.com"}
	        };

	        // String[] tt = {"@8.8.8.8", "mcgill.ca"};

	        // DNSClient client = new DNSClient();
	        // client.DNSClientHandler(tt);
	        
	        for (String[] strings : tt) {
	            System.out.println("_________________");
	            for (String strings2 : strings) {
	                System.out.println(strings2);
	            }
	            System.out.println("_________________");
	            DNSClient client = new DNSClient();
	            client.DNSClientHandler(strings);
	            System.out.println("_________________");
	        }
	    }
	}

