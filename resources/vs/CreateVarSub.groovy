#!/opt/bpa/groovy-2.4.3/bin/groovy
package com.softwaerag.gcs.wx.bdas.projectAutomator

import java.nio.file.Path

import groovy.xml.*

import groovy.xml.MarkupBuilder

def cli = new CliBuilder (usage:'createdeployerproject.groovy -r REPOSITORY_PATH -f OUTPUTFILE -p PROJECT -t TARGETSUFFIX')
cli.with {
	h longOpt:'help', 'Usage information'
	a longOpt:'a',argName:'paTemplate', args:1, 'Path to project automator template file'
	p longOpt:'project', argName:'project', args:1, 'Example: BDA_Automator_Project'
	t longOpt:'target', argName:'target', args:1, 'Must have a matching entry in the ENV.groovy flie. Example: DEV.'
	vs longOpt:'varSubDirPath', argName:'varSubDirPath', args:1, 'Base dir with the varsub files'
	o longOpt:'varSubOutputDirPath', argName:'varSubOutputDirPath', args:1, 'Output directory for varsub files'
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
assert opts.p
assert opts.t
assert opts.vs
assert opts.o


def paTemplate = opts.a
def project = opts.p
def target = opts.t
def varSubDirPath = opts.vs
def varSubOutputDirPath = opts.o

println "Creating VarSub file with the following properties:"
println "\t- Project: '${project}'"
println "\t- Project Automator Template: '${paTemplate}'"
println "\t- Target: '${target}'"
println "\t- Varsub base dir: '${varSubDirPath}'"
println "\t- varSubOutputDirPath: '${varSubOutputDirPath}'"

def currentDir = new File(".")
def varSubDir = new File(varSubDirPath)
def varSubEnvDir = new File(varSubDir, target)
assert varSubDir.exists() : "varSubDir for path '${varSubDirPath}' does not exist. Root dir: ${currentDir.getAbsolutePath()}"
if( !varSubEnvDir.exists()) {
	println "varSubDir for environment '${target}' does not exist. Expected directory at: '${varSubDir.getAbsolutePath()}/${target}'. No variable substitution will be done..."
	return;
}
def varSubOutputDir = new File(varSubOutputDirPath)
if( !varSubOutputDir.exists() ) {
	varSubOutputDir.mkdirs()
}
def paTemplateXml = new XmlSlurper().parse(new File(paTemplate))

def varSubEnvDirs = ["IS": new File(varSubEnvDir, "is"), "BPM": new File(varSubEnvDir, "bpm"), "MWS": new File(varSubEnvDir, "mws")]

//Map targetsIs = { [:].withDefault{ owner.call() } }()
def deploymentMaps = [:]
def deploymentSets = [:]

// parse project automator template and extract all composites
paTemplateXml.Projects.Project.MapSetMapping.findAll{true}.each { msm ->
	String dsName = msm.@setName
	String mapName = msm.@mapName
	deploymentMaps[mapName] = []
	msm.alias.findAll{true}.each { alias ->
		String aliasName = alias.text()
		String aliasType = alias.@type
		println "found alias '${aliasName}' of type '${aliasType}' for DeploymentSet '${dsName}'"
		deploymentMaps[mapName].add([dsName: dsName, targetServerName: aliasName, targetServerType: aliasType])
	}
}

paTemplateXml.Projects.Project.DeploymentSet.findAll {true}.each { ds ->
	String dsName = ds.@name
	deploymentSets[dsName] = [:]
	ds.Composite.findAll{true}.each{ composite ->
		String compositeName = composite.@name
		String compositeType = composite.@type
		String compositeDisplayName = composite.@displayName
		String compositeSrcAlias = composite.@srcAlias
		def s = deploymentSets[dsName]
		s[compositeType] = s[compositeType] ? s[compositeType] : []
		s[compositeType].add([compositeName: compositeName, compositeDisplayName: compositeDisplayName, compositeSrcAlias: compositeSrcAlias])
	}
}


// now create a new varsub file for all composites and all environments
deploymentMaps.each {  mapName, targetServers ->
	println "- creating varsub for DeploymentMap ${mapName}"
	File vsOutputFile = new File(varSubOutputDir, mapName.toString() + ".vs.xml")
	PrintWriter vsOutputFileWriter = new PrintWriter(new BufferedWriter(new FileWriter(vsOutputFile)));
	def varSubXml = new StreamingMarkupBuilder()
	String vsxml = varSubXml.bind {
		Root {
			targetServers.each { targetServer ->
				//				println "- creating varsub for ds ${targetServer.dsName} and targetServer ${targetServer.targetServerName} of type ${targetServer.targetServerType}"
				def deploymentSetName = targetServer.dsName
				def targetServerName = targetServer.targetServerName
				def targetServerType = targetServer.targetServerType
				//				println "deploymentSetName: ${deploymentSetName}, targetServerName: ${targetServerName}, targetServerType: ${targetServerType}"

				def deploymentSet = deploymentSets[deploymentSetName]
				deploymentSet[targetServerType].each { composite ->
					def assetCompositeName = composite.compositeName
					def compositeDisplayName = composite.compositeDisplayName
					def compositeSrcAlias = composite.compositeSrcAlias
					// get varsub xml for this composite
					def vsTemplateFile = new File(varSubEnvDirs[targetServerType], "${assetCompositeName}.vs.xml")
					if( vsTemplateFile.exists() ) {
						def p = new XmlSlurper().parse( vsTemplateFile )
						def deploymentSetNodeName = p.name()
						"$deploymentSetNodeName"(
								assetCompositeName: assetCompositeName,
								deploymentSetName: deploymentSetName,
								serverAliasName: compositeSrcAlias,
								targetServerName: targetServerName,
								targetServerType: targetServerType) {
									mkp.yield p.children()
								}
					}
				}
			}
		}
	}
	println XmlUtil.serialize( vsxml, vsOutputFileWriter )
	vsOutputFileWriter.close()
	println "wrote varsub file ${vsOutputFile.getAbsolutePath()}"
}
