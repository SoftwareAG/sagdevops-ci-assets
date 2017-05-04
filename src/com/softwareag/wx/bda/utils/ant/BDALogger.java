package com.softwareag.wx.bda.utils.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.PropertyConfigurator;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import com.softwareag.wx.bda.utils.ant.BDALogger;

public class BDALogger extends Task {

	public static void main(String args[]) {
		BDALogger t = new BDALogger();
		t.setMsg("test");
		t.setLevel("INFO");
		t.execute();
	}

	private static Properties logProperties = null;

	public void init() {
		boolean propertiesLoaded = false;
		StringBuffer logString = new StringBuffer();
		if (logProperties == null) {

			String logPropertiesPath = getProject().getProperty(
					"bda.logging.log4j.propertiesFile");
			File logPropertiesFile = null;
			if (logPropertiesPath == null || "".equals(logPropertiesPath)) {
				logPropertiesPath = "resources/log4j.properties";
				logString.append("BDA: Property 'bda.logging.log4j.propertiesFile' is not set. Trying to load log4j properties file from '"
						+ logPropertiesPath + "'.");
				logPropertiesFile = new File(logPropertiesPath);
				if (logProperties == null || !logPropertiesFile.exists()) {
					logPropertiesPath = "log4j.properties";
					logString.append("BDA: Trying to load log4j properties file from '"
							+ logPropertiesPath + "'.");
					logPropertiesFile = new File(logPropertiesPath);
				}
			} else {
				logString.append("BDA: Trying to loading log4j properties file as defined in the properties 'bda.logging.log4j.propertiesFile' from '"
						+ logPropertiesPath + "'.");
				logPropertiesFile = new File(logPropertiesPath);
			}
			logProperties = new Properties();
			try {
				logProperties.load(new FileInputStream(logPropertiesFile));
				propertiesLoaded = true;
			} catch (FileNotFoundException fnfe) {
				log("BDA: log4j properties file '" + logPropertiesPath
						+ "' for test automation could not be found: " + fnfe,
						Project.MSG_ERR);
			} catch (IOException ioe) {
				log("BDA: Exception occured while loading log4j properties file '"
						+ logPropertiesPath + "' for test automation: " + ioe,
						Project.MSG_ERR);
			}
			if (propertiesLoaded == false) {
				log(logString.toString(),
						Project.MSG_VERBOSE);
				logProperties.setProperty("log4j.logger.TestAutomation",
						"INFO, myFileAppender");
				logProperties.setProperty("log4j.appender.myFileAppender",
						"org.apache.log4j.DailyRollingFileAppender");
				logProperties.setProperty(
						"log4j.appender.myFileAppender.datePattern",
						"'.'yyyyMMdd");
				logProperties.setProperty("log4j.appender.myFileAppender.File",
						"logs/testautomation.log");
				logProperties.setProperty(
						"log4j.appender.myFileAppender.layout",
						"org.apache.log4j.PatternLayout");
				logProperties
						.setProperty(
								"log4j.appender.myFileAppender.layout.ConversionPattern",
								"%d{ISO8601} %p [%c] - %m%n");
			}
			PropertyConfigurator.configure(logProperties);
		}
	}

	private static Logger l = Logger.getLogger("TestAutomation");

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	private String msg;
	private String level = "INFO";

	public void execute() throws BuildException {
		init();
		Project p = this.getProject();
		this.msg = "[" + p.getName() + ":" + this.getOwningTarget() + "] "
				+ msg;
		if (this.level.toLowerCase().equals("info")) {
			l.info(this.msg);
			log(this.msg, Project.MSG_INFO);
		} else if (this.level.toLowerCase().equals("error")) {
			l.error(this.msg);
			log(this.msg, Project.MSG_ERR);
		} else if (this.level.toLowerCase().equals("trace")) {
			l.trace(this.msg);
			log(this.msg, Project.MSG_VERBOSE);
		} else {
			l.trace(this.msg);
			log(this.msg, Project.MSG_INFO);
		}
	}

}