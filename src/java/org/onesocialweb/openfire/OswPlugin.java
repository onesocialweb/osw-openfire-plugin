/*
 *  Copyright 2010 Vodafone Group Services Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *    
 */
package org.onesocialweb.openfire;

import java.io.File;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.database.DefaultConnectionProvider;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.Log;
import org.onesocialweb.openfire.handler.MessageEventInterceptor;
import org.onesocialweb.openfire.handler.activity.IQSubscribeInterceptor;
import org.onesocialweb.openfire.handler.activity.PEPActivityHandler;
import org.onesocialweb.openfire.handler.commenting.PEPRepliesHandler;
import org.onesocialweb.openfire.handler.inbox.PEPInboxHandler;
import org.onesocialweb.openfire.handler.pep.IQPEPHandler;
import org.onesocialweb.openfire.handler.profile.IQProfilePublishHandler;
import org.onesocialweb.openfire.handler.profile.IQProfileQueryHandler;
import org.onesocialweb.openfire.handler.relation.IQRelationQueryHandler;
import org.onesocialweb.openfire.handler.relation.IQRelationSetupHandler;
import org.onesocialweb.openfire.handler.relation.IQRelationUpdateHandler;

public class OswPlugin implements Plugin {

	private static EntityManagerFactory emFactory;

	private static File pluginDirectory;

	private Map<String, String> connProperties = new Hashtable<String, String>();

	private IQProfileQueryHandler iqProfileQueryHandler;
	private IQProfilePublishHandler iqProfileUpdateHandler;
	private IQRelationSetupHandler iqRelationSetupHandler;
	private IQRelationUpdateHandler iqRelationUpdateHandler;
	private IQRelationQueryHandler iqRelationQueryHandler;
	private IQPEPHandler iqPEPHandler;
	private MessageEventInterceptor messageInterceptor;
	private IQSubscribeInterceptor iqSubscribeInterceptor;

	public static EntityManagerFactory getEmFactory() {
		return emFactory;
	}

	public static File getPublicDirectory() {
		return pluginDirectory;
	}

	@Override
	public void initializePlugin(PluginManager manager, File directory) {
		// Save the plugin directory for later use
		pluginDirectory = directory;

		// set the connection properties
		setConnectionProperties();

		// prepare the required folders
		prepareFolders();

		// Prepare the entity manager factory
		emFactory = Persistence.createEntityManagerFactory("onesocialweb", connProperties);

		// Create the IQ handlers
		iqProfileQueryHandler = new IQProfileQueryHandler();
		iqProfileUpdateHandler = new IQProfilePublishHandler();
		iqRelationSetupHandler = new IQRelationSetupHandler();
		iqRelationQueryHandler = new IQRelationQueryHandler();
		iqRelationUpdateHandler = new IQRelationUpdateHandler();

		// Create the PEP handlers
		iqPEPHandler = new IQPEPHandler();

		// Create the message interceptors
		messageInterceptor = new MessageEventInterceptor();
		iqSubscribeInterceptor = new IQSubscribeInterceptor();
		

		// Add the IQ handlers to the router. This will trigger their
		// initialize method.
		IQRouter iqRouter = XMPPServer.getInstance().getIQRouter();
		iqRouter.addHandler(iqProfileQueryHandler);
		iqRouter.addHandler(iqProfileUpdateHandler);
		iqRouter.addHandler(iqRelationSetupHandler);
		iqRouter.addHandler(iqRelationUpdateHandler);
		iqRouter.addHandler(iqRelationQueryHandler);
		iqRouter.addHandler(iqPEPHandler);

		// Add the interceptor to process incoming notification messages
		InterceptorManager.getInstance().addInterceptor(messageInterceptor);
		InterceptorManager.getInstance().addInterceptor(iqSubscribeInterceptor);

		// Add the PEP handlers, this will trigger their initialize method
		iqPEPHandler.addHandler(new PEPActivityHandler());
		iqPEPHandler.addHandler(new PEPInboxHandler());
		iqPEPHandler.addHandler(new PEPRepliesHandler());

		Log.info("OneSocialWeb plugin has been loaded");
	}

	@Override
	public void destroyPlugin() {
		InterceptorManager.getInstance().removeInterceptor(messageInterceptor);

		IQRouter iqRouter = XMPPServer.getInstance().getIQRouter();
		iqRouter.removeHandler(iqPEPHandler);
		iqRouter.removeHandler(iqProfileQueryHandler);
		iqRouter.removeHandler(iqProfileUpdateHandler);
		iqRouter.removeHandler(iqRelationQueryHandler);
		iqRouter.removeHandler(iqRelationSetupHandler);
		iqRouter.removeHandler(iqRelationUpdateHandler);

		if (emFactory != null) {
			emFactory.close();
		}

		Log.info("OneSocialWeb plugin has been destroyed.");
	}

	/**
	 * Tries to identify the type of DB used in the Openfire server and to
	 * obtain the connection parameters. This parameters can later be passed
	 * when creating the entity manager factory to avoid reading this from the
	 * persistance.xml file.
	 * 
	 */

	private void setConnectionProperties() {
		String driver = "";
		String serverURL = "";
		String username = "";
		String password = "";

		String connectionProvider = JiveGlobals.getXMLProperty("connectionProvider.className");

		// Check if the Database in use is an external DB - default
		// configuration
		if (connectionProvider.trim().equalsIgnoreCase(DefaultConnectionProvider.class.getName())) {
			driver = JiveGlobals.getXMLProperty("database.defaultProvider.driver");
			serverURL = JiveGlobals.getXMLProperty("database.defaultProvider.serverURL");
			username = JiveGlobals.getXMLProperty("database.defaultProvider.username");
			password = JiveGlobals.getXMLProperty("database.defaultProvider.password");
		}
		// if not, it must be an embedded DB ...
		else {
			try {
				DatabaseMetaData metadata = DbConnectionManager.getConnection().getMetaData();

				driver = "org.hsqldb.jdbcDriver";
				serverURL = metadata.getURL();
				username = metadata.getUserName();
				password = "";
			} catch (SQLException e) {
				Log.error("Cannot recognise the Database.." + e.getMessage());
			}
		}
		// save the properties...
		connProperties.put("openjpa.ConnectionURL", serverURL);
		connProperties.put("openjpa.ConnectionDriverName", driver);
		connProperties.put("openjpa.ConnectionUserName", username);
		connProperties.put("openjpa.ConnectionPassword", password);

		connProperties.put("openjpa.Log", "DefaultLevel=INFO, Tool=INFO");
		connProperties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
		connProperties.put("openjpa.Multithreaded", "true");
	}

	private void prepareFolders() {
		prepareTempFolder();
		prepareUploadFolder();
	}
	
	private void prepareUploadFolder() {
		final String uploadPath = JiveGlobals.getProperty("onesocialweb.path.upload");
		if (uploadPath != null) {
			File uploadFolder = new File(uploadPath);
			if (uploadFolder.exists() && uploadFolder.canWrite()) {
				return;
			}
			JiveGlobals.deleteProperty("onesocialweb.path.upload");
			Log.error("Specified upload folder does not exist or read-only (" + uploadPath + ")");
		}

		File uploadFolder = new File(pluginDirectory, "upload");
		if (!uploadFolder.exists()) {
			if (uploadFolder.mkdirs() && uploadFolder.canWrite()) {
				Log.info("Created the upload folder at " + uploadFolder.getAbsolutePath());
				JiveGlobals.setProperty("onesocialweb.path.upload", uploadFolder.getAbsolutePath());
			}
		}
	}

	private void prepareTempFolder() {
		final String tempPath = JiveGlobals.getProperty("onesocialweb.path.temp");
		if (tempPath != null) {
			File tempFolder = new File(tempPath);
			if (tempFolder.exists() && tempFolder.canWrite()) {
				return;
			}

			JiveGlobals.deleteProperty("onesocialweb.path.temp");
			Log.error("Specified temp folder does not exist or is readonly (" + tempPath + ")");
		}

		File tempFolder = new File(pluginDirectory, "temp");
		if (!tempFolder.exists()) {
			if (tempFolder.mkdirs() && tempFolder.canWrite()) {
				Log.info("Created the temp folder at " + tempFolder.getAbsolutePath());
				JiveGlobals.setProperty("onesocialweb.path.temp", tempFolder.getAbsolutePath());
			}
		}
	}
}
