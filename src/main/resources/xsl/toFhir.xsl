<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2005/xpath-functions"
    xmlns="http://hl7.org/fhir" 
    exclude-result-prefixes="xs">

    <xsl:output indent="yes" omit-xml-declaration="no" />
    <!-- 
    <xsl:mode on-no-match="shallow-skip" />
    -->

    <xsl:template match="/">
        <ResearchStudy>
            <meta>
                <profile value="https://www.nfdi4health.de/fhir/metadataschema/StructureDefinition/nfdi4health-pr-mds-study" />
            </meta>
            <xsl:apply-templates />
        </ResearchStudy>
    </xsl:template>

    <xsl:template match="titles/text">
        <title>
            <xsl:value-of select="text()" />
        </title>
    </xsl:template>

    <xsl:template match="descriptions/text">
    <extension url="https://www.nfdi4health.de/fhir/metadataschema/StructureDefinition/nfdi4health-ex-mds-descriptions">
        <extension url="value">
            <valueString value="{text()}" />
        </extension>
        <extension url="https://www.nfdi4health.de/fhir/metadataschema/StructureDefinition/nfdi4health-ex-mds-language">
            <valueCodeableConcept>
                <coding>
                    <system value="http://terminology.hl7.org/CodeSystem/iso639-1" />
                    <code value="en" />
                    <display value="English" />
                </coding>
            </valueCodeableConcept>
        </extension>
    </extension>
    </xsl:template>
    
    <xsl:template match="subject">
     <extension url="https://www.nfdi4health.de/fhir/metadataschema/StructureDefinition/nfdi4health-ex-mds-subject">
        <valueCoding>
            <system value="http://snomed.info/sct" />
            <code value="125676002" />
            <display value="Person (person)" />
        </valueCoding>
    </extension>    
    </xsl:template>
    
    <!-- 
    https://simplifier.net/NFDI4Health-Metadata-Schema/nfdi4health-vs-mds-yes-no-undecided-snomedct-nci/~xml
    system/code/display aus dem text
    
    <xsl:choose>
        <xsl:when test="starts-with('Yes',"x")"></xsl:when>
        <xsl:when test="No"></xsl:when>
        <xsl:when test="Undecided"></xsl:when>
    </xsl:choose>
     -->
    
    <xsl:template match="administrativeInformation/status">
    
    <extension url="https://www.nfdi4health.de/fhir/metadataschema/StructureDefinition/nfdi4health-ex-mds-study-status">
        <extension url="overallStatus">
            <valueCoding>
                <system value="https://www.nfdi4health.de/fhir/metadataschema/CodeSystem/nfdi4health-cs-mds-study-status" />
                <code value="04" />
                <display value="{text()}" />
            </valueCoding>
        </extension>
        <extension url="statusEnrollingByInvitation">
            <valueCoding>
                <system value="http://snomed.info/sct" />
                <code value="373066001" />
                <display value="Yes (qualifier value)" />
            </valueCoding>
        </extension>
    </extension>
    </xsl:template>
    

    <xsl:template match="design/interventional/phase">
        <phase>
            <xsl:value-of select="text()" />
        </phase>
    </xsl:template>

</xsl:stylesheet>
