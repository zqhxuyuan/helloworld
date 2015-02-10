/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zqh.hadoop.moya.core.yarn;

import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.api.protocolrecords.GetNewApplicationResponse;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Client for Distributed Shell application submission to YARN.
 * 
 * <p>
 * The distributed shell client allows an application master to be launched that
 * in turn would run the provided shell command on a set of containers.
 * </p>
 * 
 * <p>
 * This client is meant to act as an example on how to write yarn-based
 * applications.
 * </p>
 * 
 * <p>
 * To submit an application, a client first needs to connect to the
 * <code>ResourceManager</code> aka ApplicationsManager or ASM via the
 * {@link org.apache.hadoop.yarn.api.ApplicationClientProtocol}. The {@link org.apache.hadoop.yarn.api.ApplicationClientProtocol}
 * provides a way for the client to get access to cluster information and to
 * request for a new {@link org.apache.hadoop.yarn.api.records.ApplicationId}.
 * <p>
 *
 * <p>
 * For the actual job submission, the client first has to create an
 * {@link org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext}. The
 * {@link org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext} defines the application details such as
 * {@link org.apache.hadoop.yarn.api.records.ApplicationId} and application name, the priority assigned to the
 * application and the queue to which this application needs to be assigned. In
 * addition to this, the {@link org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext} also defines the
 * {@link org.apache.hadoop.yarn.api.records.ContainerLaunchContext} which describes the <code>Container</code>
 * with which the {@link com.zqh.hadoop.moya.core.yarn.ApplicationMaster} is launched.
 * </p>
 *
 * <p>
 * The {@link org.apache.hadoop.yarn.api.records.ContainerLaunchContext} in this scenario defines the resources to
 * be allocated for the {@link com.zqh.hadoop.moya.core.yarn.ApplicationMaster}'s container, the
 * local resources (jars, configuration files) to be made available and the
 * environment to be set for the {@link com.zqh.hadoop.moya.core.yarn.ApplicationMaster} and the
 * commands to be executed to run the {@link com.zqh.hadoop.moya.core.yarn.ApplicationMaster}.
 * <p>
 *
 * <p>
 * Using the {@link org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext}, the client submits the
 * application to the <code>ResourceManager</code> and then monitors the
 * application by requesting the <code>ResourceManager</code> for an
 * {@link org.apache.hadoop.yarn.api.records.ApplicationReport} at regular time intervals. In case of the
 * application taking too long, the client kills the application by submitting a
 * {@link org.apache.hadoop.yarn.api.protocolrecords.KillApplicationRequest} to the <code>ResourceManager</code>.
 * </p>
 *
 */
@InterfaceAudience.Public
@InterfaceStability.Unstable
public class Client {

	private static final Log LOG = LogFactory.getLog(Client.class);

	// Configuration
	private Configuration conf;
	private YarnClient yarnClient;
	// Application master specific info to register a new Application with
	// RM/ASM
	private String appName = "";
	// App master priority
	private int amPriority = 0;
	// Queue for App master
	private String amQueue = "";
	// Amt. of memory resource to request for to run the App Master
	private int amMemory = 10;

	// Application master jar file
	private String appMasterJar = "";
	// Main class to invoke application master
	private final String appMasterMainClass = "org.moya.core.yarn.ApplicationMaster";

	// Shell command to be executed
	private String localLibJar = "";
	// Location of shell script

	// Env variables to be setup for the shell command
	private Map<String, String> shellEnv = new HashMap<String, String>();
	// Shell Command Container priority
	private int moyaPriority = 0;

	// Amt of memory to request for container in which shell script will be
	// executed
	private int containerMemory = 10;
	// No. of containers in which the shell script needs to be executed
	private int numContainers = 1;

	// log4j.properties file
	// if available, add to local resources and set into classpath
	private String log4jPropFile = "";

	// Debug flag
	boolean debugFlag = false;

	// Command line options
	private Options opts;

	private String ZKHosts = null;

	/**
	 * @param args
	 *            Command line arguments
	 */
	public static void main(String[] args) {
		boolean result = false;
		try {
			Client client = new Client();
			LOG.info("Initializing Client");
			try {
				boolean doRun = client.init(args);
				if (!doRun) {
					System.exit(0);
				}
			} catch (IllegalArgumentException e) {
				System.err.println(e.getLocalizedMessage());
				client.printUsage();
				System.exit(-1);
			}
			result = client.run();
		} catch (Throwable t) {
			LOG.fatal("Error running CLient", t);
			System.exit(1);
		}
		if (result) {
			LOG.info("Application completed successfully");
			System.exit(0);
		}
		LOG.error("Application failed to complete successfully");
		System.exit(2);
	}

	/**
   */
	public Client(Configuration conf) throws Exception {

		this.conf = conf;
		yarnClient = YarnClient.createYarnClient();
		yarnClient.init(conf);
		opts = new Options();
		opts.addOption("appname", true,
				"Application Name. Default value - MoYa");
		opts.addOption("priority", true, "Application Priority. Default 0");
		opts.addOption("queue", true,
				"RM Queue in which this application is to be submitted");
		opts.addOption("master_memory", true,
				"Amount of memory in MB to be requested to run the application master");
		opts.addOption("jar", true,
				"Jar file containing the application master");
		opts.addOption("lib", true,
				"Runnable Jar with MOYA inside");
		opts.addOption("moya_priority", true,
				"Priority for the MOYA containers");
		opts.addOption("container_memory", true,
				"Amount of memory in MB to be requested to run the shell command");
		opts.addOption("num_containers", true,
				"No. of containers on which the shell command needs to be executed");
		opts.addOption("log_properties", true, "log4j.properties file");
		opts.addOption("ZK", true, "Comma seperated list of ZK hosts ie - host1:port,host2:port");
		opts.addOption("debug", false, "Dump out debug information");
		opts.addOption("help", false, "Print usage");
	}

	/**
   */
	public Client() throws Exception {
		this(new YarnConfiguration());
	}

	/**
	 * Helper function to print out usage
	 */
	private void printUsage() {
		new HelpFormatter().printHelp("Client", opts);
	}

	/**
	 * Parse command line options
	 *
	 * @param args
	 *            Parsed command line options
	 * @return Whether the init was successful to run the client
	 * @throws org.apache.commons.cli.ParseException
	 */
	public boolean init(String[] args) throws ParseException {

		CommandLine cliParser = new GnuParser().parse(opts, args);

		if (args.length == 0) {
			throw new IllegalArgumentException(
					"No args specified for client to initialize");
		}

		if (cliParser.hasOption("help")) {
			printUsage();
			return false;
		}

		if (cliParser.hasOption("debug")) {
			debugFlag = true;

		}

		appName = cliParser.getOptionValue("appname", "MoYa");
		amPriority = Integer
				.parseInt(cliParser.getOptionValue("priority", "0"));
		amQueue = cliParser.getOptionValue("queue", "default");
		amMemory = Integer.parseInt(cliParser.getOptionValue("master_memory",
				"10")); //this will default to yarn.scheduler.minimum-allocation-mb unless its lower then 10...

		//TODO
		ZKHosts = cliParser.getOptionValue("ZK", "");

		if (ZKHosts.isEmpty()) {
			throw new IllegalArgumentException(
					"Invalid ZK Hosts Passed - " + ZKHosts);
		}

		if (amMemory < 0) {
			throw new IllegalArgumentException(
					"Invalid memory specified for application master, exiting."
							+ " Specified memory=" + amMemory);
		}

		if (!cliParser.hasOption("jar")) {
			throw new IllegalArgumentException(
					"No jar file specified for application master");
		}

		appMasterJar = cliParser.getOptionValue("jar");

		if (!cliParser.hasOption("lib")) {
			throw new IllegalArgumentException(
					"The runnable MOYA jar to be started by application master");
		}
		localLibJar = cliParser.getOptionValue("lib");

		moyaPriority = Integer.parseInt(cliParser.getOptionValue(
				"moya_priority", "0"));

		containerMemory = Integer.parseInt(cliParser.getOptionValue(
				"container_memory", "128"));
		numContainers = Integer.parseInt(cliParser.getOptionValue(
				"num_containers", "1"));

		if (containerMemory < 0 || numContainers < 1) {
			throw new IllegalArgumentException(
					"Invalid no. of containers or container memory specified, exiting."
							+ " Specified containerMemory=" + containerMemory
							+ ", numContainer=" + numContainers);
		}



		log4jPropFile = cliParser.getOptionValue("log_properties", "");

		return true;
	}

	/**
	 * Main run function for the client
	 *
	 * @return true if application completed successfully
	 * @throws java.io.IOException
	 * @throws org.apache.hadoop.yarn.exceptions.YarnException
	 */
	public boolean run() throws IOException, YarnException {

		LOG.info("Running Client");
		yarnClient.start();

		YarnClusterMetrics clusterMetrics = yarnClient.getYarnClusterMetrics();
		LOG.info("Got Cluster metric info from ASM" + ", numNodeManagers="
				+ clusterMetrics.getNumNodeManagers());

		List<NodeReport> clusterNodeReports = yarnClient.getNodeReports();
		LOG.info("Got Cluster node info from ASM");
		for (NodeReport node : clusterNodeReports) {
			LOG.info("Got node report from ASM for" + ", nodeId="
					+ node.getNodeId() + ", nodeAddress"
					+ node.getHttpAddress() + ", nodeRackName"
					+ node.getRackName() + ", nodeNumContainers"
					+ node.getNumContainers());
		}

		QueueInfo queueInfo = yarnClient.getQueueInfo(this.amQueue);
		LOG.info("Queue info" + ", queueName=" + queueInfo.getQueueName()
				+ ", queueCurrentCapacity=" + queueInfo.getCurrentCapacity()
				+ ", queueMaxCapacity=" + queueInfo.getMaximumCapacity()
				+ ", queueApplicationCount="
				+ queueInfo.getApplications().size()
				+ ", queueChildQueueCount=" + queueInfo.getChildQueues().size());

		List<QueueUserACLInfo> listAclInfo = yarnClient.getQueueAclsInfo();
		for (QueueUserACLInfo aclInfo : listAclInfo) {
			for (QueueACL userAcl : aclInfo.getUserAcls()) {
				LOG.info("User ACL Info for Queue" + ", queueName="
						+ aclInfo.getQueueName() + ", userAcl="
						+ userAcl.name());
			}
		}

		// Get a new application id
		YarnClientApplication app = yarnClient.createApplication();
		GetNewApplicationResponse appResponse = app.getNewApplicationResponse();
		// TODO get min/max resource capabilities from RM and change memory ask
		// if needed
		// If we do not have min/max, we may not be able to correctly request
		// the required resources from the RM for the app master
		// Memory ask has to be a multiple of min and less than max.
		// Dump out information about cluster capability as seen by the resource
		// manager
		int maxMem = appResponse.getMaximumResourceCapability().getMemory();
		LOG.info("Max mem capabililty of resources in this cluster " + maxMem);

		// A resource ask cannot exceed the max.
		if (amMemory > maxMem) {
			LOG.info("AM memory specified above max threshold of cluster. Using max value."
					+ ", specified=" + amMemory + ", max=" + maxMem);
			amMemory = maxMem;
		}

		// set the application name
		ApplicationSubmissionContext appContext = app
				.getApplicationSubmissionContext();
		ApplicationId appId = appContext.getApplicationId();
		appContext.setApplicationName(appName);

		// Set up the container launch context for the application master
		ContainerLaunchContext amContainer = Records
				.newRecord(ContainerLaunchContext.class);

		// set local resources for the application master
		// local files or archives as needed
		// In this scenario, the jar file for the application master is part of
		// the local resources
		Map<String, LocalResource> localResources = new HashMap<String, LocalResource>();

		LOG.info("Copy App Master jar from local filesystem and add to local environment");
		// Copy the application master jar to the filesystem
		// Create a local resource to point to the destination jar path
		FileSystem fs = FileSystem.get(conf);
		Path src = new Path(appMasterJar);
		String pathSuffix = appName + "/" + appId.getId() + "/AppMaster.jar";
		Path dst = new Path(fs.getHomeDirectory(), pathSuffix);
		fs.copyFromLocalFile(false, true, src, dst);
		FileStatus destStatus = fs.getFileStatus(dst);
		LocalResource amJarRsrc = Records.newRecord(LocalResource.class);

		// Set the type of resource - file or archive
		// archives are untarred at destination
		// we don't need the jar file to be untarred
		amJarRsrc.setType(LocalResourceType.FILE);
		// Set visibility of the resource
		// Setting to most private option
		amJarRsrc.setVisibility(LocalResourceVisibility.APPLICATION);
		// Set the resource to be copied over
		amJarRsrc.setResource(ConverterUtils.getYarnUrlFromPath(dst));
		// Set timestamp and length of file so that the framework
		// can do basic sanity checks for the local resource
		// after it has been copied over to ensure it is the same
		// resource the client intended to use with the application
		amJarRsrc.setTimestamp(destStatus.getModificationTime());
		amJarRsrc.setSize(destStatus.getLen());
		localResources.put("AppMaster.jar", amJarRsrc);

		// Setup App Master Constants
		String amJarLocation = "";
		long amJarLen = 0;
		long amJarTimestamp = 0;

		// adding info so we can add the jar to the App master container path
		amJarLocation = dst.toUri().toString();
		FileStatus shellFileStatus = fs.getFileStatus(dst);
		amJarLen = shellFileStatus.getLen();
		amJarTimestamp = shellFileStatus.getModificationTime();

		// ADD libs needed that will be untared
		// Keep it all archived for now so add it as a file...
		src = new Path(localLibJar);
		pathSuffix = appName + "/" + appId.getId() + "/Runnable.jar";
		dst = new Path(fs.getHomeDirectory(), pathSuffix);
		fs.copyFromLocalFile(false, true, src, dst);
		destStatus = fs.getFileStatus(dst);
		LocalResource libsJarRsrc = Records.newRecord(LocalResource.class);
		libsJarRsrc.setType(LocalResourceType.FILE);
		libsJarRsrc.setVisibility(LocalResourceVisibility.APPLICATION);
		libsJarRsrc.setResource(ConverterUtils.getYarnUrlFromPath(dst));
		libsJarRsrc.setTimestamp(destStatus.getModificationTime());
		localResources.put("Runnable.jar", libsJarRsrc);

		// Setup Libs Constants
		String libsLocation = "";
		long libsLen = 0;
		long libsTimestamp = 0;

		// adding info so we can add the jar to the App master container path
		libsLocation = dst.toUri().toString();
		FileStatus libsFileStatus = fs.getFileStatus(dst);
		libsLen = libsFileStatus.getLen();
		libsTimestamp = libsFileStatus.getModificationTime();

		// Set the log4j properties if needed
		if (!log4jPropFile.isEmpty()) {
			Path log4jSrc = new Path(log4jPropFile);
			Path log4jDst = new Path(fs.getHomeDirectory(), "log4j.props");
			fs.copyFromLocalFile(false, true, log4jSrc, log4jDst);
			FileStatus log4jFileStatus = fs.getFileStatus(log4jDst);
			LocalResource log4jRsrc = Records.newRecord(LocalResource.class);
			log4jRsrc.setType(LocalResourceType.FILE);
			log4jRsrc.setVisibility(LocalResourceVisibility.APPLICATION);
			log4jRsrc.setResource(ConverterUtils.getYarnUrlFromURI(log4jDst
					.toUri()));
			log4jRsrc.setTimestamp(log4jFileStatus.getModificationTime());
			log4jRsrc.setSize(log4jFileStatus.getLen());
			localResources.put("log4j.properties", log4jRsrc);
		}



		// Set local resource info into app master container launch context
		amContainer.setLocalResources(localResources);

		// Set the env variables to be setup in the env where the application
		// master will be run
		LOG.info("Set the environment for the application master");
		Map<String, String> env = new HashMap<String, String>();

		// put the AM jar into env and MOYA Runnable
		// using the env info, the application master will create the correct
		// local resource for the
		// eventual containers that will be launched to execute the shell
		// scripts
		env.put(MConstants.APPLICATIONMASTERJARLOCATION,
				amJarLocation);
		env.put(MConstants.APPLICATIONMASTERJARTIMESTAMP,
				Long.toString(amJarTimestamp));
		env.put(MConstants.APPLICATIONMASTERJARLEN,
				Long.toString(amJarLen));

		env.put(MConstants.LIBSLOCATION, libsLocation);
		env.put(MConstants.LIBSTIMESTAMP, Long.toString(libsTimestamp));
		env.put(MConstants.LIBSLEN, Long.toString(libsLen));

		env.put(MConstants.ZOOKEEPERHOSTS, ZKHosts);

		// Add AppMaster.jar location to classpath
		// At some point we should not be required to add
		// the hadoop specific classpaths to the env.
		// It should be provided out of the box.
		// For now setting all required classpaths including
		// the classpath to "." for the application jar
		StringBuilder classPathEnv = new StringBuilder(
				Environment.CLASSPATH.$()).append(File.pathSeparatorChar)
				.append("./*");
		for (String c : conf.getStrings(
				YarnConfiguration.YARN_APPLICATION_CLASSPATH,
				YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
			classPathEnv.append(File.pathSeparatorChar);
			classPathEnv.append(c.trim());
		}
		classPathEnv.append(File.pathSeparatorChar)
				.append("./log4j.properties");

		// add the runtime classpath needed for tests to work
		if (conf.getBoolean(YarnConfiguration.IS_MINI_YARN_CLUSTER, false)) {
			classPathEnv.append(':');
			classPathEnv.append(System.getProperty("java.class.path"));
		}

		env.put("CLASSPATH", classPathEnv.toString());

		amContainer.setEnvironment(env);

		// Set the necessary command to execute the application master
		Vector<CharSequence> vargs = new Vector<CharSequence>(30);

		// Set java executable command
		LOG.info("Setting up app master command");
		vargs.add(Environment.JAVA_HOME.$() + "/bin/java");
		// Set Xmx based on am memory size
		vargs.add("-Xmx" + amMemory + "m");
		// Set class name
		vargs.add(appMasterMainClass);
		// Set params for Application Master
		vargs.add("--container_memory " + String.valueOf(containerMemory));
		vargs.add("--num_containers " + String.valueOf(numContainers));
		vargs.add("--priority " + String.valueOf(moyaPriority));
		if (!localLibJar.isEmpty()) {
			vargs.add("--lib " + localLibJar + "");
		}
		if (debugFlag) {
			vargs.add("--debug");
		}

		vargs.add("1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR
				+ "/AppMaster.stdout");
		vargs.add("2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR
				+ "/AppMaster.stderr");

		// Get final commmand
		StringBuilder command = new StringBuilder();
		for (CharSequence str : vargs) {
			command.append(str).append(" ");
		}

		LOG.info("Completed setting up app master command "
				+ command.toString());
		List<String> commands = new ArrayList<String>();
		commands.add(command.toString());
		amContainer.setCommands(commands);

		// Set up resource type requirements
		// For now, only memory is supported so we set memory requirements
		Resource capability = Records.newRecord(Resource.class);
		capability.setMemory(amMemory);
		appContext.setResource(capability);

		// Service data is a binary blob that can be passed to the application
		// Not needed in this scenario
		// amContainer.setServiceData(serviceData);

		// The following are not required for launching an application master
		// amContainer.setContainerId(containerId);

		appContext.setAMContainerSpec(amContainer);

		// Set the priority for the application master
		Priority pri = Records.newRecord(Priority.class);
		// TODO - what is the range for priority? how to decide?
		pri.setPriority(amPriority);
		appContext.setPriority(pri);

		// Set the queue to which this application is to be submitted in the RM
		appContext.setQueue(amQueue);

		// Submit the application to the applications manager
		// SubmitApplicationResponse submitResp =
		// applicationsManager.submitApplication(appRequest);
		// Ignore the response as either a valid response object is returned on
		// success
		// or an exception thrown to denote some form of a failure
		LOG.info("Submitting application to ASM");

		yarnClient.submitApplication(appContext);

		// TODO
		// Try submitting the same request again
		// app submission failure?

		// Monitor the application
		return monitorApplication(appId);

	}

	/**
	 * Monitor the submitted application for completion. Kill application if
	 * time expires.
	 *
	 * @param appId
	 *            Application Id of application to be monitored
	 * @return true if application completed successfully
	 * @throws org.apache.hadoop.yarn.exceptions.YarnException
	 * @throws java.io.IOException
	 */
	private boolean monitorApplication(ApplicationId appId)
			throws YarnException, IOException {

		while (true) {

			// Check app status every 1 second.
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOG.debug("Thread sleep in monitoring loop interrupted");
			}

			// Get application report for the appId we are interested in
			ApplicationReport report = yarnClient.getApplicationReport(appId);

			LOG.info("Got application report from ASM for" + ", appId="
					+ appId.getId() + ", clientToAMToken="
					+ report.getClientToAMToken() + ", appDiagnostics="
					+ report.getDiagnostics() + ", appMasterHost="
					+ report.getHost() + ", appQueue=" + report.getQueue()
					+ ", appMasterRpcPort=" + report.getRpcPort()
					+ ", appStartTime=" + report.getStartTime()
					+ ", yarnAppState="
					+ report.getYarnApplicationState().toString()
					+ ", distributedFinalState="
					+ report.getFinalApplicationStatus().toString()
					+ ", appTrackingUrl=" + report.getTrackingUrl()
					+ ", appUser=" + report.getUser());

			YarnApplicationState state = report.getYarnApplicationState();
			FinalApplicationStatus dsStatus = report
					.getFinalApplicationStatus();
			if (YarnApplicationState.FINISHED == state) {
				if (FinalApplicationStatus.SUCCEEDED == dsStatus) {
					LOG.info("Application has completed successfully. Breaking monitoring loop");
					return true;
				} else {
					LOG.info("Application did finished unsuccessfully."
							+ " YarnState=" + state.toString()
							+ ", DSFinalStatus=" + dsStatus.toString()
							+ ". Breaking monitoring loop");
					return false;
				}
			} else if (YarnApplicationState.KILLED == state
					|| YarnApplicationState.FAILED == state) {
				LOG.info("Application did not finish." + " YarnState="
						+ state.toString() + ", DSFinalStatus="
						+ dsStatus.toString() + ". Breaking monitoring loop");
				return false;
			}
		}

	}

	
//	/**
//	 * Kill a submitted application by sending a call to the ASM
//	 * 
//	 * @param appId
//	 *            Application Id to be killed.
//	 * @throws YarnException
//	 * @throws IOException
//	 */
//	private void forceKillApplication(ApplicationId appId)
//			throws YarnException, IOException {
//		// TODO clarify whether multiple jobs with the same app id can be
//		// submitted and be running at
//		// the same time.
//		// If yes, can we kill a particular attempt only?
//
//		// Response can be ignored as it is non-null on success or
//		// throws an exception in case of failures
//		yarnClient.killApplication(appId);
//	}

}
