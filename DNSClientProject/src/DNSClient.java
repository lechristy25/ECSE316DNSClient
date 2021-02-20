import java.net.*;

public class DNSClient {
	static int timeout = 5;
	static int retries = 3;
	static int port = 53;
	static String queryFlag = "A";
	static byte[] serverIP = new byte[4];
	static String name = "";

	private static void parseInput(String[] input) {
		boolean serverFlag = false;
		boolean nameFlag = false;
		for (int i = 0; i < input.length; i++) {
			switch (input[i]) {
				case "-t":
					try {
						timeout = Integer.parseInt(input[i + 1]);
						System.out.println("-t detected: " + timeout);
						i += 1;
					} catch (Exception e) {
						System.out.println("Missing timeout duration value!");
						System.out.println("Rewrite input correctly.");
						return;
					}
					break;
				case "-r":
					try {
						retries = Integer.parseInt(input[i + 1]);
						System.out.println("-r detected: " + retries);
						i += 1;
					} catch (Exception e) {
						System.out.println("Missing number of retries!");
						System.out.println("Rewrite input correctly.");
						return;
					}
					break;
				case "-p":
					try {
						port = Integer.parseInt(input[i + 1]);
						System.out.println("-p detected: " + port);
						i += 1;
					} catch (Exception e) {
						System.out.println("Missing port number!");
						System.out.println("Rewrite input correctly.");
						return;
					}
					break;
				case "-mx":
					if (queryFlag.equals("NS")) {
						System.out.println("Only ONE of -mx or -ns flags allowed!");
						System.out.println("Rewrite input correctly.");
						return;
					}
					System.out.println("-mx detected");
					queryFlag = "MX";
					break;
				case "-ns":
					if (queryFlag.equals("MX")) {
						System.out.println("Only ONE of -mx or -ns flags allowed!");
						System.out.println("Rewrite input correctly.");
						return;
					}
					System.out.println("-ns detected");
					queryFlag = "NS";
					break;

				default:
					if (input[i].charAt(0) == '@') {
						System.out.println("@ detected: " + input[i]);

						// Check a.b.c.d format of the address.
						int commas = 0;
						for (int j = 0; j < input[i].length(); j++) {
							if (input[i].charAt(j) == '.')
								commas += 1;
						}

						if (commas != 3)
							System.out.println("@a.b.c.d IP Adress format not respected!");

						serverFlag = true;
					} else {
						System.out.println("Name detected: " + input[i]);
						nameFlag = true;
					}
					break;
			}
		}

		if ((serverFlag == true) && (nameFlag == true)) {
			System.out.println("All good!");
		} else {
			if (!serverFlag)
				System.out.println("Missing server!");
			if (!nameFlag)
				System.out.println("Missing name!");
		}
		// for(int i = 0; i < input.length; i ++) {
		// String currArg = input[i];
		// switch(currArg) {
		// case "-t":
		// timeout = Integer.parseInt(input[i+1]);
		// i ++;
		// break;
		// case "-r":
		// retries = Integer.parseInt(input[i + 1]);
		// i++;
		// break;
		// case "-p":
		// port = Integer.parseInt(input[i + 1]);
		// i++;
		// break;
		// case "-mx":
		// queryFlag = "MX";
		// break;
		// case "-ns":
		// queryFlag = "NS";
		// break;

		// default:
		// if(currArg.charAt(0) == '@') {
		// int[] ipNumbers =
		// Stream.of(currArg.split("\\.")).mapToInt(Integer::parseInt).toArray();

		// int counter = 0;
		// for(int number : ipNumbers) {
		// serverIP[counter] = (byte) number;
		// counter++;
		// }
		// }else {
		// name = currArg;
		// }
		// }

		// }
	}

	public static void main(String args[]) throws Exception {

		// 1- parse input args to get values for all above instance vars
		String[] test = { "-t", "-r", "22", "-p", "33", "-mx", "@132.206.85.18", "www.mcgill.ca" };

		parseInput(test);
		// 2- pass relevant vals to some sort of request builder method
		RequestPacket currPacket = new RequestPacket(name, queryFlag);
		byte[] request = currPacket.genRequestPacket();
		// 3- make socket and send the request
		DatagramSocket clientSocket = new DatagramSocket();
		// 4- receive and parse response with some sort of response parser method
		// 5- log output

	}

}
