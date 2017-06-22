# sagdevops-ci-assets
Software AG DevOps library to support assets CI (continuous integration) with webMethods 9.x and 10.0. Works together with [webMethods sample project layout](https://github.com/SoftwareAG/webmethods-sample-project-layout)


## Description
sagdevops-ci-assets is a library that easily enables CI for your webMethods projects. You can setup your infrastructure in minutes and then deploy flowlessly on your test service while also checking the quality by running all tests uploaded your version control.

Note: the scripts in this repository are not project specific, they are generic and can be used by multiple projects (in parallel). For this tutorial, the project specific scripts and source code assets like IS packages are stored in a separate repository ([https://github.com/SoftwareAG/webmethods-sample-project-layout](https://github.com/SoftwareAG/webmethods-sample-project-layout)) and are "referenced" by the pipeline setup described below.


## Set-up

### webMethods Installation
Prepare your webMethods installation - your build server can contain only a plain IntegrationServer with Deployer. Keep the server plain - there is no need for designer or database connection.

Your test server can be more complex as CI will execute unit and integration tests against it. The build and the test server must reach each other over http so that the deployment and the testing can be performed.

### CI Library
Download the sagdevops-ci-assets respository on your build server by

```
git clone https://github.com/SoftwareAG/sagdevops-ci-assets.git
```

Edit the _System.properties_ to correspond to your infrastructure:

* __config.deployer.*__: Configuration parameters which specify your Deployer installation and Deployer server
* __config.tmpdir__: Points to a tempory directory where assets are stored for the deployment process. **Note**: take care to clean this directory up regularly!
* __config.build.buildStorageDir__: Where to store the file based repositories created by the Asset Build Environment
* __config.deployer.projectNamePrefix__: Defines the Deployer Project Name prefix. Can either be static, e.g. "BDA", or it can be dynamic, e.g. "Jenkins_${env.BUILD_NUMBE}"
* __config.libs.resolve__: 
	* Set to "**remote**" if dependent jars should be downloaded from remote maven repositories with Apache Ivy. See "[resources/ivy/ivy-remote.xml](resources/ivy/ivy-remote.xml)" for list of jars. See "[resources/ivy/ivysettings-remote.xml](resources/ivy/ivysettings-remote.xml)" for list of repositories from which jars are downloaded. **Note**: Software AG jars are referenced with a filesystem resolver pointing to the respective local installation.
	* Set to "**local**" if no internet connection is available and place necessary jars (see "[resources/ivy/ivy-remote.xml](resources/ivy/ivy-remote.xml)" for list) into folder "**lib/ext**". 


### Build/CI Environment

Setup your build server in the following way:
 
* Add the default java to your PATH variable. Use the JDK that comes with your webMethods installation ${SAG_HOME}/jvm/jvm/bin/java
* Install Jenkins CI Server v2 ([https://jenkins.io/](https://jenkins.io/)) and run it with the same user that run your webMethods processes. This webMethods CI framework can work also with other CI servers, but for the reference implementation we've chosen Jenkins 2 with native Pipeline support.

Setup your Jenkins server in the following way:

* In "Jenkins > Manage Jenkins > Configure System > EnvironmentVariables" define the following environment variables:
	1. __SAG_CI_HOME__: path_to_the_sagdevops-ci-assets on the local file system. I.e., if you have cloned the sagdevops-ci-assets respository to the directory "/home/user/sag/sagdevops-ci-assets", the set "SAG_CI_HOME=/home/user/sag/sagdevops-ci-assets" in Jenkins.
	2. __SAG_HOME__: path_to_your_local_webmethods_installation

Note: Use slash "/" as path separator. Example: "C:/SoftwareAG", instead of "C:\SoftwareAG".

### Jenkins Pipeline Job
In Jenkins, create a new item from type pipeline. Give it a **unique name** as we use the job name as identifier further down the process. Scroll down the page to the pipeline definition and choose _Pipeline definition from SCM_. Choose git as system and give the url of the webmethods-sample-project-layout [https://github.com/SoftwareAG/webmethods-sample-project-layout.git](https://github.com/SoftwareAG/webmethods-sample-project-layout.git).

This sample project contains two pre-created pipeline definitions - Jenkinsfile.win and Jenkinsfile.unix that run on the respective operating systems. Type in the correct file in respect of you build server OS.

Those pipeline definition are orchestrating all steps around the build, deploy and the test on your server. If the all environment variables are set correctly you should not change anything here.


## How it works
After your pipeline job is set-up, trigger it. It will download the pipeline description automatically, then checkout the sources, build the core, deploy the code and run tests. 
Whenever a developer checks in new IS packages and Tests those will be automatically deployed and all new tests will be executed. For this to work, the structure defined here  _https://github.com/SoftwareAG/webmethods-sample-project-layout.git_ has followed.

## Notice
The wM Test Suite tests will have to be places in a directory a *setup* directory inside the test project, so that it can be picked up by the test executor.
_____________
Contact us at [TECHcommunity](mailto:technologycommunity@softwareag.com?subject=Github/SoftwareAG) if you have any questions.

## Test Execution
The wM Test Suite tests will have to be places in a directory a *setup* directory inside the test project, so that it can be picked up by the test executor. Please see [https://github.com/SoftwareAG/webmethods-sample-project-layout](https://github.com/SoftwareAG/webmethods-sample-project-layout) for details.









