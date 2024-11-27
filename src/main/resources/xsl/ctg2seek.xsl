<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2005/xpath-functions"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    exclude-result-prefixes="xs">

    <xsl:output indent="yes" omit-xml-declaration="no" />

<xsl:variable name="type" select="'Project'"/>
<xsl:variable name="seekType" select="concat(fn:lower-case($type),'s')"/>

<xsl:template match="/">
    <ObjectNode> <!-- Any name - not usedin json -->
        <data>
            <type><xsl:value-of select="$seekType"/></type>
            <xsl:apply-templates />
        </data>
    </ObjectNode>
</xsl:template>

<xsl:template match="ObjectNode">
    <xsl:apply-templates />
</xsl:template>

<xsl:template match="protocolSection">
    <attributes>
        <xsl:apply-templates mode="attributes" />
        <extended_attributes>
            <attribute_map>
                <xsl:element name="{concat('Resource_classification_',$type)}">
                    <xsl:element name="{concat('Resource_classification_',$type)}">
                        <xsl:text>Study</xsl:text>
                    </xsl:element>
                </xsl:element>
                <xsl:apply-templates/>
            </attribute_map>
        </extended_attributes>
    </attributes>
</xsl:template>

<!-- ******************************************* -->
<!-- *** Start with SEEK attributes          *** -->
<!-- ******************************************* -->
<xsl:template match="identificationModule" mode="attributes">
    <xsl:apply-templates mode="attributes" />
</xsl:template>

<xsl:template match="briefTitle" mode="attributes">
    <title>
        <xsl:value-of select="." />
    </title>
</xsl:template>

<xsl:template match="descriptionModule" mode="attributes">
    <description>
        <xsl:value-of select="." />
    </description>
</xsl:template>

<xsl:template match="*|text()" mode="attributes" />

<!-- ******************************************* -->
<!-- *** Now all the MDS 7 extended_metadata *** -->
<!-- ******************************************* -->
<xsl:template match="contactsLocationsModule">
    <xsl:apply-templates/>
    <Design_centersNumber_Project>
        <xsl:value-of select="count(locations)" />
    </Design_centersNumber_Project>
</xsl:template>



<xsl:template match="identificationModule">
    <xsl:apply-templates/>
</xsl:template>

<xsl:template match="nctId">
    <Resource_idsAlternative_Project />  <!-- Dummy to enforce json array -->
    <Resource_idsAlternative_Project>
        <Resource_idsAlternative_scheme_Project>
            <xsl:text>NCT (ClinicalTrials.gov)</xsl:text>
        </Resource_idsAlternative_scheme_Project>
        <Resource_idsAlternative_identifier_Project>
            <xsl:value-of select="." />
        </Resource_idsAlternative_identifier_Project>
    </Resource_idsAlternative_Project>
</xsl:template>

<xsl:template match="officialTitle">
    <Resource_titles_Project /> <!-- Dummy to enforce json array -->
    <Resource_titles_Project>
        <Resource_titles_text_Project>
            <xsl:value-of select="." />
        </Resource_titles_text_Project>
        <Resource_acronyms_language_Project>English</Resource_acronyms_language_Project>
    </Resource_titles_Project>
</xsl:template>

<xsl:template match="descriptionModule">
    <Resource_descriptions_Project /> <!-- Dummy to enforce json array -->
    <Resource_descriptions_Project>
        <Resource_descriptions_text_Project>
            <xsl:value-of select="." />
        </Resource_descriptions_text_Project>
        <Resource_descriptions_language_Project>English</Resource_descriptions_language_Project>
    </Resource_descriptions_Project>
</xsl:template>

<xsl:template match="acronym">
    <Resource_acronyms_Project /> <!-- Dummy to enforce json array -->
    <Resource_acronyms_Project>
        <Resource_acronyms_text_Project>
            <xsl:value-of select="." />
        </Resource_acronyms_text_Project>
        <Resource_acronyms_language_Project>English</Resource_acronyms_language_Project>
    </Resource_acronyms_Project>
</xsl:template>

<xsl:template match="*|text()" />

</xsl:stylesheet>