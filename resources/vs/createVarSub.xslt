<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" />

	<xsl:param name="varSubFileName" />
	<xsl:param name="varSubs" select="document($varSubFileName)" />
	<xsl:variable name="properties" select="$varSubs/VarSub/Property" />
	<xsl:variable name="components" select="$varSubs/VarSub/Component" />

	<xsl:param name="targetServerName" />
	<xsl:param name="packageName" />
	<xsl:param name="serverAliasName" />


	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()" />
		</xsl:copy>
	</xsl:template>

	<xsl:template name="DeploymentSet">
		<DeploementSet>
			<xsl:attribute name="assetCompositeName"><xsl:value-of select="$packageName" /></xsl:attribute>
			<xsl:attribute name="deploymentSetName">myDeploymentSet</xsl:attribute>
			<xsl:attribute name="serverAliasName"><xsl:value-of select="$serverAliasName" /></xsl:attribute>
			<xsl:attribute name="targetServerName"><xsl:value-of select="$targetServerName" /></xsl:attribute>
			<xsl:attribute name="targetServerType">IS</xsl:attribute>
			<xsl:apply-templates select="$properties" />
			<xsl:apply-templates select="$components" />
		</DeploementSet>
	</xsl:template>

	<xsl:template match="Root_Template">
		<Root>
			<xsl:call-template name="DeploymentSet"></xsl:call-template>
		</Root>
	</xsl:template>

	<xsl:template match="Root">
		<Root>
			<xsl:apply-templates select="@* | node()" />
			<xsl:call-template name="DeploymentSet"></xsl:call-template>
		</Root>
	</xsl:template>

</xsl:stylesheet>