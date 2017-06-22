<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
	xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions">
	<xsl:output method="xml" omit-xml-declaration="no" indent="yes" />
	<xsl:template match="/">
		<testsuites>
			<xsl:for-each select="CodeCheckConfiguration/checks/check">
				<xsl:variable name="currentCheck" select="." />
				<xsl:if test="results/Summary/total/text()!='0'">
					<testsuite errors="0" hostname="WIN-QRM55D89EU2"
						timestamp="2017-05-26T06:53:33" package="test" id="1">
						<xsl:attribute name="failures">
						<xsl:value-of select="results/Summary/fail/text()" />
					</xsl:attribute>
						<xsl:attribute name="tests">
						<xsl:value-of select="results/Summary/total/text()" />
					</xsl:attribute>
						<xsl:attribute name="time">
						<xsl:value-of select="results/Summary/executeTime/text()" />
					</xsl:attribute>
						<xsl:attribute name="name"><xsl:value-of
							select="@name" /> [<xsl:value-of select="@id" />]</xsl:attribute>
						<!-- -->
						<properties>
							<xsl:for-each
								select="CodeCheckConfiguration/globalParameters/globalParameter_x">
								<property>
									<xsl:attribute name="name"><xsl:value-of
										select="@name" /></xsl:attribute>
									<xsl:attribute name="value"><xsl:value-of
										select="value/text()" /></xsl:attribute>
								</property>
							</xsl:for-each>
						</properties>
						<xsl:for-each select="results/Report/Test">
							<testcase time="0.0">
								<xsl:attribute name="classname"><xsl:value-of
									select="Service/text()" /></xsl:attribute>
								<xsl:attribute name="name"><xsl:value-of
									select="$currentCheck/@name" /></xsl:attribute>
								<xsl:if test="@passed!='true'">
									<failure>
										<xsl:attribute name="message"><xsl:value-of
											select="$currentCheck/description/text()" /></xsl:attribute>
										<xsl:attribute name="type"><xsl:value-of
											select="$currentCheck/implementation/class/text()" /></xsl:attribute>
										<xsl:value-of select="$currentCheck/description/text()" />
									</failure>
								</xsl:if>
							</testcase>
							<!-- Successful test <Test passed="true" check-id="FQ3.1" ragStatus="1"> 
								<Service>Fibonachi.services:getFibunachiNumber</Service> <Value>count(//MAPINVOKE[starts-with(@SERVICE,'wm.vcs')])=0</Value> 
								</Test> -->
						</xsl:for-each>
					</testsuite>
				</xsl:if>
			</xsl:for-each>
		</testsuites>
	</xsl:template>
</xsl:stylesheet>