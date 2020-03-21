package javaserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class JavaWebServer {

    static final File WEB_ROOT = new File(".");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";

    //port to listen connection
    static final int PORT = 9001;

    private static Socket connect;

    public static void main(String[] args) {
        try{
            //creating a server socket that can listen on a specific port number
            ServerSocket serverConnect = new ServerSocket(PORT);
            System.out.println("Server started. Listening for connections on port : " + PORT);

            while(true){
                //accept a connection using the server socket variable from above
                //this function actually pauses the loop and waits indefinitely until a connection comes in
                connect = serverConnect.accept();
                //this function executes after we accept a connection on the server
                runThisCode();
            }

        } catch (IOException e){

        }
    }

    public static void runThisCode(){
        //this function manages our particular client connection

        //used to read the inputstream of data
        BufferedReader in = null;
        //used to print data onto the outputstream
        PrintWriter out = null;
        //a stream object meant to send data out through the connection
        BufferedOutputStream dataOut = null;
        String fileRequested = null;

        try {
            // we read characters from the client via input stream on the socket
            //this is where the input stream is made
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            //we get character output stream to client (for headers)
            out = new PrintWriter(connect.getOutputStream());
            //get binary output stream to client (for requested data)
            dataOut = new BufferedOutputStream(connect.getOutputStream());

            //get first line of the requested from the client
            String input = in.readLine();
            //print the first line of the request
            System.out.println(input);
            //we parse the request with a string tokenizer
            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase(); //acquire HTTP method of the client
            fileRequested = parse.nextToken().toLowerCase(); //acquire the file requested

            if(method.equals("GET")){
                //if no specific file was requested
                if(fileRequested.endsWith("/")){
                    fileRequested += DEFAULT_FILE;
                }

                File file = new File(WEB_ROOT, fileRequested);
                int fileLength = (int) file.length();
                String content = getContentType(fileRequested);

                if(method.equals("GET")){
                    // GET method so we send them content
                    byte[] fileData = readFileData(file, fileLength);

                    // send HTTP Headers with data to client
                    out.println("HTTP/1.1 200 OK");
                    out.println("Server : Java HTTP Server from Jason : 1.0");
                    out.println("Date:" + new Date());
                    out.println("Content-type: " + content);
                    out.println("Content-length: " + fileLength);
                    out.println(); //blank line required in HTTP protocol format
                    out.flush(); // flush character output stream buffer

                    dataOut.write(fileData, 0, fileLength);
                    dataOut.flush();
                }


                System.out.println("File " + fileRequested + " of type " + content + "returned");

            }

        } catch(FileNotFoundException fnfe){
            try{
                fileNotFound(out, dataOut, fileRequested);
            } catch(IOException ioe){
                System.err.println("Error with file not found exception : " + ioe.getMessage());
            }
        } catch (IOException ioe){
            System.err.println("Server error : " + ioe);
        } finally {
            try {
                in.close(); //close character input stream
                out.close();
                dataOut.close();
                connect.close(); //close socket connection between server and client
            } catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }

            System.out.println("Connection close.");

        }
    }

    //used for accessing html files and converting it into an array of bytes
    private static byte[] readFileData(File file, int fileLength) throws IOException{
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];
        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally{
            if (fileIn != null){
                fileIn.close();
            }
        }

        return fileData;

    }


    //if the requested file was not found then send a 404 not found response through the output stream
    private static void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException{
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        // send HTTP Headers with data to client
        out.println("HTTP/1.1 404 File Not Found");
        out.println("Server : Java HTTP Server from Jason : 1.0");
        out.println("Date:" + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println(); //blank line required in HTTP protocol format
        out.flush(); // flush character output stream buffer

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

        System.out.println("File " + fileRequested + " not found.");

    }

    //return supported MIME types
    private static String getContentType(String fileRequested){
        if(fileRequested.endsWith(".htm") || fileRequested.endsWith(".html")){
            return "text/html ";
        }else{
            return "text/plain ";
        }
    }
}
