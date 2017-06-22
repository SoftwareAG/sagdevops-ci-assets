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
packageArr.each { println it }

packageArr.each { packageName ->
	def packageDir = new File(packagesDir, packageName)
	assert packageDir.exists() : "Package ${packageName} does not exist in directory ${packagesDir.getAbsolutePath()}."
	def nsDir = new File(packageDir, "ns")
	println ("Searching for all triggers in package " + packageName)
	nsDir.traverse(
			type : FileType.FILES,
			nameFilter: ~/node.ndf.triggerBak/,
			preDir: { if (it.name == '.svn' || it.name == '.git') return SKIP_SUBTREE },
			) { nodeNdf ->
				def origFile = new File(nodeNdf.getParent(), "node.ndf")
				origFile << nodeNdf.text
			}

}


