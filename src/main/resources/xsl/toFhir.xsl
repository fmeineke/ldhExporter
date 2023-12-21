<xsl:stylesheet version="3.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2005/xpath-functions"
    xmlns="http://hl7.org/fhir" 
    exclude-result-prefixes="xs">

    <xsl:output indent="yes" omit-xml-declaration="no" />
    <xsl:mode on-no-match="shallow-skip" />

    <xsl:template match="/">
        <ResearchStudy>
            <xsl:apply-templates />
        </ResearchStudy>
    </xsl:template>

    <xsl:template match="titles/text">
        <title>
            <xsl:value-of select="text()" />
        </title>
    </xsl:template>

    <xsl:template match="descriptions/text">
        <description>
            <xsl:value-of select="text()" />
        </description>
    </xsl:template>

    <xsl:template match="design/interventional/phase">
        <phase>
            <xsl:value-of select="text()" />
        </phase>
    </xsl:template>

</xsl:stylesheet>
