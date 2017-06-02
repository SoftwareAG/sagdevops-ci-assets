#!/opt/bpa/groovy-2.4.3/bin/groovy
package com.softwaerag.gcs.wx.bdas.projectAutomator

import java.nio.file.Path
import groovy.xml.*

import groovy.xml.MarkupBuilder

def cli = new CliBuilder (usage:'ExtractVarSub.groovy -varsubFilePath varSubFilePath -varsubDirPath varSubTemplateDirPath -target targetEnvironment')
cli.with {
	h longOpt:'help', 'Usage information'
	varsubFilePath longOpt:'varsubFilePath',argName:'varsubFilePath', args:1, 'Path to the varsub file exported from Deployer'
	varsubDirPath longOpt:'varsubDirPath', argName:'varsubDirPath', args:1, 'Path to the dir with the varsub templates'
	target longOpt:'target', argName:'target', args:1, 'Must have a matching entry in the ENV.groovy flie. Example: DEV.'
}
def opts = cli.parse(args)
org.codehaus.groovy.ant.Groovy
if(!opts) return
	if(opts.help) {
		cli.usage()
		return
	}

assert opts
assert opts.varsubFilePath
assert opts.varsubDirPath
assert opts.target


def varsubFilePath = opts.varsubFilePath
def varsubDirPath = opts.varsubDirPath
def target = opts.target

println "Creating VarSub file with the following properties: "
println "\t- Varsub file exported from Deployer: '${varsubFilePath}'"
println "\t- Directory where to store varsub template files: '${varsubDirPath}'"
println "\t- Target environment: '${target}'"

def currentDir = new File(".")
def varSubDir = new File(varsubDirPath)
def varSubEnvDir = new File(varSubDir, target)
if( !varSubEnvDir.exists() ) {
	varSubEnvDir.mkdirs()
	println "Created varsub directory '${varSubEnvDir.getAbsolutePath()}' for target environment '${target}'"
}
// varsub xml files for IntegrationServer packages must be stored in a sub folder called "is"
def varSubEnvISDir = new File(varSubEnvDir, "is")
// varsub xml files for MWS assets must be stored in a sub folder called "mws"
def varSubEnvMWSDir = new File(varSubEnvDir, "mws")

// get the varsub file which was exported from Deployer
def varsubFile = new File(varsubFilePath)
assert varsubFile.exists() : "Varsubfile '${varsubFile}' does not exist"
def varsubFileXml = new XmlSlurper().parse(varsubFile)

// parse the varsub file and get all IS composites
varsubFileXml.'**'.findAll{ (it.name() == 'DeploymentSet' || it.name() == 'DepoeymentSet') && it.@targetServerType == 'IS'}.each { ISDeploementSet ->
//varsubFileXml.'**'.findAll{ it.name() == 'DeploymentSet' && it.@targetServerType == 'IS'}.each { ISDeploementSet ->
	def assetName = "" + ISDeploementSet.@assetCompositeName
	def deploymentSetName = ISDeploementSet.name()
	println "Found varsub for IS package '${assetName}'"
	// create a new XML with a StreamingMarkupBuilder, which will store the varsub for this IS package
	def varSubXml = new StreamingMarkupBuilder()
	// give the new varsub for this IS package the standard name ${assetName}.vs.xml
	def outputFile = new File(varSubEnvISDir, assetName + ".vs.xml")
	// check if a varsub file already exists for this package. If so, create a numbered backup
	if( outputFile.exists() ) {
		def backupFile = new File(varSubEnvISDir, assetName + ".vs.xml.bak")
		def i=0
		while( backupFile.exists() ) {
			backupFile = new File(varSubEnvISDir, assetName + ".vs.xml.bak" + ++i)
		} 
		backupFile << outputFile.text
		println "created backup '${backupFile.getAbsolutePath()}' of existing varsub file"
	}
	PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
	// create the varsub xml using the StreamingMarkupBuilder
	String nxml = varSubXml.bind {
		"$deploymentSetName" {
			// add the string representation of the current deployment set to the varsub file
			// note: "ISDeploementSet.children().toString()" returns a valid xml string
			mkp.yield ISDeploementSet.children()
		}
	}
	// write the xml to file
	println XmlUtil.serialize( nxml, writer )
	writer.close()
	println "done creating varsub template '${outputFile.getAbsolutePath()}'"
}
