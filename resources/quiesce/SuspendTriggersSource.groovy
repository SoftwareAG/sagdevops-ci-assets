#!/opt/bpa/groovy-2.4.3/bin/groovy
package com.softwaerag.gcs.wx.bdas.build.quiesce

import groovy.io.FileType;


def cli = new CliBuilder (usage:'SuspendTriggersSource.groovy ')
cli.with {
	h longOpt:'help', 'Usage information'
	s longOpt:'pathToIsPackageSource',argName:'pathToIsPackageSource', args:1, required:true, 'Path to the the checkout/source dir with the IS packages"'
	p longOpt:'packageList',argName:'packageList', args:1, optionalArg:true, '[optional] Comma separated list of package names for which to suspend triggers. If not provided, all packages in the source dir are scanned.'
	t longOpt:'triggerType',argName:'triggerType', args:1, required:true, 'Type of triggers to suspend. Valid options are [all|jms|messaging]'
}
def opts = cli.parse(args)
org.codehaus.groovy.ant.Groovy
if(!opts) return
	if(opts.help) {
		cli.usage()
		return
	}

assert opts
assert opts.pathToIsPackageSource
assert opts.triggerType.equalsIgnoreCase("all")  ||opts.triggerType.equalsIgnoreCase("messaging")  ||opts.triggerType.equalsIgnoreCase("jms")

def pathToIsPackageSource = opts.pathToIsPackageSource
def packageList = opts.packageList
def triggerType = opts.triggerType

def packagesDir = new File(pathToIsPackageSource)
assert packagesDir.exists() : "Path to IS package directory '${pathToIsPackageSource}' does not exist"

// create list of all packages, either all in the dir or all provided as CSV by user
def packageArr = []
if( !packageList ) {
	// get list of all packages in the package dir
	packagesDir.eachFile (FileType.DIRECTORIES) { dir ->
		packageArr << dir.name
	}
} else {
	packageArr = packageList.split(",").collect { it.trim() }
}

println "suspending triggers for all packages in dir '${packagesDir.getAbsolutePath()}'. Packages: "
packageArr.each { println "- " + it }

packageArr.each { packageName ->
	def packageDir = new File(packagesDir, packageName)
	if( !packageDir.exists() ) {
		println "Package ${packageName} does not exist in directory ${packagesDir.getAbsolutePath()}."
		return
	}
	// start in the ns dir of a package
	def nsDir = new File(packageDir, "ns")
	if( !nsDir.exists() ) {
		// this is no valid IS package directory since the "ns" directory is missing
		println "Folder ${packageName} in directory ${packagesDir.getAbsolutePath()} does not represent a valid IS package, since the 'ns' folder is missing. Ignoring."
		return;
	}
	println ("Searching for all triggers in package '" + packageName + "'.")
	nsDir.traverse(
			type : FileType.FILES, // filter for files
			nameFilter: ~/node.ndf/, // with name node.df
			preDir: { if (it.name == '.svn' || it.name == '.git') return SKIP_SUBTREE } // which arent a VCS dir 
			) { nodeNdf ->
				// slurp node.ndf xml
				def node = new XmlSlurper().parse(nodeNdf)
				// find the following entry  <value name="trigger_type">jms-trigger</value>
				node.'value'.findAll { value ->
					value.attributes().get("name") == "trigger_type"
				}.each() { trigger -> 
					println "found trigger of type '${trigger}' in file '${nodeNdf.getAbsolutePath()}'"
					// cretae backup of node.ndf file
					def backupFile = new File(nodeNdf.getParent(), "node.ndf.triggerBak")
					backupFile.write(nodeNdf.text)
					// check if it is a JMS or a Messaging trigger
					if( trigger == "jms-trigger" ) {
						// replace <Boolean name="enabled">true</Boolean> with <Boolean name="enabled">false</Boolean>
						node.record.Boolean.replaceBody 'false'
						nodeNdf.write(groovy.xml.XmlUtil.serialize( node ))
					} else if(trigger == "broker-trigger") {
						// find two nodes in broker (i.e. messaging) trigger node.ndf, which both represent the supsended state of this trigger 
						node.record.'value'.findAll { recordValue ->
							recordValue.attributes().get("name") == "processingSuspended" ||
							recordValue.attributes().get("name") == "retrievalSuspended"
						}.each() {
							it.replaceBody 'true'
						}
						nodeNdf.write(groovy.xml.XmlUtil.serialize( node ))
					} else {
						assert false : "Unknown trigger type..."
					}
				}
			};
}

