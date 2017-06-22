<?xml version="1.0" encoding="UTF-8"?><Root>
  <DeploementSet assetCompositeName="isconfiguration" deploymentSetName="DeploymentSet" serverAliasName="henningGitHubPipeline_1.0.0.31_fbrRepo" targetServerName="is_node1" targetServerType="IS">
    <Property propertyName="suspendTasksDuringDeploy" propertyValue="none"/>
    <Property propertyName="activateTasksAfterDeploy" propertyValue="none"/>
    <Property propertyName="enablePortsAfterDeploy" propertyValue="none"/>
    <Property propertyName="reloadCacheManagersAfterDeployment" propertyValue="none"/>
    <Component name="Port.SimpleTestPackage.FilePollingListener.c.\tmp\citrus\csv\import" type="isport">
      <Property propertyName="pkg" propertyValue="SimpleTestPackage"/>
      <Property propertyName="enable" propertyValue="false"/>
      <Property propertyName="hostAccessMode" propertyValue="global"/>
      <Property propertyName="hostList" propertyValue=""/>
      <Property propertyName="monitorDir" propertyValue="/my/monitoring/dir/import"/>
      <Property propertyName="workDir" propertyValue=""/>
      <Property propertyName="completionDir" propertyValue=""/>
      <Property propertyName="errorDir" propertyValue=""/>
      <Property propertyName="clusterEnabled" propertyValue="no"/>
      <Property propertyName="runUser" propertyValue="Administrator"/>
      <Property propertyName="NFSDirectories" propertyValue="no"/>
    </Component>
    <Component name="Port.SimpleTestPackage.Regular.5111" type="isport">
      <Property propertyName="port" propertyValue="5112"/>
      <Property propertyName="bindAddress" propertyValue=""/>
      <Property propertyName="pkg" propertyValue="SimpleTestPackage"/>
      <Property propertyName="enable" propertyValue="false"/>
      <Property propertyName="hostAccessMode" propertyValue="global"/>
      <Property propertyName="hostList" propertyValue=""/>
    </Component>
  </DeploementSet>
</Root>
