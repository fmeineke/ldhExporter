<xsl:stylesheet version="2.0" 
    xmlns:x="http://www.w3.org/2005/xpath-functions"
	xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	exclude-result-prefixes="xs math x">
<!-- 
<xsl:mode on-no-match="shallow-skip"/>
-->
<xsl:output indent="yes" omit-xml-declaration="no" />

<xsl:template match="/">
	<Resource>
		<xsl:apply-templates/>
	</Resource>
</xsl:template>

<xsl:template match="x:*[starts-with(@key,'investigation') or @key='descxription' or @key='title']" priority="2"/>

<xsl:template match="x:array[@key]" priority="1">
	<xsl:variable name="n1" select="x:tokenize(@key,'\.')[last()]"/>	
	
	<xsl:for-each select="*">
		<xsl:element name="{$n1}">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:for-each>
</xsl:template>

<xsl:template match="x:string[@key]" priority="1">
	<xsl:variable name="n" select="x:tokenize(@key,'\.')[last()]"/>	
	<xsl:if test="normalize-space(.)">
		<xsl:element name="{$n}">
			<xsl:value-of select="."/>
		</xsl:element>
	</xsl:if>
</xsl:template>

<xsl:template match="x:*[@key]">
	<xsl:variable name="n" select="x:tokenize(@key,'\.')[last()]"/>	
	<xsl:if test="normalize-space(.)">
		<xsl:element name="{$n}">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:if>
</xsl:template>


</xsl:stylesheet>
