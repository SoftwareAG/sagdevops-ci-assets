<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	
	<xsl:output method="xml" encoding="utf-8" />
	
	<xsl:param name="paramPackageName"/>
	
	
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="DeployerSpec/Projects/Project/DeploymentSet">
		<DeploymentSet>
				<xsl:apply-templates select="@* | *" />
				<Composite srcAlias="RepoSource" type="IS">
					<xsl:attribute name="displayName">Package/<xsl:value-of select="$paramPackageName"/></xsl:attribute>
					<xsl:attribute name="name"><xsl:value-of select="$paramPackageName"/></xsl:attribute>
				</Composite>
		</DeploymentSet>
	</xsl:template>

</xsl:stylesheet>