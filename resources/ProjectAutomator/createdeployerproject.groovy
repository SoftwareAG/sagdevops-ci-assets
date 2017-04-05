#!/opt/bpa/groovy-2.4.3/bin/groovy

import groovy.xml.MarkupBuilder

def cli = new CliBuilder (usage:'createdeployerproject.groovy -r REPOSITORY_PATH -f OUTPUTFILE -p PROJECT -t TARGETSUFFIX')
cli.with {
 h longOpt:'help', 'Usage information'
 r longOpt:'repository',argName:'repository', args:1, type:Number.class,'Default is 8080'
 f longOpt:'outputfile', argName:'outputfile', args:1, 'Default is .'
 p longOpt:'project', argName:'project', args:1, 'Default is .'
 t longOpt:'targetsuffix', argName:'targetsuffix', args:1, 'Default is TRUNK.'
 o longOpt:'targethost', argName:'targethost', args:1, 'Default is localhost.'

}
def opts = cli.parse(args)
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
assert opts.o

def project = opts.p
def repo = opts.r
def outputFile = opts.f
def targetSuffix = opts.t
def targetHost = opts.o

def writer = new FileWriter(new File(outputFile))
def xml = new MarkupBuilder(writer)
def packages = []
def bpms = []
def mws = []
def tn = []

def config = new ConfigSlurper(targetSuffix).parse(new File('E:/webmethods9.9/common/AssetBuildEnvironment/master_build/environments.groovy').toURL())

println config.prettyPrint()

// scan given repo path for ACDL files
new File(repo).eachDirRecurse() { dir ->
    dir.eachFileMatch(~/.*.acdl/) { file ->
        def asset_composite = new XmlParser().parse(file)

        println file.getPath()
        println asset_composite.'@name'
        println asset_composite.'@displayName'

        // need to get it by index because element name contains a dot '.'
        type = asset_composite.children().get(0).'@type'

        displayName = asset_composite.'@displayName'
        name = asset_composite.'@name'

        if(type == 'bpmprocess') {
            bpms.add([name: name, displayName: displayName])
        } else if (type == 'ispackage') {
            packages.add([name: name, displayName: displayName])
        } else if (type == 'war') {
            mws.add([name: name, displayName: displayName])
        } else if (type == 'pdp') {
			mws.add([name: name, displayName: displayName])
        } else if (type == 'cdp') {
			mws.add([name: name, displayName: displayName])
		} else {
			// TN handling
			targetnamespace = asset_composite.'@targetNamespace'
			if (targetnamespace == 'http://namespaces.softwareag.com/webMethods/TN') {
				tn.add([name: name, displayName: displayName])
			}
			
		
		}
        // extend for more

    }
}
xml.setDoubleQuotes(true)
// creation of deployerspec
xml.DeployerSpec(exitOnError:'true', sourceType:'Repository') {
        DeployerServer {
                host("${targetHost}:5555")
                user('Administrator')
                pwd('manage')
        }
        Environment {
                Repository {
                        repalias(name: "${project}") {
                                type("FlatFile")
                                urlOrDirectory("E:/abe_repositories/${project}")
                                Test('False')
                        }

                }
                IS {
                        isalias(name: "IS_${targetSuffix}") {
                                host(config.integrationserver.host)
                                port(config.integrationserver.port)
                                user(config.integrationserver.username)
                                pwd(config.integrationserver.pwd)
                                useSSL(config.integrationserver.useSSL)
                                version(config.integrationserver.version)
                                installDeployerResource(config.integrationserver.installDeployerResource)
                                Test(config.integrationserver.Test)
                                executeACL(config.integrationserver.executeACL)


                        }

                }
                ProcessModel {
                        pmalias(name: "BPM_${targetSuffix}") {
                                host(config.pm.host)
                                port(config.pm.port)
                                user(config.pm.username)
                                pwd(config.pm.pwd)
                                useSSL(config.pm.useSSL)
                                version(config.pm.version)
                                Test(config.pm.Test)
                              }
                }
                MWS {
                        mwsalias(name: "MWS_${targetSuffix}") {
                            host(config.mws.host)
                            port(config.mws.port)
                            user(config.mws.username)
                            pwd(config.mws.pwd)
                            useSSL(config.mws.useSSL)
                            version(config.mws.version)
                            excludeCoreTaskEngineDependencies(config.mws.excludeCoreTaskEngineDependencies)
                            cacheTimeOut(config.mws.cacheTimeOut)
                            includeSecurityDependencies(config.mws.includeSecurityDependencies)
                            rootFolderAliases(config.mws.rootFolderAliases)
                            maximumFolderObjectCount(config.mws.maximumFolderObjectCount)
                            enableAddtionalLogging(config.mws.enableAddtionalLogging)
                            maxFolderDepth(config.mws.maxFolderDepth)
                            Test(config.mws.Test)
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
                        DeploymentSet (autoResolve:'full', description: '', name: 'DeploymentSet', srcAlias:"${project}") {
						
								if (!packages.empty) {								
									packages.each() {
											Composite(name:"${it.name}", srcAlias:"${project}", type:'IS')
									}
								}
								
								if (!bpms.empty) {
									bpms.each() {
											Composite(name:"${it.name}", srcAlias:"${project}", type:'BPM')
									}
								}
								if (!mws.empty) {
									mws.each() {
											Composite(name:"${it.name}", srcAlias:"${project}", type:'MWS')
									}			
								}	
								
								if (!tn.empty) {								
									tn.each() {
											Composite(name:"${it.name}", srcAlias:"${project}", type:'TN')
									}
								}
								
                                // extend for more composites



                        }
                        DeploymentMap(description: '', name:'DeploymentMap')
                        MapSetMapping(mapName: 'DeploymentMap', setName: 'DeploymentSet') {
							if (!packages.empty || !tn.empty) {
                                alias(type:'IS', "IS_${targetSuffix}")
							}
							if (!bpms.empty) {
                                alias(type:'BPM', "BPM_${targetSuffix}")
							}
							if (!mws.empty) {
                                alias(type:'MWS', "MWS_${targetSuffix}")
							}
                        }
                        DeploymentCandidate(description: 'Deployment ${project}', mapName: 'DeploymentMap', name:'Deployment')
                }


        }

}


