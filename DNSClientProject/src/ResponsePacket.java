import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;

public class ResponsePacket {
    // ByteBuffer NAME = ByteBuffer.allocate(10);
    // ByteBuffer TYPE = ByteBuffer.allocate(2);
    // ByteBuffer CLASS = ByteBuffer.allocate(2);
    // ByteBuffer TTL = ByteBuffer.allocate(4);
    // ByteBuffer RDLENGTH = ByteBuffer.allocate(2);
    // ByteBuffer RDATA = ByteBuffer.allocate(2);

    private boolean QR, AA, TC, RD, RA;
    private int RCODE, QDCOUNT, ANCOUNT, NSCOUNT, ARCOUNT;
    private int OFFSET, RTYPE, RCLASS, TTL, RDLENGTH;

    private int MXOffset, NSOffset, CNOffset;

    private String Name = "";
    private String IPAddress = "";

    byte[] response;
    String queryFlag;

    // Get packet and add
    public ResponsePacket(byte[] response, int size, String queryFlag) {
        // int counter = 0;
        // for (byte b : response) {
        // System.out.println(b);
        // if (counter == 250)
        // return;
        // counter++;
        // }
        this.response = response;
        this.queryFlag = queryFlag;

        getHeader();
        // Check header
        if (QR) {
            if (RCODE == 0) {
                getResponse();
                System.out.println("***Answer Section (" + ANCOUNT + " records)***");
                if (RTYPE == 1) {
                    System.out.println("IP\t" + IPAddress + "\t" + TTL + "\t" + auth(AA));
                }
                if (RTYPE == 15) {
                    System.out.println("MX\t" + IPAddress + "\t" + TTL + "\t" + auth(AA));
                    // getDName(29);
                }
                // if (ANCOUNT > 1) {
                // for (byte b : response) {
                // System.out.println(b);
                // }
                // }
                // for (int i = 0; i < ANCOUNT; i++) {
                // getResponse();
                // }
            }
        } else {
            System.out.println("ERROR: This is not a response.");
        }

        // System.out.println(response);

        // Check if response is a response by checking QR bit
        // if (isAResponse(response[2])) {
        // Header.
        // System.out.println("Response: " + isAResponse(response[2]));
        // System.out.println("Authority: " + authority(response[2]));
        System.out.println("Truncated: " + truncated(response[2]));
        // System.out.println("Recursion Request: " + recursionRequest(response[2]));
        // System.out.println("Recursion Response: " + recursionResponse(response[3]));
        // errorCode(response[3]);

        // System.out.println("QDCOUNT: " + (response[4] + response[5]));
        // System.out.println("ANCOUNT: " + (response[6] + response[7]));
        // System.out.println("NSCOUNT: " + (response[8] + response[9]));
        System.out.println("ARCOUNT: " + (response[10] + response[11]));

        // Check if RA bit is different
        // } else {
        // }

        // Parse the response.
    }

    private boolean isAResponse(byte data) {
        return (getBit(data, 7) == 1);
    }

    private boolean authority(byte data) {
        return (getBit(data, 2) == 1);
    }

    private String auth(boolean auth) {
        return ((auth) ? "auth" : "nonauth");
    }

    private boolean truncated(byte data) {
        return (getBit(data, 1) == 1);
    }

    private boolean recursionRequest(byte data) {
        return (getBit(data, 0) == 1);
    }

    private boolean recursionResponse(byte data) {
        return (getBit(data, 7) == 1);
    }

    private String getDName(int off) {
        LinkedList<Byte> byteList = new LinkedList<Byte>();
        String s = "";
        int i = off;
        while (response[i] != 0) {
            s += ((char) (response[i] & 0xFF));
            // System.out.println(response[i] & 0xFF);
            // byteList.add(response[i]);
            i += 1;
        }
        System.out.println(s);
        return s;
        // String t = new String(byteList.toArray());
        // return new String(byteList.toArray());
    }

    private int errorCode(byte data) {
        int errorCode = 0;
        data = (byte) (data << 4);
        data = (byte) (data >> 4);
        switch (data) {
            case 1:
                System.out.println("RCODE 1: Format error: the name server was unable to interpret the query");
                errorCode = 1;
                break;
            case 2:
                System.out.println(
                        "RCODE 2: Server failure: the name server was unable to process this query due to a problem with the name server");
                errorCode = 2;
                break;
            case 3:
                System.out.println("RCODE 3: NOTFOUND");
                errorCode = 3;
                break;
            case 4:
                System.out.println(
                        "RCODE 4: Not implemented: the name server does not support the requested kind of query");
                errorCode = 4;
                break;
            case 5:
                System.out.println(
                        "RCODE 5: Refused: the name server refuses to perform the requested operation for policy reasons");
                errorCode = 5;
                break;

            default:
                System.out.println("RCODE 0: No error");
                break;
        }
        return errorCode;
    }

    void getHeader() {
        QR = isAResponse(response[2]);
        AA = authority(response[2]);
        TC = truncated(response[2]);
        RD = recursionRequest(response[2]);
        RA = recursionResponse(response[3]);
        RCODE = errorCode(response[3]);
        QDCOUNT = ((response[4] * 256) + response[5]);
        ANCOUNT = ((response[6] * 256) + response[7]);
        NSCOUNT = ((response[8] * 256) + response[9]);
        ARCOUNT = ((response[10] * 256) + response[11]);

        // System.out.println("Response: " + isAResponse(response[2]));
        // System.out.println("Authority: " + authority(response[2]));
        // System.out.println("Truncated: " + truncated(response[2]));
        // System.out.println("Recursion Request: " + recursionRequest(response[2]));
        // System.out.println("Recursion Response: " + recursionResponse(response[3]));
        // // errorCode(response[3]);

        // System.out.println("QDCOUNT: " + (response[4] + response[5]));
        // System.out.println("ANCOUNT: " + (response[6] + response[7]));
        // System.out.println("NSCOUNT: " + (response[8] + response[9]));
        // System.out.println("ARCOUNT: " + (response[10] + response[11]));
    }

    void getResponse() {
        int index = 12;
        while (response[index] != 0) {
            index++;
        }
        index++;

        OFFSET = response[index + 5];
        RTYPE = ((response[index + 6] * 256) + response[index + 7]);
        RCLASS = ((response[index + 8] * 256) + response[index + 9]);
        byte[] ttlByte = { response[index + 10], response[index + 11], response[index + 12], response[index + 13] };
        TTL = TTLInteger(ttlByte);
        RDLENGTH = ((response[index + 14] * 256) + response[index + 15]);
        byte[] ipByte = { response[index + 16], response[index + 17], response[index + 18], response[index + 19] };
        IPAddress = IPString(ipByte);

        switch (RTYPE) {
            case 1:

                break;
            case 2:
                MXOffset = index + 17;
                getMXResponse();

                break;
            case 5:

                break;
            case 15:
                MXOffset = index + 17;
                getMXResponse();
                // System.out.println(MXOffset);
                // System.out.println(response[MXOffset + 0]);
                // System.out.println(response[MXOffset + 1]);
                // System.out.println(response[MXOffset + 2]);
                // System.out.println(response[MXOffset + 3]);
                // System.out.println(response[MXOffset + 4]);
                // System.out.println(response[MXOffset + 5]);
                // System.out.println(response[MXOffset + 6]);
                // System.out.println(response[MXOffset + 7]);
                // System.out.println(response[MXOffset + 8]);
                // System.out.println(response[MXOffset + 9]);
                // System.out.println(response[MXOffset + 10]);
                // System.out.println(response[MXOffset + 11]);
                // System.out.println(response[MXOffset + 12]);
                // System.out.println(response[MXOffset + 13]);
                // System.out.println(response[MXOffset + 14]);
                // System.out.println(response[MXOffset + 15]);
                // System.out.println(response[MXOffset + 16]);
                // System.out.println(response[MXOffset + 17]);
                // System.out.println(response[MXOffset + 18]);
                // System.out.println(response[MXOffset + 19]);
                // System.out.println(response[MXOffset + 20]);
                // System.out.println(response[MXOffset + 21]);
                // System.out.println(response[MXOffset + 22]);
                // System.out.println(response[MXOffset + 23]);
                // System.out.println(response[MXOffset + 24]);
                // System.out.println(response[MXOffset + 25]);
                // System.out.println(response[MXOffset + 26]);
                // System.out.println(response[MXOffset + 27]);
                // System.out.println(response[MXOffset + 28]);
                // System.out.println(response[MXOffset + 29]);
                break;

            default:
                break;
        }

        // System.out.println(response[index + 20]);
        // System.out.println(index + 20);
        // System.out.println("QTYPE: " + (response[index] + response[index + 1]));
        // System.out.println("QCLASS: " + (response[index + 2] + response[index + 3]));
        // System.out.println("OFFSET: " + response[index + 5]);
        // System.out.println("RTYPE: " + (response[index + 6] + response[index + 7]));
        // System.out.println("RCLASS: " + (response[index + 8] + response[index + 9]));
        // System.out.println(
        // "TTL: " + (response[index + 10] + response[index + 11] + response[index + 12]
        // + response[index + 13]));
        // System.out.println("RDLENGTH: " + (response[index + 14] + response[index +
        // 15]));
        // System.out.println("IP: " + (response[index + 16] + 256) + "." +
        // (response[index + 17] + 256) + "."
        // + (response[index + 18] + 256) + "." + (response[index + 19] + 256));

        // int a = getHex(response[index + 12], 0);
        // int b = getHex(response[index + 13], 1);
        // int c = getHex(response[index + 13], 0);

        // System.out.println(a + "." + b + "." + c);
    }

    // public void printP() {
    // System.out.println("FUCL");
    // }

    private int TTLInteger(byte[] data) {
        int ttl = 0;
        ttl |= data[0] & 0xFF;
        ttl <<= 8;
        ttl |= data[1] & 0xFF;
        ttl <<= 8;
        ttl |= data[2] & 0xFF;
        ttl <<= 8;
        ttl |= data[3] & 0xFF;
        return ttl;
    }

    private String IPString(byte[] data) {
        return ((response[0] + 256) + "." + (response[1] + 256) + "." + (response[2] + 256) + "."
                + (response[3] + 256));
    }

    private int getBit(byte data, int index) {
        return (data >> index) & 1;
    }

    private int getHex(byte data, int index) {
        if (index == 0) {
            data = (byte) (data << 4);
            data = (byte) ((data >> 4) & 1);
            return data;
        }
        return 0;
    }

    private void getMXResponse() {
        int alias = 0;
        String url = "";
        int temp = MXOffset;
        int count = 1;

        int[] pattern = { 0, RTYPE, 0, 1 };
        ArrayList<Byte> byteList = new ArrayList<Byte>();

        while (count < ANCOUNT) {
            for (int i = temp; i < response.length - 4; i++) {
                if ((pattern[0] == response[i]) && (pattern[1] == response[i + 1]) && (pattern[2] == response[i + 2])
                        && (pattern[3] == response[i + 3])) {
                    for (int j = temp; j < i; j++) {
                        byteList.add(response[j]);
                    }
                    System.out.println("__________");
                    printBytes(byteList);
                    temp = i + 11;
                    break;
                }
            }
            byteList.clear();
            count++;
        }

        int[] bpattern = { 0, 0, 0, 0 };
        for (int i = temp; i < response.length; i++) {
            if ((bpattern[0] == response[i]) && (bpattern[1] == response[i + 1]) && (bpattern[2] == response[i + 2])
                    && (bpattern[3] == response[i + 3])) {
                break;
            }
            byteList.add(response[i]);
        }
        // for (int i = temp; i < response.length - 7; i++) {
        System.out.println("__________");
        printBytes(byteList);
        // if ((pattern[0] == response[i]) && (pattern[1] == response[i + 1]) &&
        // (pattern[2] == response[i + 2])
        // && (pattern[3] == response[i + 3]) && (pattern[4] == response[i + 4])
        // && (pattern[5] == response[i + 5]) && (pattern[6] == response[i + 6])) {
        // for (int j = temp; j < i; j++) {
        // byteList.add(response[j]);
        // }
        // System.out.println("__________");
        // printBytes(byteList);
        // break;
        // }
        // }

        // while (count < ANCOUNT) {
        // urlByteExtract(temp);
        // break;
        // // temp += 11;
        // // count += 1;
        // }

        // for (int i = MXOffset + 1; i < response.length; i++) {
        // String c = "" + ((char) (response[i] & 0xFF));
        // if (c.matches("\\A\\p{ASCII}*\\z")) {
        // url += c;
        // }
        // }
        // System.out.println(url);
        // System.out.println("MX" + "\t" + url + "\t" + response[MXOffset] + "\t" + TTL
        // + "\t" + auth(AA));
    }

    private void printBytes(ArrayList<Byte> byteList) {
        for (Byte b : byteList) {
            System.out.println(b);
        }
    }

    private void urlByteExtract(int offset) {
        // int[] pattern = { 0, 15, 0, 1, 0, 0, 0 };
        // ArrayList<Byte> byteList = new ArrayList<Byte>();
        // for (int i = offset; i < response.length - 7; i++) {
        // if ((pattern[0] == response[i]) && (pattern[1] == response[i + 1]) &&
        // (pattern[2] == response[i + 2])
        // && (pattern[3] == response[i + 3]) && (pattern[4] == response[i + 4])
        // && (pattern[5] == response[i + 5]) && (pattern[6] == response[i + 6])) {
        // for (int j = MXOffset; j < i; j++) {
        // byteList.add(response[j]);
        // }
        // System.out.println("__________");
        // printBytes(byteList);
        // break;
        // }
        // }
    }

}