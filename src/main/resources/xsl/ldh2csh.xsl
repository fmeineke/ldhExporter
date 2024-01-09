<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xpath-default-namespace="http://www.w3.org/2005/xpath-functions"
    xmlns="http://www.w3.org/2005/xpath-functions"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    exclude-result-prefixes="xs math fn" 
    version="3.0">

    <xsl:output indent="yes" omit-xml-declaration="no" />
    <xsl:mode on-no-match="shallow-skip" />

<xsl:template match="/">
    <map>
        <xsl:apply-templates
            select="map/map/string[@key='id']" />
        <xsl:apply-templates
            select="map/map/map[@key='attributes']/string" />
        <xsl:apply-templates
            select="map/map/map/map[@key='extended_attributes']/map/* " />
    </map>
</xsl:template>

<xsl:template name="setKey">
    <xsl:if test="@key">
        <xsl:attribute name="key"
            select="fn:tokenize(fn:replace(@key,'_Investigation',''),'_')[last()]" />
    </xsl:if>
</xsl:template>

<xsl:template match="array[normalize-space(.)]">
    <array>
        <xsl:call-template name="setKey" />
        <xsl:apply-templates />
    </array>
</xsl:template>

<xsl:template
    match="boolean[starts-with(@key,'Resource') or starts-with(@key,'Design')]">
    <boolean>
        <xsl:call-template name="setKey" />
        <xsl:value-of select="text()" />
    </boolean>
</xsl:template>

<xsl:template match="string[normalize-space(.)]">
    <string>
        <xsl:call-template name="setKey" />
        <xsl:value-of select="text()" />
    </string>
</xsl:template>

<xsl:template match="map[normalize-space(.)]">
    <map>
        <xsl:call-template name="setKey" />
        <xsl:apply-templates />
    </map>
</xsl:template>


<!-- ****************************************************************************** 
    Remove these 
    ************************************************************************** -->
<xsl:template
    match="*[starts-with(@key,'investigation.is')]" priority="2" />
<xsl:template match="string[@key='id' or @key='title']"
    priority="1" />
<xsl:template match="string[@key='description']"
    priority="2" />
<xsl:template match="map[@key='jsonapi']" />

<!-- ****************************************************************************** 
    Unclear who to blame ******************************************************************************* -->
<!-- Design is expected to be lower case -->
<xsl:template match="map[@key='Design_Investigation']"
    priority="2">
    <map>
        <xsl:attribute name="key" select="'design'" />
        <xsl:apply-templates />
    </map>
</xsl:template>

<!-- ****************************************************************************** 
    Repair MDS 3.2 SEED File flaws ******************************************************************************* -->
<xsl:template
    match="array[@key='Design_centers_Investigation']" priority="2">
    <string>
        <xsl:call-template name="setKey" />
        <xsl:value-of select="*/text()" />
    </string>
</xsl:template>

<xsl:template
    match="array[@key='Design_interventional_phase_Investigation']"
    priority="2">
    <string>
        <xsl:call-template name="setKey" />
        <xsl:value-of select="*/text()" />
    </string>
</xsl:template>

<!-- ****************************************************************************** 
    Repair extended metadata export flaws Problem: Some elements have to be arrays 
    rather then single items. This matches only with map having a key; they should 
    be lifted to an array 
    *************************************************************************** -->

<!-- Create array from single map -->
<xsl:template
    match="string[
   @key='Design_centersNumber_Investigation' 
or @key='Design_eligibilityCriteria_ageMin_number_Investigation']"
    priority="2">
    <number>
        <xsl:call-template name="setKey" />
        <xsl:value-of select="text()" />
    </number>
</xsl:template>

<!-- Create array from single map -->
<xsl:template
    match="map[
   @key='Resource_titles_Investigation' 
or @key='Resource_acronyms_Investigation'
or @key='Resource_descriptions_Investigation'
or @key='Resource_contributors_Investigation'
or @key='Resource_contributors_personal_identifiers_Investigation'
or @key='Resource_contributors_affiliations_Investigation'
or @key='Resource_contributors_affiliations_identifiers_Investigation'
or @key='Resource_idsAlternative_Investigation'
or @key='Resource_ids_Investigation'
or @key='Resource_idsNfdi4health_Investigation'
or @key='Design_conditions_Investigation'
]"
    priority="2">
    <array>
        <xsl:call-template name="setKey" />
        <map>
            <xsl:apply-templates select="*" />
        </map>
    </array>
</xsl:template>

<!-- Create array from single string -->
<xsl:template
    match="string[
   @key='Design_population_countries_Investigation'
or @key='Design_eligibilityCriteria_genders_Investigation'
   ]"
    priority="2">
    <array>
        <xsl:call-template name="setKey" />
        <string>
            <xsl:value-of select="text()" />
        </string>
    </array>
</xsl:template>

<!-- Map language codes to be in csh style-->
<xsl:template
    match="string[@key='Resource_acronyms_language_Investigation']
    |string[@key='Resource_titles_language_Investigation']
    |array[@key='Resource_languages_Investigation']/string"
    priority="2">
    <string>
        <xsl:call-template name="setKey" />
        <xsl:choose>
            <xsl:when test="text()='English'"><xsl:text>EN (English)</xsl:text></xsl:when>
            <xsl:otherwise><xsl:value-of select="text()"/></xsl:otherwise>
        </xsl:choose>       
    </string>
</xsl:template>


<!-- Convert String to boolean-->
<xsl:template
    match="string[
   @key='Resource_nutritionalData_Investigation'
or @key='Resource_chronicDiseases_Investigation'
   ]"
    priority="2">
    <boolean>
        <xsl:call-template name="setKey" />
         <xsl:value-of select="'false'" />
    </boolean>
</xsl:template>

</xsl:stylesheet>

