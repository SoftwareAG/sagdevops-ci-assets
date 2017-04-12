#!/opt/bpa/groovy-2.4.3/bin/groovy
package com.softwaerag.gcs.wx.bdas.projectAutomator

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

import groovy.xml.MarkupBuilder

def cli = new CliBuilder (usage:'createdeployerproject.groovy -r REPOSITORY_PATH -f OUTPUTFILE -p PROJECT -t TARGETSUFFIX')
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

}
def opts = cli.parse(args)
org.codehaus.groovy.ant.Groovy
if(!opts) return
if(opts.help) {
  cli.usage()
  return
}


assert opts
assert opts.r
assert opts.p
assert opts.f
assert opts.t
assert opts.d
assert opts.u
assert opts.s
assert opts.e
assert opts.repoName


def project = opts.p
def repo = opts.r
def outputFile = opts.f
def target = opts.t
def deployerHostPort = opts.d
def deployerUser = opts.u
def deployerPassword = opts.s
def environments = opts.e
def repoName = opts.repoName

def outFile = new File(outputFile)
//def writer = new FileWriter(outFile)
PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
def xml = new MarkupBuilder(writer)
def packages = []
def bpms = []
def mws = []
def tn = []

println "Creating Project Automator templates with the following properties:"
println "\t- Project: '${project}'" 
println "\t- Repository: '${repo}'" 
println "\t- Output File: '${outputFile}'" 
println "\t- Target: '${target}'" 
println "\t- Deployer host: '${deployerHostPort}'" 
println "\t- Deployer user: '${deployerUser}'" 
println "\t- Deployer passsword: *****" 
println "\t- Repository name: '${repoName}'" 
println "\t- Environments definition: '${environments}'" 

println "${opts.arguments()}"

def configSlurper = new ConfigSlurper(target)
configSlurper.classLoader = this.class.getClassLoader()
def config = configSlurper.parse(new File(environments).toURL())
//def config = configSlurper.parse(configText)

xml.setDoubleQuotes(true)
// creation of deployerspec
xml.DeployerSpec(exitOnError:'true', sourceType:'Repository') {
        DeployerServer {
                host("${deployerHostPort}")
                user("${deployerUser}")
                pwd("${deployerPassword}")
        }
        Environment {
			if(config.IntegrationServers.size() > 0 ) {
            	IS {
					config.IntegrationServers.each { name, isConfig ->
						def integ = isConfig + config.IntegrationServer.defaults;
						isalias(name: "${name}") {
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
						def bpm = bpmConfig + config.ProcessModel.defaults;
						pmalias(name: "${name}") {
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
						def mwsConfigMerged = mwsConfig + config.MyWebmethodsServer.defaults;
						mwsalias(name: "${name}") {
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
        Projects(projectPrefix: ''){
                Project(description: "Generated by script on ${new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())}", name: "${project}", overwrite: 'true', type:'Repository') {
                        ProjectProperties {
                                Property(name:'projectLocking', 'false')
                                Property(name:'concurrentDeployment', 'false')
                                Property(name:'ignoreMissingDependencies', 'true')
                                Property(name:'isTransactionalDeployment', 'true')
                        }
                        DeploymentSet (autoResolve:'ignore', description: '', name: 'DeploymentSet', srcAlias:"${repoName}") {
							
						}
                        DeploymentMap(description: '', name:'DeploymentMap')
                        MapSetMapping(mapName: 'DeploymentMap', setName: 'DeploymentSet') {
							if(config.IntegrationServers.size() > 0 ) {
								config.IntegrationServers.keySet().each {
                                	alias(type:'IS', "${it}")
                                }
							}
							if(config.ProcessModels.size() > 0 ) {
								config.ProcessModels.keySet().each {
									alias(type:'BPM', "${it}") 
								}
							}
							if(config.MWS.size() > 0 ) {
								config.MWS.keySet().each {
									alias(type:'MWS', "${it}")
								}
							}
                        }
                        DeploymentCandidate(description: 'Deployment ${project}', mapName: 'DeploymentMap', name:'Deployment')
                }
        }
}
writer.close()


