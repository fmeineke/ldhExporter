<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xpath-default-namespace="http://www.w3.org/2005/xpath-functions"
    xmlns="http://www.w3.org/2005/xpath-functions"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    exclude-result-prefixes="xs math fn" >

    <xsl:output indent="yes" omit-xml-declaration="no" />
    <!-- 
    <xsl:mode on-no-match="shallow-skip" />
    -->
<xsl:variable name="r1" select="//map[fn:starts-with(@key,'Resource_classification')]/@key"/>
<xsl:variable name="resource" select="concat('_',fn:tokenize($r1,'_')[last()])"/>

<xsl:template match="/">
<map>
    <map>
        <xsl:attribute name="key" select="'resource'"/>
        <xsl:apply-templates
            select="map/map/string[@key='id']" />
            <!-- 
        <xsl:apply-templates
            select="map/map/map[@key='attributes']/string" />
             -->
        <xsl:apply-templates
            select="map/map/map/map[@key='extended_attributes']/map " />
         
    </map>
</map>
</xsl:template>

<xsl:template match="map[@key='attribute_map']" priority="2">
     <array key="ids">
        <map>
            <xsl:variable name="self" select="//string[@key='self']/text()"/>
            <xsl:variable name="base_url" select="//string[@key='base_url']/text()"/>
            <string key="identifier"><xsl:value-of select="concat($base_url,$self)"/></string>
            <string key="scheme">URL</string>
            <string key="relationType">A is identical to B</string>
        </map>
    </array>
    <xsl:apply-templates/>
</xsl:template>

<!-- convert key (e.g. "Resource_classification_type_Project" to "type" -->
<xsl:template name="setKey">
    <xsl:if test="@key">
        <xsl:attribute name="key"
            select="fn:tokenize(fn:replace(@key,$resource,''),'_')[last()]" />
    </xsl:if>
</xsl:template>

<xsl:template match="*[normalize-space(.)]">
    <xsl:element name="{fn:name()}">
        <xsl:call-template name="setKey" />
        <xsl:apply-templates />    
    </xsl:element>
</xsl:template>

<!-- ****************************************************************************** 
    Remove SEEK specific metadata and data
    ************************************************************************** -->
<xsl:template
    match="*[starts-with(@key,'investigation.is')]" priority="2" />
<xsl:template match="string[@key='id' or @key='title' ]"
    priority="1" />
<xsl:template match="string[@key='description']"
    priority="2" />
<xsl:template match="map[@key='jsonapi']" />

<!-- ***************************************************************************** --> 
<!-- Exceptions --> 
<!-- ***************************************************************************** --> 

<!-- Design is expected to be lower case -->
<xsl:template match="map[@key=concat('Design',$resource)]"
    priority="2">
    <map>
        <xsl:attribute name="key" select="'design'" />
        <xsl:apply-templates />      
    </map>
</xsl:template>

<!-- single instead of array -->
<xsl:template
    match="array[@key=concat('Design_centers',$resource)]" priority="2">
    <string>    
        <xsl:call-template name="setKey" />
        <xsl:value-of select="string/text()"/>
   </string>
</xsl:template>


<!-- Map language codes to be in csh style (e.g. "English" to "EN (English)" -->
<xsl:template
    match="string[
    @key=concat('Resource_acronyms_language',$resource)
    or @key=concat('Resource_titles_language',$resource)
    or @key=concat('Resource_descriptions_language',$resource)]  
    |array[@key=concat('Resource_languages',$resource)]/string"
    priority="2">
    <string>
        <xsl:call-template name="setKey" />
        <xsl:choose>
            <xsl:when test="text()='English'"><xsl:text>EN (English)</xsl:text></xsl:when>
            <xsl:when test="text()='German'"><xsl:text>DE (German)</xsl:text></xsl:when>
            <xsl:when test="text()='French'"><xsl:text>FR (French)</xsl:text></xsl:when>
            <xsl:otherwise><xsl:value-of select="fn:concat(fn:upper-case(fn:substring(text(),1,2)),' (',text(),')')"/></xsl:otherwise>
        </xsl:choose>       
        <!-- Reactivate for exceptions of general mapping rule 
        Falsch! German ist NICHT GE (German)
        <xsl:value-of select="fn:concat(fn:upper-case(fn:substring(text(),1,2)),' (',text(),')')"/>
         -->
    </string>
</xsl:template>


</xsl:stylesheet>

