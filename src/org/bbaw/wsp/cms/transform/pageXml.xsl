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

<!-- transform to browser like xml display -->
<xsl:template match="element()">
  <xsl:variable name="elementName" select="name()"/>
  <xsl:variable name="elementPresentation">
    <xsl:choose>
    <xsl:when test="element() = node() or text() != ''">
      <xsl:value-of select="'&lt;'"/>
      <span class="xml elementName"><xsl:value-of select="$elementName"/></span>
      <xsl:apply-templates select="attribute()"/>
      <xsl:value-of select="'&gt;'"/>
      <xsl:apply-templates select="element()|text()|comment()|processing-instruction()"/>
      <xsl:value-of select="'&lt;/'"/>
      <span class="xml elementName"><xsl:value-of select="$elementName"/></span>
      <xsl:value-of select="'&gt;'"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="'&lt;'"/>
      <span class="xml elementName"><xsl:value-of select="$elementName"/></span>
      <xsl:apply-templates select="attribute()"/>
      <xsl:value-of select="'/&gt;'"/>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:choose>
    <xsl:when test="$elementName = 'w' and $mode != 'tokenized'">
      <xsl:apply-templates/>
    </xsl:when>
    <xsl:otherwise>
      <ul class="xml element">
        <xsl:sequence select="$elementPresentation"/>
      </ul>
    </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template match="attribute()">
  <xsl:variable name="attributeName" select="name()"/>
  <span class="xml attributeName">
    <xsl:value-of select="' '"/>
    <xsl:value-of select="$attributeName"/>
  </span>
  <xsl:value-of select="'=&quot;'"/>
  <span class="xml attributeValue"><xsl:value-of select="."/></span><xsl:value-of select="'&quot;'"/>
</xsl:template>

<xsl:template match="comment()">
  <span class="xml comment">
    <xsl:value-of select="'&lt;!-- '"/><xsl:value-of select="."/><xsl:value-of select="' --&gt;'"/>
  </span>
</xsl:template>

<xsl:template match="processing-instruction()">
</xsl:template>

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
