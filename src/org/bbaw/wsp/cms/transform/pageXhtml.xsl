<?xml version="1.0"?>
<xsl:stylesheet version="2.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:functx="http://www.functx.com"
  xmlns:saxon="http://saxon.sf.net/"
  xmlns:dc="http://purl.org/dc/elements/1.1/" 
  xmlns:dcterms="http://purl.org/dc/terms"
  xmlns:math="http://www.w3.org/1998/Math/MathML"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xsl xlink xs functx saxon dc dcterms math svg xhtml"
  >

<xsl:output method="xhtml" encoding="utf-8"/>

<xsl:param name="docId"></xsl:param>
<xsl:param name="mode"></xsl:param>  <!-- tokenized or untokenized -->
<xsl:param name="page"></xsl:param>
<xsl:param name="normalization"></xsl:param>

<xsl:variable name="dictionaryServiceName" select="'http://mpdl-service.mpiwg-berlin.mpg.de/mpiwg-mpdl-lt-web/lt/GetDictionaryEntries'"/>

<xsl:template match="*:html">
  <xsl:apply-templates mode="text"/>
</xsl:template>

<xsl:template match="body" mode="text">
  <xsl:apply-templates mode="html"/>
</xsl:template>

<!-- html mode: transform html texts -->
<xsl:template match="@*|node()" mode="html">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()" mode="html"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="s" mode="html">
  <xsl:apply-templates select="." mode="text"/>
</xsl:template>

<xsl:template match="w" mode="html">
  <xsl:apply-templates select="." mode="text"/>
</xsl:template>


<xsl:template match="*:expan|*:emph|*:q|*:quote|*:blockquote|*:set-off|*:reg|*:var|*:num|*:figure" mode="text">
  <xsl:element name="span">
    <xsl:attribute name="class"><xsl:value-of select="name()"/></xsl:attribute>
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates mode="text"/>
  </xsl:element>
</xsl:template>

<xsl:template match="*:div" mode="text">
  <div class="div">
    <xsl:apply-templates mode="text"/>
  </div>
</xsl:template>

<xsl:template match="*:p" mode="text">
  <div class="p">
    <xsl:apply-templates mode="text"/>
  </div>
</xsl:template>

<xsl:template match="pb" mode="text">
  <xsl:variable name="number" select="count(./preceding::*:pb) + 1"/>
  <xsl:variable name="file" select="$number"/>
  <div>
    <xsl:attribute name="class"><xsl:value-of select="'pb'"/></xsl:attribute>
    <span>
      <xsl:attribute name="class"><xsl:value-of select="'src'"/></xsl:attribute>
      <xsl:attribute name="title"><xsl:value-of select="concat('pageimg/', string($file))"/></xsl:attribute>
      <span>
        <xsl:attribute name="class"><xsl:value-of select="'n'"/></xsl:attribute>
        <xsl:value-of select="$number"/>
      </span>
    </span>
  </div>
</xsl:template>

<xsl:template match="*:head" mode="text">
  <div class="head bf">
    <xsl:apply-templates mode="text"/>
  </div>
</xsl:template>

<!--  MathML    -->
<xsl:template match="math:*" mode="text">
  <xsl:element name="{name()}" namespace="">
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates mode="text"/>
  </xsl:element>
</xsl:template>

<!-- SVG    -->
<xsl:template match="svg:*" mode="text">
  <xsl:element name="{name()}" namespace="">
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates mode="text"/>
  </xsl:element>
</xsl:template>

<xsl:template match="lb|br" mode="text">
  <br/><xsl:apply-templates mode="text"/>
</xsl:template>

<xsl:template match="cb" mode="text">
  <br/><xsl:apply-templates mode="text"/>
</xsl:template>

<xsl:template match="note" mode="text">
  <span class="note"><xsl:apply-templates mode="text"/></span>
</xsl:template>

<xsl:template match="foreign" mode="text">
  <xsl:variable name="lang" select="@lang"/>
  <xsl:variable name="xmllang" select="@xml:lang"/>
  <xsl:variable name="language">
    <xsl:choose>
      <xsl:when test="not(empty($xmllang))"><xsl:value-of select="$xmllang"/></xsl:when>
      <xsl:when test="not(empty($lang))"><xsl:value-of select="$lang"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="''"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>  
  <span>
    <xsl:attribute name="class"><xsl:value-of select="concat('foreign ', $language)"/></xsl:attribute>
    <xsl:apply-templates mode="text"/>
  </span>
</xsl:template>

<xsl:template match="gap" mode="text">
  <xsl:variable name="extent" select="@extent"/>
  <xsl:variable name="count">
    <xsl:choose>
      <xsl:when test="empty($extent)"><xsl:value-of select="number(3)"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="number($extent)"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="gapChars" select="'...'"/>
  <xsl:value-of select="concat('[', $gapChars, ']')"/><xsl:apply-templates mode="text"/>
</xsl:template>

<!-- XHTML: remove the xhtml namespace   -->
<xsl:template match="xhtml:*" mode="text">
  <xsl:variable name="hasLabel" select="string(@xlink:label) != ''"/>
  <xsl:variable name="isTable" select="name() = 'table'"/>
  <xsl:choose>
    <xsl:when test="(not($hasLabel)) or ($isTable and $hasLabel)">
      <xsl:element name="{name()}" namespace="">
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates mode="text"/>
      </xsl:element>
    </xsl:when>
    <xsl:otherwise></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- words                            -->
<xsl:template match="*:w" mode="text">
  <xsl:variable name="wordLanguage" select="@lang"/>
  <xsl:variable name="form" select="encode-for-uri(string(@form))"/>
  <xsl:variable name="formNotUrlEncoded" select="string(@form)"/>
  <xsl:variable name="formRegularized" select="string(@formRegularized)"/>
  <xsl:variable name="formNormalized" select="string(@formNormalized)"/>
  <xsl:variable name="lemmas" select="string(@lemmas)"/>
  <xsl:variable name="dictionary" select="string(@dictionary)"/>
  <xsl:variable name="displayWord">
    <xsl:choose>
      <xsl:when test="$normalization = 'orig'"><xsl:apply-templates mode="text"/></xsl:when>
      <xsl:when test="$normalization = 'reg' and $formRegularized = ''"><xsl:apply-templates mode="text"/></xsl:when>
      <xsl:when test="$normalization = 'reg' and $formRegularized != ''"><xsl:sequence select="$formRegularized"/></xsl:when>
      <xsl:when test="$normalization = 'norm'"><xsl:apply-templates mode="text"/></xsl:when>
      <xsl:otherwise><xsl:apply-templates mode="text"/></xsl:otherwise>
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

<xsl:template match="s" mode="text">
  <span class="s">
    <xsl:apply-templates mode="text"/>
  </span>
</xsl:template>

<xsl:template match="*:hi" mode="text">
  <xsl:choose>
    <xsl:when test="@type = 'elem'">
      <div>
        <xsl:attribute name="class"><xsl:value-of select="concat('highlight ', @type)"/></xsl:attribute>
        <xsl:apply-templates mode="text"/>
      </div>
    </xsl:when>
    <xsl:otherwise>
      <span>
        <xsl:attribute name="class"><xsl:value-of select="concat('highlight ', @type)"/></xsl:attribute>
        <xsl:apply-templates mode="text"/>
      </span>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="text()" mode="text">
  <xsl:value-of select="."/>
</xsl:template>

</xsl:stylesheet>
