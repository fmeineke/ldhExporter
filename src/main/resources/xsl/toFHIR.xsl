<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xs="http://www.w3.org/2005/xpath-functions"
        xmlns="http://hl7.org/fhir"
        version="3.0">

<xsl:output indent="yes" omit-xml-declaration="yes" />
<xsl:mode on-no-match="shallow-skip"/>

<xsl:template match="/">
	<ResearchStudy>
    <meta>
        <profile value="https://www.nfdi4health.de/fhir/metadataschema/StructureDefinition/nfdi4health-pr-mds-study" />
    </meta>
    <extension url="https://www.nfdi4health.de/fhir/metadataschema/StructureDefinition/nfdi4health-ex-mds-resource-type">
        <valueCoding>
            <system value="http://terminology.hl7.org/CodeSystem/umls" />
            <code value="C0947630" />
            <display value="Scientific Study (Research Activity)" />
        </valueCoding>
    </extension>
	<description>
		<xsl:attribute name="value">
			<xsl:value-of select="//xs:string[@key='description']"/>
		</xsl:attribute>
	</description>
	</ResearchStudy>
</xsl:template>
</xsl:stylesheet>
