#!/opt/bpa/groovy-2.4.3/bin/groovy
package com.softwaerag.gcs.wx.bdas.projectAutomator

import java.nio.file.Path

@Grapes([
	@Grab(group='commons-logging', module='commons-logging', version='1.2'),
	@Grab(group='org.springframework', module='spring-core', version='4.3.5.RELEASE'),
	@Grab(group='org.apache.httpcomponents', module='httpcore', version='4.3.3'),
	@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.0'),
	@Grab(group='org.apache.ant', module='ant', version='1.9.8'),
	@Grab(group='org.apache.ant', module='ant-launcher', version='1.9.8'),
	@Grab(group='org.apache.ivy', module='ivy', version='2.4.0')

]
)
import org.apache.http.entity.FileEntity

import groovy.lang.Grab
import groovy.lang.Grapes
import groovy.swing.SwingBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import groovy.xml.*

import groovy.xml.MarkupBuilder

def cli = new CliBuilder (usage:'createdeployerproject.groovy -r REPOSITORY_PATH -f OUTPUTFILE -p PROJECT -t TARGETSUFFIX')
cli.with {
	h longOpt:'help', 'Usage information'
	a longOpt:'a',argName:'paTemplate', args:1, 'Path to project automator template file'
	f longOpt:'outputfile', argName:'outputfile', args:1, 'Example: projectAutomator.xml'
	p longOpt:'project', argName:'project', args:1, 'Example: BDA_Automator_Project'
	t longOpt:'target', argName:'target', args:1, 'Must have a matching entry in the ENV.groovy flie. Example: DEV.'
	vs longOpt:'varSubDirPath', argName:'varSubDirPath', args:1, 'Base dir with the varsub files'
}
def opts = cli.parse(args)
org.codehaus.groovy.ant.Groovy
if(!opts) return
	if(opts.help) {
		cli.usage()
		return
	}


assert opts
assert opts.a
assert opts.f
assert opts.p
assert opts.t
assert opts.vs


def paTemplate = opts.a
def outputFile = opts.f
def project = opts.p
def target = opts.t
def varSubDirPath = opts.vs

println "Creating VarSub file with the following properties:"
println "\t- Project: '${project}'"
println "\t- Project Automator Template: '${paTemplate}'"
println "\t- Output File: '${outputFile}'"
println "\t- Target: '${target}'"
println "\t- Varsub base dir: '${varSubDirPath}'"

def currentDir = new File(".")
def varSubDir = new File(varSubDirPath)
def varSubEnvDir = new File(varSubDir, target)
assert varSubDir.exists() : "varSubDir for path '${varSubDirPath}' does not exist. Root dir: ${currentDir.getAbsolutePath()}"
if( !varSubEnvDir.exists()) 
	println "varSubDir for environment '${target}' does not exist. Expected directory at: '${varSubDir.getAbsolutePath()}/${target}'. No variable substitution will be done..."

def outFile = new File(outputFile)
//def writer = new FileWriter(outFile)
PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
def xml = new MarkupBuilder(writer)
def varSubXml = new StreamingMarkupBuilder()
def packages = []
def bpms = []
def mws = []
def tn = []
def isTargets = []
def bpmsTargets = []
def mwsTargets = []

def paTemplateXml = new XmlSlurper().parse(new File(paTemplate))
// parse project automator template and extract all composites
def DeploymentSet = paTemplateXml.Projects.Project.DeploymentSet
DeploymentSet.Composite.findAll {true}.each { Composite ->
	if( Composite.@type == 'IS') {
//		println("found IS package composite" + XmlUtil.serialize(Composite))
		packages.add([name: Composite.@name, srcAlias: Composite.@srcAlias])
	}
}
// parse project automator template and extract all environments, i.e. targets
def Environment = paTemplateXml.Environment
Environment.IS.'*'.findAll {true}.each { isalias ->
	if( isalias.name() == 'isalias') {
//		println("found IS environment" + XmlUtil.serialize(env))
		isTargets.add([alias: isalias.@name])
	}
}

String nxml = varSubXml.bind {
	Root {
		def varSubEnvIsDir = new File(varSubEnvDir, "is")
		if( varSubEnvDir.exists() ) {
			packages.each { isPackage ->
				// first check if there is a varsub file for this package and the given target environment
				println "checking varsub for package ${isPackage.name}"
				def packageVarSubFile = new File(varSubEnvIsDir, "${isPackage.name}.vs.xml")
				if( packageVarSubFile.exists() ) {
					def p = new XmlSlurper().parse( packageVarSubFile )
					isTargets.each { isTarget ->
						println "creating varsub for package ${isPackage.name} and target ${isTarget.alias}"
						DeploementSet(
							assetCompositeName:"${isPackage.name}",
							deploymentSetName: 'DeploymentSet',
							serverAliasName: "${isPackage.srcAlias}",
							targetServerName: "${isTarget.alias}",
							targetServerType: "IS") {
							mkp.yield p.children()
						}
					}
				} else {
					println "No varsub file exists for package ${isPackage.name}. Expected varsub at '${varSubEnvIsDir.getAbsolutePath()}/${isPackage.name}.vs.xml'"
				}
			}
		} else {
			println "no var sub dir for is assets exists at '" + varSubEnvDir.getAbsolutePath() + "/is'"
		}
	  }
}
println XmlUtil.serialize( nxml, writer )

writer.close()
println "created varsub file '${outputFile}'"
