<?xml version="1.0"?>

<xsl:stylesheet 
  version="2.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xsl xlink xhtml"
>

<xsl:output method="xml" encoding="utf-8"/>

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

<!-- insert figure number  -->
<xsl:template match="*:figure">
  <xsl:variable name="number" select="count(./preceding::*:figure) + 1"/>
  <xsl:element name="{name()}" namespace="{namespace-uri()}"><!-- remove namespace in figure element and its subnodes  -->
    <xsl:apply-templates select="@*"/>
    <xsl:attribute name="number"><xsl:value-of select="$number"/></xsl:attribute>
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

<!-- remove the labeled handwritten elements, unlabeled handwritten elements: insert figure number  -->
<xsl:template match="*:handwritten">
  <xsl:variable name="number" select="count(./preceding::*:handwritten) + 1"/>
  <xsl:variable name="hasLabel" select="not(empty(@xlink:label))"/>
  <xsl:if test="not($hasLabel)">
    <handwritten>
      <xsl:attribute name="number"><xsl:value-of select="$number"/></xsl:attribute>
      <xsl:apply-templates/>
    </handwritten>
  </xsl:if>
</xsl:template>

<!-- anchored/labeled handwritten elements (called by anchor), insert figure number  -->
<xsl:template match="*:handwritten" mode="replace">
  <xsl:variable name="number" select="count(./preceding::*:handwritten) + 1"/>
  <handwritten>
    <xsl:attribute name="number"><xsl:value-of select="$number"/></xsl:attribute>
    <xsl:apply-templates/>
  </handwritten>
</xsl:template>

<xsl:template match="*:anchor">
  <xsl:variable name="type" select="@type"/>
  <xsl:variable name="href" select="@xlink:href"/>
  <xsl:choose>
    <xsl:when test="$type = 'figure'">
      <xsl:variable name="figure" select="//*:figure[@xlink:label = $href]"/>
      <xsl:apply-templates select="$figure"/>
    </xsl:when>
    <xsl:when test="$type = 'handwritten'">
      <xsl:variable name="handwritten" select="//*:handwritten[@xlink:label = $href]"/>
      <xsl:apply-templates select="$handwritten" mode="replace"/>
    </xsl:when>
    <xsl:when test="$type = 'note'">
      <xsl:variable name="note" select="//*:note[@xlink:label = $href]"/>
      <xsl:sequence select="$note"/>
    </xsl:when>
    <xsl:when test="$type = 'table'">
      <xsl:variable name="table" select="//xhtml:table[@xlink:label = $href]"/>
      <xsl:sequence select="$table"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- remove the anchored divs: div which contain a labeled figure, note, handwritten or table -->
<xsl:template match="*:div">
  <xsl:variable name="containingElems" select="*:figure|*:note|*:handwritten|xhtml:table"/>
  <xsl:variable name="hasLabel" select="not(empty($containingElems[1]/@xlink:label))"/>
  <xsl:choose>
    <xsl:when test="$hasLabel"></xsl:when>
    <xsl:otherwise>
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


</xsl:stylesheet>
