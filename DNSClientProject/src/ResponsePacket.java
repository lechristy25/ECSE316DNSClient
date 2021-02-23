import java.security.KeyStore.Entry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ResponsePacket {
    private byte[] response;
    private String queryFlag;

    private boolean QR, AA, TC, RD, RA, firstURL, lastURL;
    private int RCODE, QDCOUNT, ANCOUNT, NSCOUNT, ARCOUNT;
    private int OFFSET, RTYPE, RCLASS, TTL, RDLENGTH;

    private int MXOffset, NSOffset, CNOffset;

    private String Name = "";
    private String IPAddress = "";
    private String baseURL = "";

    HashMap<Integer, String> urlMAP = new HashMap<Integer, String>();

    // Get packet and add
    public ResponsePacket(byte[] response, int size, String queryFlag, String baseURL) {
        this.response = response;
        this.queryFlag = queryFlag;
        this.baseURL = baseURL;

        getHeader();
        // Check header
        if (QR) {
            if (RCODE == 0) {
                System.out.println("***Answer Section (" + ANCOUNT + " records)***");
                getResponse();
            } else {

            }
        } else {
            System.out.println("ERROR\tThis is not a response.");
        }
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
            i += 1;
        }
        System.out.println(s);
        return s;
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
                // System.out.println("RCODE 0: No error");
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
                System.out.println("IP\t" + IPAddress + "\t" + TTL + "\t" + auth(AA));
                break;
            case 2:
                MXOffset = index + 17;
                getFlaggedResponse();
                break;
            case 5:

                break;
            case 15:
                MXOffset = index + 17;
                getFlaggedResponse();
                break;

            default:
                break;
        }

    }

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

    private void getFlaggedResponse() {
        int alias = 0;
        String url = "";
        int temp = MXOffset;
        int count = 1;

        int[] pattern = { 0, RTYPE, 0, 1 };
        ArrayList<Byte> byteList = new ArrayList<Byte>();

        if (RTYPE == 15)
            temp += 1;

        firstURL = true;
        if (ANCOUNT == 1) {
            lastURL = false;
        }
        while (count < ANCOUNT) {
            for (int i = temp; i < response.length - 4; i++) {
                if ((pattern[0] == response[i]) && (pattern[1] == response[i + 1]) && (pattern[2] == response[i + 2])
                        && (pattern[3] == response[i + 3])) {
                    for (int j = temp; j < i; j++) {
                        byteList.add(response[j]);
                    }

                    urlMaker(byteList, temp);
                    temp = i + 11;
                    if (RTYPE == 15)
                        temp += 1;
                    break;
                }
            }
            byteList.clear();
            count++;
            firstURL = false;
        }

        int[] bpattern = { 0, 0, 0, 0 };
        for (int i = temp; i < response.length; i++) {
            if ((bpattern[0] == response[i]) && (bpattern[1] == response[i + 1]) && (bpattern[2] == response[i + 2])
                    && (bpattern[3] == response[i + 3])) {
                break;
            }
            byteList.add(response[i]);
        }

        if (ANCOUNT != 1)
            lastURL = true;

        urlMaker(byteList, temp);

    }

    private void printBytes(ArrayList<Byte> byteList) {
        for (Byte b : byteList) {
            System.out.println(b);
        }
    }

    private void urlMaker(ArrayList<Byte> byteList, int startIndex) {
        String url = "";
        ArrayList<String> urlARRAY = new ArrayList<String>();

        String otherBase = "";
        boolean firstSixFour = false;

        int indexComplete = 0;
        // int startIndex = MXOffset;

        for (int i = 0; i < byteList.size(); i++) {
            Byte b = byteList.get(i);
            // System.out.println("h" + b);

            // String c = "" + ((char) (b & 0xFF));
            if ((b >= 33) && (b <= 126)) {
                String c = Character.toString(((char) (b & 0xFF)));
                url += c;
            } else {
                if (i == 0)
                    continue;
                urlMAP.put(startIndex, url);
                urlARRAY.add(url);
                url = "";
                startIndex += i;
                
                if (byteList.get(i) == -64)
                break;
                
            }
            if (i == byteList.size() - 1) {
                urlMAP.put(startIndex, url);
                urlARRAY.add(url);
                url = "";

            }
        }
        if (firstURL) {
            for (int i = 1; i < urlARRAY.size(); i++) {
                // System.out.println("hello");
                if (i == urlARRAY.size()) {
                    baseURL += urlARRAY.get(i);
                } else {
                    baseURL += urlARRAY.get(i) + ".";
                }
            }
        }
        firstURL = false;

        // Getting pointers
        boolean addedBaseURL = false;
        for (int i = 0; i < byteList.size(); i++) {
            if (byteList.get(i) == -64) {
                if (byteList.get(i + 1) == 12) {
                    if (!addedBaseURL) {
                        urlARRAY.add(baseURL);
                        addedBaseURL = true;
                    }
                } else {
                    urlARRAY.add(urlMAP.get((int) byteList.get(i + 1)));
                }
            }
        }

        String finalURL = "";

        for (int i = 0; i < urlARRAY.size(); i++) {
            if (i == urlARRAY.size() - 1) {
                finalURL += urlARRAY.get(i);
            } else {
                finalURL += urlARRAY.get(i) + ".";
            }
        }

        if (RTYPE == 15) {
            if (lastURL) {
                finalURL += "." + baseURL;
            }
        }
        switch (RTYPE) {
            case 2:
                System.out.println("NS\t" + finalURL + "\t" + TTL + "\t" + auth(AA));
                break;
            case 15:
                System.out.println("MX\t" + finalURL + "\t" + TTL + "\t" + auth(AA));
                break;
            default:
                break;
        }

    }

    private void completeURL(ArrayList<Byte> byteList, int i, ArrayList<String> urlARRAY) {
        for (int j = i + 1; j < byteList.size(); j++) {
            int xxx = byteList.get(j);
            System.out.println(xxx);
        }
    }

    private void urlByteExtract(int offset) {
    }

}
