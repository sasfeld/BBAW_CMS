<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html" encoding="utf-8"/>

<xsl:template match="*:hi">
  <xsl:variable name="type" select="string(@type)"/>
  <xsl:choose>
    <xsl:when test="$type = 'elem'">
      <div style="background-color:#D3D3D3"><xsl:apply-templates/></div>
    </xsl:when>
    <xsl:when test="$type = 'hit'">
      <span style="background-color:#77DD77"><xsl:apply-templates/></span>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*[. except self::*:hi]">
  <xsl:element name="span">
    <xsl:attribute name="class"><xsl:value-of select="name()"/></xsl:attribute>
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

</xsl:stylesheet>
