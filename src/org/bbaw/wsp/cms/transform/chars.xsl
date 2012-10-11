<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml" encoding="utf-8" omit-xml-declaration="yes"/>

<xsl:template match="text()">
  <xsl:sequence select="normalize-space(.)"/>
</xsl:template>

</xsl:stylesheet>
