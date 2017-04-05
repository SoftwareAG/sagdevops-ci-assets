<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" />

	<xsl:param name="featureSetFileName" />
	<xsl:param name="repoName" />
	<xsl:param name="featureset" select="document($featureSetFileName)" />

	<xsl:variable name="composites"
		select="$featureset/FeatureDefinition/featureset/Composite" />
	<xsl:variable name="components"
		select="$featureset/FeatureDefinition/featureset/Component" />

	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="Composite/@srcAlias">
		<xsl:attribute name="srcAlias"><xsl:value-of select="$repoName" /></xsl:attribute>
	</xsl:template>

	<xsl:template match="Component/@srcAlias">
		<xsl:attribute name="srcAlias"><xsl:value-of select="$repoName" /></xsl:attribute>
	</xsl:template>

	<xsl:template match="DeploymentSet">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()" />
			<xsl:apply-templates select="$composites" />
			<xsl:apply-templates select="$components" />
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>