<?xml version="1.0"?>
<xsl:stylesheet version="2.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:dc="http://purl.org/dc/elements/1.1/" 
  xmlns:dcterms="http://purl.org/dc/terms"
  xmlns:math="http://www.w3.org/1998/Math/MathML"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xsl xlink dcterms math svg xhtml"
  >

<xsl:output method="xhtml" encoding="utf-8"/>

<xsl:param name="docId"></xsl:param>
<xsl:param name="mode"></xsl:param>  <!-- tokenized or untokenized -->
<xsl:param name="page"></xsl:param>
<xsl:param name="normalization"></xsl:param>

<xsl:variable name="dictionaryServiceName" select="'http://mpdl-service.mpiwg-berlin.mpg.de/mpiwg-mpdl-lt-web/lt/GetDictionaryEntries'"/>

<xsl:template match="element()">
  <span>
    <xsl:attribute name="class"><xsl:value-of select="name()"/></xsl:attribute>
    <xsl:apply-templates/>
  </span>
</xsl:template>

<!-- words                            -->
<xsl:template match="*:w">
  <xsl:variable name="wordLanguage" select="@lang"/>
  <xsl:variable name="form" select="encode-for-uri(string(@form))"/>
  <xsl:variable name="formNotUrlEncoded" select="string(@form)"/>
  <xsl:variable name="formRegularized" select="string(@formRegularized)"/>
  <xsl:variable name="formNormalized" select="string(@formNormalized)"/>
  <xsl:variable name="lemmas" select="string(@lemmas)"/>
  <xsl:variable name="dictionary" select="string(@dictionary)"/>
  <xsl:variable name="displayWord">
    <xsl:choose>
      <xsl:when test="$normalization = 'orig'"><xsl:apply-templates/></xsl:when>
      <xsl:when test="$normalization = 'reg' and $formRegularized = ''"><xsl:apply-templates/></xsl:when>
      <xsl:when test="$normalization = 'reg' and $formRegularized != ''"><xsl:sequence select="$formRegularized"/></xsl:when>
      <xsl:when test="$normalization = 'norm'"><xsl:apply-templates/></xsl:when>
      <xsl:otherwise><xsl:apply-templates/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="displayWordUrlEncoded" select="encode-for-uri($displayWord)"/>
  <xsl:choose>
    <xsl:when test="$dictionary = 'true' and $mode = 'tokenized'">
      <a class="dictionary">
        <xsl:attribute name="href"><xsl:value-of select="concat($dictionaryServiceName, '?query=', $form, '&amp;queryDisplay=', $displayWordUrlEncoded, '&amp;language=', $wordLanguage, '&amp;outputFormat=html', '&amp;outputType=morphCompact&amp;outputType=dictFull')"/></xsl:attribute>
        <xsl:sequence select="$displayWord"/>
      </a>
    </xsl:when>
    <xsl:otherwise><xsl:sequence select="$displayWord"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!--  highlighting -->
<xsl:template match="*:hi">
  <xsl:choose>
    <xsl:when test="@type = 'elem'">
      <div>
        <xsl:attribute name="class"><xsl:value-of select="concat('highlight ', @type)"/></xsl:attribute>
        <xsl:apply-templates/>
      </div>
    </xsl:when>
    <xsl:otherwise>
      <span>
        <xsl:attribute name="class"><xsl:value-of select="concat('highlight ', @type)"/></xsl:attribute>
        <xsl:apply-templates/>
      </span>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
