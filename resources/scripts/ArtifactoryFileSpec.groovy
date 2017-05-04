#!/opt/bpa/groovy-2.4.3/bin/groovy
package com.softwaerag.gcs.wx.bdas.build.artifactory


def cli = new CliBuilder (usage:'artifactoryFileSpec.groovy -outputDir OUTPUT_DIR')
cli.with {
 h longOpt:'help', 'Usage information'
 outputDir longOpt:'outputDir',argName:'outputDir', args:1, 'Path to the directory where to store the artifactory json upload file spec'
 fileSpecName longOpt:'fileSpecName',argName:'fileSpecName', args:1, 'Name of the file spec json file'
 pathToFbrZip longOpt:'pathToFbrZip',argName:'pathToFbrZip', args:1, 'Path to the zipped FBR, only mandatory for type="upload"'
 artifactoryRepository longOpt:'artifactoryRepository',argName:'artifactoryRepository', args:1, 'Name of the Artifactory Repository'
 org longOpt:'org',argName:'org', args:1, 'Artifactory Organization. Use dot-notation for Ivy repo, e.g. com.sag.gcs'
 moduleName longOpt:'moduleName',argName:'moduleName', args:1, 'Artifactory Module name'
 baseRevision longOpt:'baseRevision',argName:'baseRevision', args:1, 'Artifactory base revision, e.g. version_number.build_number'
 type longOpt:'type',argName:'type', args:1, 'Type of file spec, wither "upload" or "download"'

}
def opts = cli.parse(args)
org.codehaus.groovy.ant.Groovy
if(!opts) return
if(opts.help) {
  cli.usage()
  return
}

assert opts
assert opts.outputDir
assert opts.fileSpecName
assert opts.pathToFbrZip || opts.type == "download"
assert opts.org
assert opts.moduleName
assert opts.baseRevision
assert opts.artifactoryRepository
assert opts.type == "download" || opts.type == "upload"

def outputDir = opts.outputDir
def pathToFbrZip = opts.pathToFbrZip
def fileSpecName = opts.fileSpecName
def org = opts.org
def moduleName = opts.moduleName
def baseRevision = opts.baseRevision
def artifactoryRepository = opts.artifactoryRepository
def type = opts.type

def outDir = new File(outputDir)
outDir.mkdirs()
assert outDir.exists() 
println "Creating Artifactory File Spec with the folllwing properties: "
println "\t outputDir: '${outputDir}'"
println "\t pathToFbrZip: '${pathToFbrZip}'"
println "\t fileSpecName: '${fileSpecName}'"
println "\t org: '${org}'"
println "\t moduleName: '${moduleName}'"
println "\t baseRevision: '${baseRevision}'"
println "\t artifactoryRepository: '${artifactoryRepository}'"
println "\t type: '${type}'"

def fileList
def repoPath = "${artifactoryRepository}/${org}/${moduleName}/${baseRevision}/fbrs/${moduleName}-${baseRevision}.zip"
if( type == "upload" ) {
	fileList = [
		[ pattern : { "${pathToFbrZip}" }, target : { "${repoPath}" }, props : { 'type=zip;status=ready' }, flat : { "true" }   ]
	]
} else if ( type == "download") {
	fileList = [
		[ pattern : { "${repoPath}" }, target : { "${outputDir}" }, props : { 'type=zip;status=ready' }, flat : { "true" }  ]
	]
}



jsonBuilder = new groovy.json.JsonBuilder()

jsonBuilder {
	files fileList.collect {
		[
			pattern: it.pattern(),
			target: it.target(),
			props: it.props(),
			flat: it.flat()
		]
	}
}
println "Generated File Spec:"
println jsonBuilder.toPrettyString()


File fileSpecFile = new File(outDir, "${fileSpecName}")
fileSpecFile.write(jsonBuilder.toPrettyString())
println "Successfully create file spec: '${fileSpecFile.getAbsolutePath()}'"

