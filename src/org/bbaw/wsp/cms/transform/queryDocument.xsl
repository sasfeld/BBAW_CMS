<?xml version="1.0"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xhtml" encoding="utf-8"/>
 
<xsl:param name="query"></xsl:param>
<xsl:param name="flags"></xsl:param>
<xsl:param name="outputFormat"></xsl:param>

<xsl:variable name="apos">'</xsl:variable>
<xsl:variable name="xpathQuery">
  <xsl:choose>
    <xsl:when test="$flags = ''"><xsl:value-of select="concat('//*:s[matches(.,', $apos, $query, $apos, ')]', '|', '//*:head[matches(.,', $apos, $query, $apos, ')]')"/></xsl:when>
    <xsl:otherwise><xsl:value-of select="concat('//*:s[matches(.,', $apos, $query, $apos, ',', $apos, $flags, $apos, ')]', '|', '//*:head[matches(.,', $apos, $query, $apos, ',', $apos, $flags, $apos, ')]')"/></xsl:otherwise>
  </xsl:choose>
</xsl:variable>
<xsl:variable name="queryResult" select="saxon:evaluate($xpathQuery)" xmlns:saxon="http://saxon.sf.net/"/>
<xsl:variable name="queryResultSize"><xsl:value-of select="count($queryResult)"/></xsl:variable>
<xsl:variable name="queryResultPageSize" select="20"/>
<xsl:variable name="queryResultPages">
  <xsl:choose>
    <xsl:when test="$queryResultSize = 0"><xsl:value-of select="count($queryResult)"/></xsl:when>
    <xsl:otherwise><xsl:value-of select="$queryResultSize idiv $queryResultPageSize + 1"/></xsl:otherwise>
  </xsl:choose>
</xsl:variable>
<xsl:variable name="pageBreaks" select="saxon:evaluate('//*:pb')" xmlns:saxon="http://saxon.sf.net/"/>
<xsl:variable name="queryResultHtml">
<xsl:choose>
  <xsl:when test="$outputFormat = 'html'">
    <table>
      <thead>
        <tr>
          <th align="left" valign="top">
            <button name="order-by" value="author" style="padding:0px;font-weight:bold;font-size:14px;background:none;border:none;">No.</button>
          </th>
          <th align="left" valign="top">
            <button name="order-by" value="author" style="padding:0px;font-weight:bold;font-size:14px;background:none;border:none;">Path</button>
          </th>
          <th align="left" valign="top">
            <button name="order-by" value="author" style="padding:0px;font-weight:bold;font-size:14px;background:none;border:none;">Hit</button>
          </th>
          <th align="left" valign="top">
            <button name="order-by" value="author" style="padding:0px;font-weight:bold;font-size:14px;background:none;border:none;">Page</button>
          </th>
        </tr>
      </thead>
      <tbody>
        <xsl:for-each select="$queryResult">
          <xsl:variable name="hit" select="."/>
          <tr>
            <td align="left" valign="top"><xsl:value-of select="position()"/></td>
            <td align="left" valign="top"><xsl:value-of select="saxon:path(.)" xmlns:saxon="http://saxon.sf.net/"/></td>
            <td align="left" valign="top"><xsl:value-of select="."/></td>
            <td align="left" valign="top"><xsl:value-of select="count(./preceding::*:pb)"/></td>     <!-- better performance: count($pageBreaks[. << $hit])  -->
          </tr>
        </xsl:for-each>    
      </tbody>
    </table>
  </xsl:when>
  <xsl:otherwise>
    <result>
      <query><xsl:value-of select="$query"/></query>
      <flags><xsl:value-of select="$flags"/></flags>
      <size><xsl:value-of select="$queryResultSize"/></size>
      <page-size><xsl:value-of select="$queryResultPageSize"/></page-size>
      <pages><xsl:value-of select="$queryResultPages"/></pages>
      <pn>1</pn>
      <hits>
        <xsl:for-each select="$queryResult">
          <xsl:variable name="hit" select="."/>
          <hit>
            <hitType><xsl:value-of select="'s'"/></hitType>
            <pos><xsl:value-of select="position()"/></pos>
            <pn><xsl:value-of select="count(./preceding::*:pb)"/></pn>
            <hitId>xmlId</hitId>
            <hitPos>4711</hitPos>
            <hitString><xsl:value-of select="."/></hitString>
            <hitSurroundsPB>false</hitSurroundsPB>
          </hit>
        </xsl:for-each>    
      </hits>
      <query-forms></query-forms>
      <query-regularizations></query-regularizations>
    </result>
  </xsl:otherwise>
</xsl:choose>
</xsl:variable>
<xsl:template match="/">
  <xsl:sequence select="$queryResultHtml"/>
</xsl:template>

</xsl:stylesheet>
