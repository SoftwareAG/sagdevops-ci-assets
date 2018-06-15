# sagdevops-ci-assets
Software AG DevOps library to support assets CI (continuous integration) with webMethods 10.2
Currently customized to work with Order to cash project located at https://github.com/vidb/WM_O2C.git


## Description



## Set-up

### webMethods Installation
Prepare your webMethods installation - your build server can contain only a plain IntegrationServer with Deployer. Keep the server plain - there is no need for designer or database connection.
Your test server can be more complex as CI will execute unit and integration tests against it. The build and the test server must reach each other over http so that the deployment and the testing can be performed.
