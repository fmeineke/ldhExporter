<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    exclude-result-prefixes="fn" >

    <xsl:output method="text" />

    <xsl:template match="/">
        <xsl:value-of select="fn:xml-to-json(.)" />
    </xsl:template>
</xsl:stylesheet>