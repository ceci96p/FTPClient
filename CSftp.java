import javax.imageio.IIOException;
import java.io.*;
import java.lang.System;
import java.net.*;
import java.nio.file.FileAlreadyExistsException;
import java.io.File;
//
// This is an implementation of a simplified version of a command
// line ftp client. The program always takes two arguments
//


public class CSftp {
	static final int MAX_LEN = 255;
	public static String IP;
	public static Integer PORT;
	public static String IP2;
	public static Integer PORT2;
	public static String IP3;
	public static Integer PORT3;
	static final Integer timeout1 = 20000;
	static final Integer timeout2 = 10000;
	private static boolean run = true;
	private static Socket s = new Socket();
	private static  boolean repeat = false;



	public static void main(String[] args) { // main
		byte cmdString[] = new byte[MAX_LEN];
		start(args);
		connect(IP, PORT);
	}

	static private void start(String[] args) {                   //check initial amount of given values should be 1 or 2
		if (args.length == 1) {
			IP = args[0];
			PORT = 21;
		} else if (args.length == 2) {
			IP = args[0];
			PORT = Integer.parseInt(args[1]);
		} else if (args.length >= 3 || args.length == 0) {
			System.out.println("0x002 Incorrect number of arguments.");
			System.exit(1);
			return;
		}
	}

	static private void connect(String IP, Integer PORT) {
		try {
			s.connect(new InetSocketAddress(IP, PORT), timeout1);                                      //make connection
			PrintWriter out = new PrintWriter(s.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			String fromServer = null;
			while (run) {                                                // principal loop that keeps conversation going
				if (repeat){                                 //if there is a failure it happens at the client input side
					execute(out, in, stdIn);
				}
				else {
					while ((fromServer = in.readLine()) != null) { //if there is a failure it happens at the server side
						System.out.println("<-- " + fromServer);
						sleep(in);
						if(!in.ready()){
							break;
						}
					}
					execute(out, in, stdIn);
				}
			}
			s.close();

		} catch (ConnectException exp) {
			System.err.println("0xFFFC Control connection to " + IP + " on port " + PORT + " failed to open.");
			try {
				s.close();
			} catch (IOException e) {
			}System.exit(1);
		} catch (SocketTimeoutException exp) {
			System.err.println("0xFFFC Control connection to " + IP + " on port " + PORT + " failed to open.");
			try {
				s.close();
			} catch (IOException e) {
			}System.exit(1);
		} catch (SocketException exp) {
			System.err.println("0xFFFC Control connection to " + IP + " on port " + PORT + " failed to open.");
			try {
				s.close();
			} catch (IOException e) {
			}System.exit(1);
		} catch (IIOException exp) {
			System.err.println("0xFFFD Control connection I/O error, closing control connection.");
			try {
				s.close();
			} catch (IOException e) {
			}System.exit(1);
		} catch (IOException exp) {
			System.err.println("998 Input error while reading commands, terminating.");
			try {
				s.close();
			} catch (IOException e) {
			}System.exit(1);
		}

	}

	private static void sleep(BufferedReader in) throws IOException { //function used to help print all lines from server and exit WHILE lopp
		long time = System.currentTimeMillis();
		while(System.currentTimeMillis()-time < 200){
			if(in.ready()){
				break;
			}
		}
	}

	static private void execute(PrintWriter out, BufferedReader in, BufferedReader stdIn) {
	    String fromServer;
		try {
			System.out.print("csftp> ");

			String fromUser = stdIn.readLine();
			if (fromUser.startsWith("#") || fromUser.startsWith(" ")){
				repeat = true;
				return;
			}
			if (fromUser != null) {
				String[] splited = fromUser.split("\\s+");
				System.out.println("--> " + fromUser);
				switch (splited[0]) {                                                            //switch on user inputs
					case "user":                                                                           // user login
						if (splited.length == 2) {
							out.print( "USER" + " " + splited[1] + "\r\n");
							out.flush();
							repeat = false;
						} else if (splited.length != 2) {
							System.out.println("0x002 Incorrect number of arguments.");
							repeat = true;
						}
						break;

					case "pw":                                                                          //password login
						if (splited.length == 2) {
							out.print( "PASS" + " " + splited[1] + "\r\n");
							out.flush();
							repeat = false;
						} else if (splited.length != 2) {
							System.out.println("0x002 Incorrect number of arguments.");
							repeat = true;
						}
						break;

					case "quit":                                                                       //quit connection
						if (splited.length == 1) {
							out.print("QUIT" + "\r\n");
							out.flush();
							fromServer = in.readLine();
							System.out.println("<-- " + fromServer);
							run = false;

						} else if (splited.length != 1) {
							System.out.println("0x002 Incorrect number of arguments.");
							repeat = true;
						}
						break;

					case "get":                                                       // retrieve file indicated by user
						if (splited.length == 2) {
							Socket s3 = new Socket();
							out.print("PASV" + "\r\n");
							out.flush();
							fromServer = in.readLine();
							System.out.println("<-- " + fromServer);
							get(s3,out, fromServer, splited[1],in);
							//repeat = false;
						} else if (splited.length != 2) {
							System.out.println("0x002 Incorrect number of arguments.");
							repeat = true;
						}

						break;
					case "features":                      // requests the set of features/extensions the server supports
						if (splited.length == 1) {
							out.print("FEAT" + "\r\n");
                            out.flush();
							features(in);
							repeat = true;
						} else if (splited.length != 1) {
							System.out.println("0x002 Incorrect number of arguments.");
							repeat = true;
						}

						break;
					case "cd":                                    // changes the current working directory on the server
						if (splited.length == 2) {
							out.print("CWD" + " " + splited[1] + "\r\n");
                            out.flush();
							repeat = false;
						} else if (splited.length != 2) {
							System.out.println("0x002 Incorrect number of arguments.");
							repeat = true;
						}

						break;
					case "dir":              // retrieves a list of files in the current working directory on the server
						if (splited.length == 1 ) {
							Socket s2 = new Socket();
							out.print("PASV" + "\r\n");                                          // go into passive mode
                            out.flush();
							fromServer = in.readLine();
							System.out.println("<-- " + fromServer);
							dir(s2,out, in, fromServer);
							repeat = false;
						} else if (splited.length != 1) {
							System.out.println("0x002 Incorrect number of arguments.");
							repeat = true;
						}

						break;
					default:
						System.out.println("0x001 Invalid command.");
						repeat = true;
						break;
				}
				return;
			}
		} catch (ConnectException exp) {
			System.err.println("0xFFFC Control connection to " + IP + " on port " + PORT + " failed to open.");
			try {
				s.close();
			} catch (IOException e) {
			}System.exit(1);
		} catch (SocketTimeoutException exp) {
			System.err.println("0xFFFC Control connection to " + IP + " on port " + PORT + " failed to open.");
			try {
				s.close();
			} catch (IOException e) {
			}System.exit(1);
		} catch (SocketException exp) {
			System.err.println("0xFFFC Control connection to " + IP + " on port " + PORT + " failed to open.");
			try {
				s.close();
			} catch (IOException e) {
			}System.exit(1);
		} catch (IIOException exp) {
			System.err.println("0xFFFD Control connection I/O error, closing control connection.");
			try {
				s.close();
			} catch (IOException e) {
			}System.exit(1);
		} catch (IOException exp) {
			System.err.println("998 Input error while reading commands, terminating.");
			try {
				s.close();
			} catch (IOException e) {
			}System.exit(1);
		}
	}

	static private void features(BufferedReader in){
		try { String fromServer;
			while ((fromServer = in.readLine()) != null) {       //print list of features until servers stops outputting
				System.out.println("<-- " + fromServer);
				sleep(in);
				if(!in.ready()){
					break;
				}
			}
			return;
		}catch (IOException exp){
			System.err.println("error");
		}
	}

	static private void dir(Socket s2,PrintWriter out,BufferedReader in, String fromServer) {
		if (fromServer.substring(0, 3).equals("227")){                           //calculation from value obtain by PASS
			int sIndex = fromServer.indexOf("(");
			int eIndex = fromServer.lastIndexOf(")");
			String number = fromServer.substring(sIndex + 1, eIndex);
			String[] sequence = number.split(",");
			IP2 = sequence[0] + "." + sequence[1] + "." + sequence[2] + "." + sequence[3];                     //a.b.c.d
			PORT2 = ((Integer.parseInt(sequence[4]) * 256) + Integer.parseInt(sequence[5]));             //(e * 256) + f
		}

		try {
			s2.connect(new InetSocketAddress(IP2, PORT2), timeout2);                         //establish data connection
			BufferedReader in2 = new BufferedReader(new InputStreamReader(s2.getInputStream()));
			String fromServer2;

			out.print("LIST" + "\r\n");                                                           //get directory's list
            out.flush();

			fromServer = in.readLine();
			System.out.println("<-- " + fromServer);

			while ((fromServer2 = in2.readLine()) != null) {
				System.out.println("<-- " + fromServer2);
				if(fromServer.contains("226")){  // if server yields :226 Closing data connection then quit connection and throw message error
					System.err.println("0x3A7 Data transfer connection I/O error, closing data connection.");
					try {
						s2.close();
					} catch (IOException e) {
					}System.exit(1);
				}
			}
			s2.close();

		} catch (ConnectException exp) {
			System.err.println("0x3A2  Data transfer connection to " + IP2 + " on port " + PORT2 + " failed to open.");
			try {
				s2.close();
			} catch (IOException e) {
			}System.exit(1);
		} catch (SocketTimeoutException exp) {
			System.err.println("0x3A2  Data transfer connection to " + IP2 + " on port " + PORT2 + " failed to open.");
			try {
				s2.close();
			} catch (IOException e) {
			}System.exit(1);
		} catch (SocketException exp) {
			System.err.println("0x3A2  Data transfer connection to " + IP2 + " on port " + PORT2 + " failed to open.");
			try {
				s2.close();
			} catch (IOException e) {
			}System.exit(1);
		} catch (IIOException exp) {
			System.err.println("0x3A7 Data transfer connection I/O error, closing data connection.");
			try {
				s2.close();
			} catch (IOException e) {
			}System.exit(1);
		} catch (IOException exp) {
			System.err.println("998 Input error while reading commands, terminating.");
			try {
				s2.close();
			} catch (IOException e) {
			}System.exit(1);
		}
	}

	static private void get(Socket s3,PrintWriter out, String fromServer, String remote, BufferedReader in)throws FileAlreadyExistsException  {
		if (fromServer.substring(0, 3).equals("227")) {                          //calculation from value obtain by PASS
			int sIndex = fromServer.indexOf("(");
			int eIndex = fromServer.lastIndexOf(")");
			String number = fromServer.substring(sIndex + 1, eIndex);
			String[] sequence = number.split(",");
			IP3 = sequence[0] + "." + sequence[1] + "." + sequence[2] + "." + sequence[3];                     //a.b.c.d
			PORT3 = ((Integer.parseInt(sequence[4]) * 256) + Integer.parseInt(sequence[5]));             //(e * 256) + f

			try {
				s3.connect(new InetSocketAddress(IP3, PORT3), timeout2);                     //establish data connection

				out.print("TYPE I" + "\r\n"); 						                                  // set: binaryMode
                out.flush();
				fromServer = in.readLine();
				System.out.println("<--- " + fromServer);

                out.print("SIZE" + " "+ remote + "\r\n");                            // size: GET size of FIle  in bytes
                out.flush();
                fromServer = in.readLine();
                System.out.println("<--- " + fromServer);
                String[] bytesSize = fromServer.split(("\\s+"));


				out.print("RETR" + " "+ remote + "\r\n");  			                      // remote: is FILE TO RETRIEVE
                out.flush();
				fromServer = in.readLine();
				System.out.println("<--- " + fromServer);
				if (fromServer.contains("150") || fromServer.contains("125" )) {  // opening binary mode data connection
					try {
						InputStream initialStream = s3.getInputStream();
						byte[] buffer = new byte[Integer.parseInt(bytesSize[1])];                // buffer bytes to save
						initialStream.read(buffer);

							String fileName;
							if (remote.contains("/")) {             // check if variable remote is a name file or a path
								String[] realName = remote.split("\\/");
								fileName = realName[realName.length - 1];
							} else {

                            fileName = remote;
							}
							if (new File(fileName).exists()){//if file already exists on local directory then throw error
								s3.close();
								repeat = false;
                                out.print("ABOR" + "\r\n");
                                out.flush();
                                throw new FileAlreadyExistsException("0x38E Access to local file " + remote + " denied.");
							}

							FileOutputStream outStream = new FileOutputStream(fileName);
							int length;
							while ((length = initialStream.read(buffer)) > 0) {                   //write bits into file
								outStream.write(buffer, 0, length);
							}
							s3.close();
							repeat = false;
							return;
						} catch(FileAlreadyExistsException e){
							System.err.println("0x38E Access to local file " + remote + " denied.");
							s3.close();
							repeat = false;
							return;
						}
					}
				else {
					s3.close();
					repeat = true;
					return;
				}

			} catch (ConnectException exp) {
				System.err.println("0x3A2  Data transfer connection to " + IP3 + " on port " + PORT3 + " failed to open.");
				try {
					s3.close();
				} catch (IOException e) {
				}System.exit(1);
			} catch (SocketTimeoutException exp) {
				System.err.println("0x3A2  Data transfer connection to " + IP3 + " on port " + PORT3 + " failed to open.");
				try {
					s3.close();
				} catch (IOException e) {
				}System.exit(1);
			} catch (SocketException exp) {
				System.err.println("0x3A2  Data transfer connection to " + IP3 + " on port " + PORT3 + " failed to open.");
				try { s3.close();
				} catch (IOException e) {
				}System.exit(1);
			} catch (IIOException exp) {
				System.err.println("0x3A7 Data transfer connection I/O error, closing data connection.");
				try {
					s3.close();
				} catch (IOException e) {
				}System.exit(1);
			} catch (IOException exp) {
				System.err.println("0x3A2  Input error while reading commands, terminating.");
				try {
					s3.close();
				} catch (IOException e) {
				}System.exit(1);
			}
		}
	}
}


