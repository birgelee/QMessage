package jqmessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JQMessage {

    /**
     * @param args the command line arguments
     */
    private static boolean serverMode = false;
    private static int port = 8080;
    private static String hostName = "";

    private static Socket socket = null;

    public static void main(String[] args) {
        if (args.length < 1) {
            serverMode = true;
        } else if (args.length == 1) {
            if (!args[0].contains(":")) {
                serverMode = true;
                port = Integer.parseInt(args[0]);
            } else {
                hostName = args[0].split(":")[0];
                port = Integer.parseInt(args[0].split(":")[1]);
            }
        } else {
            serverMode = false;
            hostName = args[0];
            port = Integer.parseInt(args[1]);
        }

        try {
            if (serverMode) {
                ServerSocket serverSocket = new ServerSocket(port);

                System.out.println("Waiting for client ...");
                socket = serverSocket.accept();

                System.out.println("Connection made");
            } else {
                System.out.println("Waiting for server ...");
                while (true) {
                    try {
                        socket = new Socket(hostName, port);
                        System.out.println("Connection made");
                        break;
                    } catch (IOException ex) {
                    }
                }
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    printMessage();
                }
            }).start();
            Scanner usrIn = new Scanner(System.in);
            while (true) {
                putString(usrIn.nextLine());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void printMessage() {
        try {
            while (true) {
                System.out.println("[in]" + readOffString(socket.getInputStream()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String readOffString(InputStream inputStream) throws IOException {
        int length = readOffInt(inputStream);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append((char) decrypt((byte) inputStream.read()));
        }
        return result.toString();
    }

    static int readOffInt(InputStream inputStream) throws IOException {
        byte[] br = new byte[4];
        br[0] = (byte) inputStream.read();//reasde size into a byte array
        br[1] = (byte) inputStream.read();
        br[2] = (byte) inputStream.read();
        br[3] = (byte) inputStream.read();
        return getInt(br);
    }

    static int getInt(byte[] rno) {
        int i = (rno[0] << 24) & 0xff000000
                | (rno[1] << 16) & 0x00ff0000
                | (rno[2] << 8) & 0x0000ff00
                | (rno[3] << 0) & 0x000000ff;
        return i;
    }

    private static void putString(String s) throws IOException {
        socket.getOutputStream().write(ByteBuffer.allocate(4).putInt(s.length()).array());
        socket.getOutputStream().write(encrypt(s.getBytes()));
    }
    
    
    private static final byte[] encryptionKey = "fs489gfdsj%#_@+$-R(%@###$Gvhijksd$%)@@".getBytes();
    private static int encryptionIndex = 0;
    private static int decryptionIndex = 0;
    
    public static byte encrypt(byte b) {
        encryptionIndex = (encryptionIndex + 1) % encryptionKey.length;
        return (byte) (b ^ encryptionKey[encryptionIndex]);
    }

    public static byte[] encrypt(byte[] barr) {
        byte[] result = new byte[barr.length];
        for (int i = 0; i < barr.length; i++) {
            result[i] = encrypt(barr[i]);
        }
        return result;
    }
    
    public static byte decrypt(byte b) {
        decryptionIndex = (decryptionIndex + 1) % encryptionKey.length;
        return (byte) (b ^ encryptionKey[decryptionIndex]);
    }

    public static byte[] decrypt(byte[] barr) {
        byte[] result = new byte[barr.length];
        for (int i = 0; i < barr.length; i++) {
            result[i] = encrypt(barr[i]);
        }
        return result;
    }

}
