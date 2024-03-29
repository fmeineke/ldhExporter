<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    exclude-result-prefixes="fn" >

    <xsl:output indent="yes" omit-xml-declaration="no" />

    <xsl:template match="/">
        <xsl:copy-of select="fn:json-to-xml(.)" />
    </xsl:template>
</xsl:stylesheet>
