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


def project = opts.p
def repo = opts.r
def outputFile = opts.f
def target = opts.t
def deployerHostPort = opts.d
def deployerUser = opts.u
def deployerPassword = opts.s
def environments = opts.e

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
println "\t- Environments definition: '${environments}'" 
	

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
				println "did find ${config.IntegrationServers.size()} IntegrationServers in config ${environments} for target ${target}"
		            	IS {
					config.IntegrationServers.each { name, isConfig ->
						isalias(name: "${name}") {
			                            host(isConfig.host)
                        			    port(isConfig.port)
			                            user(isConfig.username)
                        			    pwd(isConfig.pwd)
			                            useSSL(isConfig.useSSL)
                        			    version(isConfig.version)
			                            installDeployerResource(isConfig.installDeployerResource)
                        			    Test(isConfig.test)
		                   	 	}
					}
				}
			} else {
				println "did not find any IntegrationServer config in ${environments} for target ${target}"
			}
			if(config.ProcessModels.size() > 0 ) {
				ProcessModel {
					config.ProcessModels.each { name, bpmConfig ->
						pmalias(name: "${name}") {
							host(bpmConfig.host)
							port(bpmConfig.port)
							user(bpmConfig.username)
							pwd(bpmConfig.pwd)
							useSSL(bpmConfig.useSSL)
							version(bpmConfig.version)
							Test(bpmConfig.test)
						}
					}
				}
			}
			if(config.MWS.size() > 0 ) {
				MWS {
					config.MWS.each { name, mwsConfig ->
						mwsalias(name: "${name}") {
	                            host(mwsConfig.host)
	                            port(mwsConfig.port)
	                            user(mwsConfig.username)
	                            pwd(mwsConfig.pwd)
	                            useSSL(mwsConfig.useSSL)
	                            version(mwsConfig.version)
	                            excludeCoreTaskEngineDependencies(mwsConfig.excludeCoreTaskEngineDependencies)
	                            cacheTimeOut(mwsConfig.cacheTimeOut)
	                            includeSecurityDependencies(mwsConfig.includeSecurityDependencies)
	                            rootFolderAliases(mwsConfig.rootFolderAliases)
	                            maximumFolderObjectCount(mwsConfig.maximumFolderObjectCount)
	                            enableAddtionalLogging(mwsConfig.enableAddtionalLogging)
	                            maxFolderDepth(mwsConfig.maxFolderDepth)
	                            Test(mwsConfig.test)
						}
					}
				}
			}
			Repository {
				repalias(name: "Repo_${project}") {
						type("FlatFile")
						urlOrDirectory("${repo}")
						Test('true')
				}
			}
        }
        Projects(projectPrefix: ''){
                Project(description: '', name: "${project}", overwrite: 'true', type:'Repository') {
                        ProjectProperties {
                                Property(name:'projectLocking', 'false')
                                Property(name:'concurrentDeployment', 'false')
                                Property(name:'ignoreMissingDependencies', 'true')
                                Property(name:'isTransactionalDeployment', 'true')
                        }
                        DeploymentSet (autoResolve:'ignore', description: '', name: 'DeploymentSet', srcAlias:"Repo_${project}") {
							
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
println "finished creating project automator template '${outputFile}'"

