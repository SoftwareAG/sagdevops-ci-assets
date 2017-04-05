<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	
	<xsl:output method="xml" encoding="utf-8" indent="yes"/>
	
	<xsl:param name="config.deployer.deployerHost"/>
	<xsl:param name="config.deployer.deployerPort"/>
	<xsl:param name="config.deployer.deployerUsername"/>
	<xsl:param name="config.deployer.deployerPassword"/>
	
	<xsl:param name="config.test.testISHost"/>
	<xsl:param name="config.test.testISPort"/>
	<xsl:param name="config.test.testISUsername"/>
	<xsl:param name="config.test.testISPassword"/>
	
	<xsl:param name="repoName"/>
	<xsl:param name="repoPath"/>
	<xsl:param name="projectName"/>
		
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="DeployerSpec/DeployerServer">
		<DeployerServer>
			<host><xsl:value-of select="$config.deployer.deployerHost"/>:<xsl:value-of select="$config.deployer.deployerPort"/></host>
			<user><xsl:value-of select="$config.deployer.deployerUsername"/></user>
			<pwd><xsl:value-of select="$config.deployer.deployerPassword"/></pwd>
		</DeployerServer>
	</xsl:template>

	<xsl:template match="DeployerSpec/Environment">
	    <Environment>
			<IS>
				<isalias name="testServer">
					<host><xsl:value-of select="config.test.testISHost"/></host>
					<port><xsl:value-of select="$config.test.testISPort"/></port>
					<user><xsl:value-of select="$config.test.testISUsername"/></user>
					<pwd><xsl:value-of select="$config.test.testISPassword"/></pwd>
					<useSSL>false</useSSL>
					<installDeployerResource>true</installDeployerResource>
					<Test>true</Test>
				</isalias>
			</IS>
			<xsl:apply-templates select="@* | *" />
		</Environment>
	</xsl:template>

	
	<xsl:template match="DeployerSpec/Environment/Repository">
		<Repository>
			<xsl:apply-templates select="@* | *" />
			
			<repalias>
			<xsl:attribute name="name"><xsl:value-of select="$repoName"/></xsl:attribute>
				<type>FlatFile</type>
				<urlOrDirectory><xsl:value-of select="$repoPath"/></urlOrDirectory>
				<Test>true</Test>
			</repalias>
	
		</Repository>
	</xsl:template>
	
	
	<xsl:template match="DeployerSpec/Projects">
		<Projects>
			<xsl:apply-templates select="@* | *" />
			
			<Project description="" ignoreMissingDependencies="true" overwrite="true" type="Repository">
			<xsl:attribute name="name"><xsl:value-of select="$projectName"/></xsl:attribute>			

				<DeploymentSet autoResolve="full" description="" name="myDeploymentSet">
				<xsl:attribute name="srcAlias"><xsl:value-of select="$repoName"/></xsl:attribute>

					<Composite displayName="" name="*" type="*">
						<xsl:attribute name="srcAlias"><xsl:value-of select="$repoName"/></xsl:attribute>
                                        </Composite> 
				</DeploymentSet>
				
				<DeploymentMap description="" name="myDeploymentMap"/>			
				<MapSetMapping mapName="myDeploymentMap" setName="myDeploymentSet">								
					<alias type="IS">testServer</alias>
				</MapSetMapping>	
				<DeploymentCandidate description="" mapName="myDeploymentMap" name="myDeployment"/>
			</Project>

		</Projects>		
	</xsl:template>

</xsl:stylesheet>
