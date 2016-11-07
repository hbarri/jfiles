/*
 * Copyright (C) 2016 - WSU CEG3120 Students
 * 
 * Roberto C. Sánchez <roberto.sanchez@wright.edu>
 * 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package edu.wright.cs.jfiles.server;

import edu.wright.cs.jfiles.core.CommandExecutor;
import edu.wright.cs.jfiles.core.CommandLine;
import edu.wright.cs.jfiles.core.CommandParser;
import edu.wright.cs.jfiles.core.Environment;
import edu.wright.cs.jfiles.core.ExecutablePath;
import edu.wright.cs.jfiles.exception.CommandNotFoundException;
import edu.wright.cs.jfiles.exception.ExecutionResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * The main class of the JFiles server application.
 * 
 * @author Roberto C. Sánchez &lt;roberto.sanchez@wright.edu&gt;
 *
 */
public class JFilesServer implements Runnable {

	static final Logger logger = LogManager.getLogger(JFilesServer.class);
	private static final int PORT = 9786;
	//private final ServerSocket serverSocket;
	private static final String UTF_8 = "UTF-8";
	private JFilesServerThread clients[] = new JFilesServerThread[50];
	private ServerSocket server = null;
	private Thread thread = null;
	private int clientCount = 0;

	/**
	 * Handles allocating resources needed for the server.
	 * 
	 * @throws IOException
	 *             If there is a problem binding to the socket
	 */
	  JFilesServer(int port) {
		try {
			System.out.println("Binding to port " + PORT + ", please wait  ...");
			server = new ServerSocket(PORT);
			System.out.println("Server started: " + server);
			start();
		} catch (IOException ioe) {
			System.out.println("Can not bind to port " + PORT + ": " + ioe.getMessage());
		}
	}

	public void run() {
		while (true) {
			try {
				System.out.println("Waiting for a client ...");
				addThread(server.accept());
			} catch (IOException ioe) {
				System.out.println("Server accept error: " + ioe);
				stop();
			}
		}
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	public void stop() {
		if (thread != null) {
			thread.stop();
			thread = null;
		}
	}

	private int findClient(int ID) {
		for (int i = 0; i < clientCount; i++)
			if (clients[i].getID() == ID)
				return i;
		return -1;
	}

	public synchronized void handle(int ID, String input) throws IOException {

		// logger.info("Received connection from" +
		// server.getRemoteSocketAddress());
		String dir = System.getProperty("user.dir");
		File history = new File("SearchHistory.txt");

		Locale.setDefault(new Locale("English"));
		FileWriter hstWrt = new FileWriter(history); // history writer
		String cmd;

		ExecutablePath executablePath = new ExecutablePath();
		Environment environment = new Environment();

		CommandParser parser = new CommandParser(environment);
		CommandExecutor executor = new CommandExecutor(executablePath, environment);
		
		// out.write("Prompt :> ");
		// command example
		// ========================================================================
		// out.write("Prompt :> ");
		// CommandLine commandLine = parser.parse(cmd);
		//
		// try {
		// ExecutionResult result = executor.executeCommand(commandLine,
		// out);
		// if (result.isExitShell()) {
		// break;
		// }
		// } catch (CommandNotFoundException e) {
		// out.write(" " + e.getMessage() + ": command not found\n");
		// }
		// ========================================================================

		String[] baseCommand = input.split(" ");

		switch (baseCommand[0].toUpperCase(Locale.ENGLISH)) {
		case "LIST":

			listCmd(dir, ID);
			break;
		case "FIND":
			if (isValid(baseCommand)) {
				findCmd(dir, ID, baseCommand[1].toLowerCase(Locale.ENGLISH));
			} else {

				clients[findClient(ID)].send("Invaild Command\n");

			}

			break;
		case "FINDR":
			if (isValid(baseCommand)) {
				recursiveFindCmd(dir, ID, baseCommand[1].toLowerCase(Locale.ENGLISH));
			} else {
				clients[findClient(ID)].send("Invaild Command\n");
			}

			break;
		case "FILE":
			break;
		case "EXIT":
			clients[findClient(ID)].send(".exit");
			remove(ID);
			break;
		default:
			logger.info("Hit default switch." + System.lineSeparator());
			break;
		}
		// if (history.exists()) {
		// hstWrt.append(baseCommand + "\n");
		// } else {
		// hstWrt.write(baseCommand + "\n");
		// }

		// out.flush();
		clients[findClient(ID)].send(">");
		hstWrt.close();

	}

	public synchronized void remove(int ID) {
		int pos = findClient(ID);
		if (pos >= 0) {
			JFilesServerThread toTerminate = clients[pos];
			System.out.println("Removing client thread " + ID + " at " + pos);
			if (pos < clientCount - 1)
				for (int i = pos + 1; i < clientCount; i++)
					clients[i - 1] = clients[i];
			clientCount--;
			try {
				toTerminate.close();
			} catch (IOException ioe) {
				System.out.println("Error closing thread: " + ioe);
			}
			toTerminate.stop();
		}
	}

	private void addThread(Socket socket) {
		if (clientCount < clients.length) {
			System.out.println("Client accepted: " + socket);
			clients[clientCount] = new JFilesServerThread(this, socket);
			try {
				clients[clientCount].open();
				clients[clientCount].start();
				clientCount++;
			} catch (IOException ioe) {
				System.out.println("Error opening thread: " + ioe);
			}
		} else
			System.out.println("Client refused: maximum " + clients.length + " reached.");
	}

	boolean isValid(String[] command) {
		if (command.length <= 1) { // used for handling invalid error
			logger.error("Invalid Input, nothing to find");
			return false;
		} else {
			return true;
		}
	}

	/**
	 * List Command function. Method for the list command.
	 * 
	 * @throws IOException
	 *             If there is a problem binding to the socket
	 */
	private void listCmd(String dir, int ID) {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(dir))) {
			for (Path path : directoryStream) {
				// out.write(path.toString() +
				// System.getProperty("line.separator"));
				clients[findClient(ID)]
						.send(path.toString() + System.getProperty("line.separator"));
			}
		} catch (IOException e) {
			// TODO AUto-generated catch block
			// e.printStackTrace();
			logger.error("Some error occured", e);
		}
	}

	/**
	 * Find Command function. Method for the find command. Writes results found
	 * within current directory. Search supports glob patterns
	 * 
	 * @throws IOException
	 *             If there is a problem binding to the socket
	 */
	private void findCmd(String dir, int ID, String searchTerm) {
		int findCount = 0;
		try (DirectoryStream<Path> directoryStream =
				Files.newDirectoryStream(Paths.get(dir), searchTerm)) {
			for (Path path : directoryStream) {
				if (path.toString().toLowerCase(Locale.ENGLISH).contains(searchTerm)) {
					// out.write(path.toString() + "\n");
					clients[findClient(ID)].send(path.toString() + "\n");
					findCount++;
				}
			}
			System.out.println("Found " + findCount + " file(s) in " + dir + " that contains \""
					+ searchTerm + "\"\n");
		} catch (IOException e) {
			// TODO AUto-generated catch block
			// e.printStackTrace();
			logger.error("Some error occured", e);
		}
	}

	/**
	 * Recursive find Command function. Method for the recursive option of the
	 * find command. Calls itself if a child directory is found, otherwise calls
	 * findCmd to get results from current directory.
	 * 
	 * @throws IOException
	 *             If there is a problem binding to the socket
	 */
	private void recursiveFindCmd(String dir, int ID, String searchTerm) {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(dir))) {
			for (Path path : directoryStream) {
				if (path.toFile().isDirectory()) {
					recursiveFindCmd(path.toString(), ID, searchTerm);
				}
			}
		} catch (IOException e) {
			// TODO AUto-generated catch block
			// e.printStackTrace();
			logger.error("Some error occured", e);
		}
		findCmd(dir, ID, searchTerm);
	}

	/**
	 * The main entry point to the program.
	 * 
	 * @throws IOException
	 *             If there is a problem binding to the socket
	 */
	public static void main(String args[]) {
		JFilesServer server = null;
		int porter = 5050;

		server = new JFilesServer(porter);
	}

}
