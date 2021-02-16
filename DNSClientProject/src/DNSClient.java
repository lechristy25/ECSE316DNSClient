import java.io.*;
import java.net.*;
import java.util.stream.Stream;

public class DNSClient {
	int timeout = 5; 
	int retries = 3; 
	int port = 53; 
	String queryFlag = "A";
	byte[] serverIP = new byte[4]; 
	String name; 
	
	public void main(String args[]) throws Exception{
		
		//1- parse input args to get values for all above instance vars
		parseInput(args);
		//2- pass relevant vals to some sort of request builder method
		//3- make socket and send the request
		//4- receive and parse response with some sort of response parser method
		//5- log output
		DatagramSocket clientSocket = new DatagramSocket();
		
	}
	
	private void parseInput(String[] input) {
		for(int i = 0; i < input.length; i ++) {
			String currArg = input[i];
			switch(currArg) {
			case "-t": 
				timeout = Integer.parseInt(input[i+1]); 
				i ++; 
				break;
			case "-r": 
				retries = Integer.parseInt(input[i + 1]);
				i++; 
				break; 
			case "-p": 
				port = Integer.parseInt(input[i + 1]);
				i++; 
				break; 
			case "-mx": 
				queryFlag = "MX";
				break; 
			case "-ns": 
				queryFlag = "NS";
				break;
				
			default: 
				if(currArg.charAt(0) == '@') {
					int[] ipNumbers = Stream.of(currArg.split("\\.")).mapToInt(Integer::parseInt).toArray();
					
					int counter = 0; 
					for(int number : ipNumbers) {
						serverIP[counter] = (byte) number;
						counter++; 
					}
				}else {
					name = currArg; 
				}
			}
			
				
		}
	}
}
