/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.internal.management.as7.tests.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.jboss.ide.eclipse.as.internal.management.as71.AS71Manager;
import org.jboss.ide.eclipse.as.management.core.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;
import org.osgi.framework.Bundle;

/**
 * @author André Dietisheim
 */
public class AS7ManagerTestUtils {

	public static final String GWT_HELLOWORLD_WAR = "gwt-helloworld.war";
	public static final String MINIMALISTIC_WAR = "minimalistic.war";

	public static final String LOCALHOST = "localhost";
	public static final int WEB_PORT = 8080;

	private static final String WAR_FOLDER = "/wars/";
	private static final String BUNDLE_ID = "org.jboss.ide.eclipse.as.management.as7.tests";

	private static final int RESPONSE_TIMEOUT = 10 * 1024;
	private static final long WAIT_TIMEOUT = 10 * 1024;

	private AS7ManagerTestUtils() {
		// inhibit instantiation
	}

	public static File getWarFile(String name) throws URISyntaxException, IOException {
		Bundle bundle = Platform.getBundle(BUNDLE_ID);
		URL entryUrl = bundle.getEntry(WAR_FOLDER + name);
		return new File(FileLocator.resolve(entryUrl).toURI());
	}

	public static String waitForRespose(String name, String host, int port) throws IOException {
		HttpURLConnection response1 = waitForResponseCode(200, name, host, port);
		response1.disconnect();
		String result = getResponse(name, host, port);
		return result;
	}

	public static void quietlyUndeploy(File file, AS71Manager manager) {
		quietlyUndeploy(file.getName(), manager);
	}

	public static void quietlyUndeploy(String name, AS71Manager manager) {
		try {
			// DetypedDeployer.undeploy(name, AS7ManagerTestUtils.LOCALHOST,
			// AS7ManagerTestUtils.MGMT_PORT);
			waitUntilFinished(manager.undeploy(name));
		} catch (Exception e) {
			// ignore
		}
	}

	public static void quietlyRemove(String name, AS71Manager manager) {
		try {
			// DetypedDeployer.remove(name, AS7ManagerTestUtils.LOCALHOST,
			// AS7ManagerTestUtils.MGMT_PORT);
			waitUntilFinished(manager.remove(name));
		} catch (Exception e) {
			// ignore
		}
	}

	public static void waitUntilFinished(IJBoss7DeploymentResult result) throws JBoss7ManangerException {
		result.getStatus(); // wait for operation to finish
	}

	public static String getResponse(String name, String host, int port) throws IOException {
		URL url = new URL("http://" + host + ":" + port + "/" + name);
		HttpURLConnection connection = connect(url);
		String s = toString(new BufferedInputStream(connection.getInputStream()));
		connection.disconnect();
		return s;
	}

	public static HttpURLConnection waitForResponseCode(int code, String name, String host, int port)
			throws IOException {
		URL url = new URL("http://" + host + ":" + port + "/" + name);
		long until = System.currentTimeMillis() + WAIT_TIMEOUT;
		int resetCount = 0;
		while (System.currentTimeMillis() < until) {
			HttpURLConnection connection = connect(url);
			try {
				if (connection.getResponseCode() == code) {
					return connection;
				}
			} catch (FileNotFoundException e) {
				if (code == 404) {
					return connection;
				}
				throw e;
			} catch( SocketException se ) {
				resetCount++;
				if( resetCount >= 10 )
					throw se;
			} finally {
				connection.disconnect();
			}
		}
		throw new RuntimeException("wait on url " + url + " for response code " + code + " timed out.");
	}

	private static HttpURLConnection connect(URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setAllowUserInteraction(false);
		connection.setConnectTimeout(RESPONSE_TIMEOUT);
		connection.setInstanceFollowRedirects(true);
		connection.setDoOutput(false);
		return connection;
	}

	public static String toString(InputStream in) throws IOException {
		StringWriter writer = new StringWriter();
		for (int data = -1; ((data = in.read()) != -1);) {
			writer.write(data);
		}
		return writer.toString();
	}

	public static boolean isListening(String host, int port) throws UnknownHostException, IOException {
		Socket socket = null;
		try {
			socket = new Socket(host, port);
			return socket.isConnected();
		} catch (ConnectException e) {
			return false;
		} finally {
			if (socket != null) {
				socket.close();
			}
		}
	}
}