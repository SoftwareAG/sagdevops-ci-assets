#!/opt/bpa/groovy-2.4.3/bin/groovy
package com.softwaerag.gcs.wx.bdas.projectAutomator

import groovy.xml.dom.DOMCategory
import groovy.xml.MarkupBuilder
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil

def cli = new CliBuilder (usage:'createdeployerproject.groovy [options]')
// command line argument line example: -r c:\Users\Administrator\github\henning-sagdevops-ci-assets\tmp\fbr\Henning-local-sagdevops-ci-assets_fbrRepo -f out.xml -p BDA_TEST_Henning-local-sagdevops-ci-assets -t TEST -d localhost:5555 -u Administrator -s manage -e c:\Users\Administrator\github\henning-webmethods-sample-project-layout\ENV.groovy -repoName repoName -splitDeploymentSets false
cli.with {
	h longOpt:'help', 'Usage information'
	r longOpt:'repository',argName:'repository', args:1, 'Path to ABE file based repository'
	f longOpt:'outputfile', argName:'outputfile', args:1, 'Example: projectAutomator.xml'
	p longOpt:'project', argName:'project', args:1, 'Example: BDA_Automator_Project'
	t longOpt:'target', argName:'target', args:1, 'Must have a matching entry in the ENV.groovy flie. Example: DEV.'
	d longOpt:'deployer.hostPort', argName:'deployerHostPort', args:1, 'Example: localhost:5555.'
	u longOpt:'deployer.user', argName:'deployerUser', args:1, 'Example: Administrator.'
	s longOpt:'deployer.password', argName:'deployerPassword', args:1, 'Example: manage.'
	e longOpt:'environments', argName:'environments', args:1, 'Example: ENV.groovy.'
	repoName longOpt:'repoName', argName:'repoName', args:1, 'Name of the Repository to create in deployer'
	splitDeploymentSets longOpt:'splitDeploymentSets', argName:'splitDeploymentSets', args:1, 'Boolean. If true, for each IntegrationServer a separate Deployment Set is created and deployments are done sequentially. Default: false.'
}
def opts = cli.parse(args)
org.codehaus.groovy.ant.Groovy
if(!opts) return
	if(opts.help) {
		cli.usage()
		return
	}

assert opts

project = opts.p
def repo = opts.r
def outputFile = opts.f
target = opts.t
def deployerHostPort = opts.d
def deployerUser = opts.u
def deployerPassword = opts.s
def environments = opts.e
repoName = opts.repoName
splitDeploymentSets = new Boolean(opts.splitDeploymentSets)

println "> Creating Project Automator templates with the following properties:"
println "\t- Project: '${project}'"
println "\t- Repository: '${repo}'"
println "\t- Output File: '${outputFile}'"
println "\t- Target: '${target}'"
println "\t- Deployer host: '${deployerHostPort}'"
println "\t- Deployer user: '${deployerUser}'"
println "\t- Deployer passsword: *****"
println "\t- Environments definition: '${environments}'"
println "\t- Repository name: '${repoName}'"
println "\t- SplitDeploymentSets: '${splitDeploymentSets}'"

assert project
assert repo
assert outputFile
assert target
assert deployerHostPort
assert deployerUser
assert deployerPassword
assert environments
assert repoName

//println "${opts.arguments()}"

def outFile = new File(outputFile)
//def writer = new FileWriter(outFile)
PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));

//def xml = new MarkupBuilder(writer)
def xml = new StreamingMarkupBuilder();
packages = []
bpms = []
mws = []
tn = []


def configSlurper = new ConfigSlurper(target)
configSlurper.classLoader = this.class.getClassLoader()
config = configSlurper.parse(new File(environments).toURL())
//def config = configSlurper.parse(configText)

// first parse the file-based repository

def repoDir = new File(repo)
assert repoDir.exists() : "Repository directory '${repo}' does not exist"

// we only support packages, processes and MWS projects currently...
repoDir.eachDirRecurse() { dir ->
	dir.eachFileMatch(~/.*.acdl/) { file ->
		def doc  = groovy.xml.DOMBuilder.parse(new FileReader(file))
		def asset_composite = doc.documentElement
		use (DOMCategory) {
			def implementation_generic = asset_composite.'implementation.generic'
			def type=implementation_generic.'@type'[0]
			def displayName = asset_composite.'@displayName'
			def name = asset_composite.'@name'

			println "found acdl ${file} of type=${type} with name=${name} and displayName=${displayName}"

			if(type == 'bpmprocess') {
				bpms.add([name: name, displayName: displayName])
			} else if (type == 'ispackage' || type == 'isconfiguration') {
				packages.add([name: name, displayName: name])
			} else if (type == 'war') {
				mws.add([name: name, displayName: displayName])
			} else if (type == 'pdp') {
				mws.add([name: name, displayName: displayName])
			} else if (type == 'cdp') {
				mws.add([name: name, displayName: displayName])
			}
		}
	}
}

/**
 * craeetes a  deployment maps for IntegrationServers only, if splitDeploymentSets is true
 * @param xml
 * @param splitDeploymentSets
 * @return
 */
def createISOnlyDeploymentMapSetMappingAndDeploymentCandidate(splitDeploymentSets) {
	if( splitDeploymentSets ) {
		if(config.IntegrationServers.size() > 0  && !packages.empty) {
			def ds = new StreamingMarkupBuilder().bind() {
				config.IntegrationServers.keySet().each {
					def isAliasName = it.toString()
					DeploymentMap(description:"Deployment Map for IS DeploymentSet to IS node ${isAliasName}", name:"IS_DeploymentMap_${isAliasName}")
				}
				config.IntegrationServers.keySet().each {
					def isAliasName = it.toString()
					MapSetMapping(mapName: "IS_DeploymentMap_${isAliasName}", setName: "IS_DeploymentSet_${isAliasName}") {
						alias(type:'IS', "${target}_${isAliasName}")
					}
				}
				config.IntegrationServers.keySet().each {
					def isAliasName = it.toString()
					DeploymentCandidate(description: "Deployment to IS node ${isAliasName} only", mapName: "IS_DeploymentMap_${isAliasName}", name: "IS_Deployment_${isAliasName}")
				}
			}
			String output = ds.toString()
			return output
		}
	}
	return ""
}

/**
 * craeetes a standard deployment map set mapping for the standard deployment set (named DeploymentSet)
 * IS assets will only be added if splitDeploymentSets==false
 * @param xml
 * @return
 */
def createDeploymentMapSetMappingAndDeploymentCandidate(splitDeploymentSets) {
	boolean hasChildren = false;
	def ds = new StreamingMarkupBuilder().bindNode() {
		DeploymentMap(description:"Deployment Map for standard Deployment Set", name:"DeploymentMap")
		MapSetMapping(mapName: 'DeploymentMap', setName: 'DeploymentSet') {
			if(config.IntegrationServers.size() > 0 && !splitDeploymentSets) {
				config.IntegrationServers.keySet().each {
					alias(type:'IS', "${target}_${it}")
					hasChildren = true
				}
			}
			if(config.ProcessModels.size() > 0 ) {
				config.ProcessModels.keySet().each {
					alias(type:'BPM', "${target}_${it}")
					hasChildren = true
				}
			}
			if(config.MWS.size() > 0 ) {
				config.MWS.keySet().each {
					alias(type:'MWS', "${target}_${it}")
					hasChildren = true
				}
			}
		}
		DeploymentCandidate(description: "Deployment", mapName: "DeploymentMap", name: "Deployment")
	}
	def output = ds.toString()
	if( hasChildren ) {
		return output;
	}
	return "";
}

/**
 * Creates the DelpoymentSet for IS only, one DeploymentSet for each configured IS target node. 
 * The DeploymentSets will only be created if the parameter splitDeploymentSet is true
 * @param xml
 * @param splitDeloymentSets
 * @return
 */
def createISOnlyDeploymentSets(splitDeploymentSets) {
	if( splitDeploymentSets ) {
		if(config.IntegrationServers.size() > 0 && !packages.empty) {
			def ds = new StreamingMarkupBuilder().bind() {
				config.IntegrationServers.keySet().each {
					DeploymentSet (autoResolve:'ignore', description: 'Deployment set containing only is assets for IS target node ' + it, name: "IS_DeploymentSet_${it}", srcAlias:"${repoName}") {
						packages.each() {
							Composite (name:"${it.name}", displayName:"${it.name}", srcAlias:"${repoName}", type:'IS')
						}
					}
				}
			}
			String output = ds.toString()
			return output
		}
	}
	return ""
}

/**
 * craeetes a standard deployment set for all collected assets
 * IS assets will only be added if splitDeploymentSets==false
 * @param xml
 * @return
 */
def createDeploymentSets(splitDeploymentSets) {
	boolean hasChildren = false
	def builder = new StreamingMarkupBuilder()
	def ds = builder.bindNode() {
		DeploymentSet (autoResolve:'ignore', description: 'deployment set containing all assets', name: 'DeploymentSet', srcAlias:"${repoName}") {
			if (!packages.empty && !splitDeploymentSets) {
				packages.each() {
					Composite (name:"${it.name}", displayName:"${it.name}", srcAlias:"${repoName}", type:'IS')
					hasChildren = true
				}
			}
			if (!bpms.empty) {
				bpms.each() {
					Composite (name:"${it.name}", displayName:"${it.displayName}", srcAlias:"${repoName}", type:'BPM')
					hasChildren = true
				}
			}
			if (!mws.empty) {
				mws.each() {
					Composite (name:"${it.name}", displayName:"${it.displayName}", srcAlias:"${repoName}", type:'MWS')
					hasChildren = true
				}
			}
		}
	}
	def output = ds.toString()
	if( hasChildren ) {
		return output;
	}
	return "";
}

// creation of deployerspec
//xml.setDoubleQuotes(true)
def nxml = xml.bind() {
	DeployerSpec(exitOnError:'true', sourceType:'Repository') {
		DeployerServer {
			host("${deployerHostPort}")
			user("${deployerUser}")
			pwd("${deployerPassword}")
		}
		Environment {
			if(config.IntegrationServers.size() > 0 ) {
				IS {
					config.IntegrationServers.each { name, isConfig ->
						/*
						 * Merge explicit values in isConfig (ConfigSlurfer ConfigObject) with default
						 * values in config.IntegrationServer.defaults (hashmap), then merge again
						 * with explicit values (isConfig), so that default values do not replace
						 * explicit values, but only append
						 */
						def integ = isConfig + config.IntegrationServer.defaults + isConfig;
						isalias(name: "${target}_${name}") {
							host(integ.host)
							port(integ.port)
							user(integ.username)
							if( integ.isSet('pwdHandle') ) {
								pwdHandle(integ.pwdHandle)
							} else {
								pwd(integ.pwd)
							}
							useSSL(integ.useSSL)
							version(integ.version)
							installDeployerResource(integ.installDeployerResource)
							Test(integ.test)
						}
					}
				}
			}
			if(config.ProcessModels.size() > 0 ) {
				ProcessModel {
					config.ProcessModels.each { name, bpmConfig ->
						def bpm = bpmConfig + config.ProcessModel.defaults + bpmConfig
						pmalias(name: "${target}_${name}") {
							host(bpm.host)
							port(bpm.port)
							user(bpm.username)
							if( bpm.isSet('pwdHandle') ) {
								pwdHandle(bpm.pwdHandle)
							} else {
								pwd(bpm.pwd)
							}
							useSSL(bpm.useSSL)
							version(bpm.version)
							Test(bpm.test)
						}
					}
				}
			}
			if(config.MWS.size() > 0 ) {
				MWS {
					config.MWS.each { name, mwsConfig ->
						def mwsConfigMerged = mwsConfig + config.MyWebmethodsServer.defaults + mwsConfig
						mwsalias(name: "${target}_${name}") {
							host(mwsConfigMerged.host)
							port(mwsConfigMerged.port)
							user(mwsConfigMerged.username)
							if( mwsConfigMerged.isSet('pwdHandle') ) {
								pwdHandle(mwsConfigMerged.pwdHandle)
							} else {
								pwd(mwsConfigMerged.pwd)
							}
							useSSL(mwsConfigMerged.useSSL)
							version(mwsConfigMerged.version)
							excludeCoreTaskEngineDependencies(mwsConfigMerged.excludeCoreTaskEngineDependencies)
							cacheTimeOut(mwsConfigMerged.cacheTimeOut)
							includeSecurityDependencies(mwsConfigMerged.includeSecurityDependencies)
							rootFolderAliases(mwsConfigMerged.rootFolderAliases)
							maximumFolderObjectCount(mwsConfigMerged.maximumFolderObjectCount)
							enableAddtionalLogging(mwsConfigMerged.enableAddtionalLogging)
							maxFolderDepth(mwsConfigMerged.maxFolderDepth)
							Test(mwsConfigMerged.test)
						}
					}
				}
			}
			Repository {
				repalias(name: "${repoName}") {
					type("FlatFile")
					urlOrDirectory("${repo}")
					Test('true')
				}
			}
		}
		Projects(projectPrefix: '') {
			Project(description: "Generated by script on ${new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())}", name: "${project}", overwrite: 'true', type:'Repository') {
				ProjectProperties {
					Property(name:'projectLocking', 'false')
					Property(name:'concurrentDeployment', 'false')
					Property(name:'ignoreMissingDependencies', 'true')
					Property(name:'isTransactionalDeployment', 'true')
				}
				mkp.yieldUnescaped createISOnlyDeploymentSets(splitDeploymentSets)
				mkp.yieldUnescaped createDeploymentSets(splitDeploymentSets)

				mkp.yieldUnescaped createISOnlyDeploymentMapSetMappingAndDeploymentCandidate(splitDeploymentSets)
				mkp.yieldUnescaped createDeploymentMapSetMappingAndDeploymentCandidate(splitDeploymentSets)
			}
		}
	}
}
println XmlUtil.serialize( nxml, writer )

writer.close()
println "Successfully create Project Automator Template '${outFile.getAbsolutePath()}'"

