<?xml version="1.0"?>

<xsl:stylesheet 
  version="2.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<xsl:output encoding="utf-8" indent="yes" omit-xml-declaration="yes"/>

<xsl:param name="type"></xsl:param>

<xsl:template match="/">
  <xsl:choose>
    <xsl:when test="$type = 'toc'"><xsl:sequence select="//*:list[@type = 'toc']"/></xsl:when>
    <xsl:when test="$type = 'figures'"><xsl:sequence select="//*:list[@type = 'figures']"/></xsl:when>
    <xsl:when test="$type = 'handwritten'"><xsl:sequence select="//*:list[@type = 'handwritten']"/></xsl:when>
    <xsl:when test="$type = 'pages'"><xsl:sequence select="//*:list[@type = 'pages']"/></xsl:when>
    <xsl:otherwise></xsl:otherwise>
  </xsl:choose>
</xsl:template>


</xsl:stylesheet>
