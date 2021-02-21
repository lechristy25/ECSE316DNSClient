import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Random;

public class RequestPacket {
	String[] name; 
	String queryFlag; 
	byte[] requestPacket; 
	
	public RequestPacket(String name, String queryFlag) {
		System.out.println("name: " + name);
		this.name = name.split("\\."); 
		this.queryFlag = queryFlag; 
	}
	
	public byte[] genRequestPacket() {
		
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); 
		byteStream.writeBytes(genHeader());
		byteStream.writeBytes(genQuestion());
		
		return byteStream.toByteArray();
		
	}
	
	private byte[] genHeader() {
		ByteArrayOutputStream byteStreamHeader = new ByteArrayOutputStream(); 
		Random r = new Random();
		
		byte[] id = new byte[2]; 
		r.nextBytes(id);
		byte[] flags = {(byte) 0x01, (byte) 0x00};
		byte[] qdCount = {(byte) 0x00, (byte) 0x01};
		byte[] anCount = {(byte) 0x00, (byte) 0x00};
		byte[] nsCount = {(byte) 0x00, (byte) 0x00};
		byte[] arCount = {(byte) 0x00, (byte) 0x00};
		
		byteStreamHeader.writeBytes(id);
		byteStreamHeader.writeBytes(flags);
		byteStreamHeader.writeBytes(qdCount);
		byteStreamHeader.writeBytes(anCount);
		byteStreamHeader.writeBytes(nsCount);
		byteStreamHeader.writeBytes(arCount);
		
		return byteStreamHeader.toByteArray();
		
		
//		byte[] header = Arrays.copyOf(id, 12); 
//		Arrays.
	}
	
	private byte[] genQuestion() {
		ByteArrayOutputStream byteStreamQuestion = new ByteArrayOutputStream(); 
		for(String label : name) {
			
			int labelLength = label.length();
			byteStreamQuestion.write((byte) labelLength);
			byteStreamQuestion.writeBytes(genASCIIReps(label));
			
		}
		byteStreamQuestion.write((byte) 0);
		
		byte[] qType = new byte[2];
		qType[0] = (byte) 0x00;
		switch(queryFlag) {
			case "MX":
				qType[1] = (byte) 0x0f ;
				break;
			case "NX":
				qType[1] = (byte) 0x02 ;
				break; 
			default: 
				qType[1] = (byte) 0x01 ;
				break;
		}
		byteStreamQuestion.writeBytes(qType);
		byte[] qClass = {(byte) 0x00, (byte) 0x01};
		byteStreamQuestion.writeBytes(qClass);
		
		return byteStreamQuestion.toByteArray();
	}
	
	private byte[] genASCIIReps(String label) {
		char[] chars = label.toCharArray();
		byte[] reps = new byte[chars.length];
		for(int i = 0; i < chars.length; i ++ ) {
			reps[i] = (byte)((int)chars[i]);
		}
		return reps; 
	}
}
