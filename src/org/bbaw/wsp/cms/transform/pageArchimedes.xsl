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

<xsl:template match="*:archimedes">
  <xsl:apply-templates mode="text"/>
</xsl:template>

<xsl:template match="*:expan|*:emph|*:q|*:quote|*:blockquote|*:set-off|*:reg|*:var|*:num" mode="text">
  <xsl:element name="span">
    <xsl:attribute name="class"><xsl:value-of select="name()"/></xsl:attribute>
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates mode="text"/>
  </xsl:element>
</xsl:template>

<xsl:template match="text" mode="text">
  <xsl:apply-templates mode="text"/>
  <!--   Notes                      -->
  <xsl:variable name="bottomNotes" select="//*:note[contains(@position, 'bottom') or empty(string(@position))]"/>
  <xsl:variable name="countBottomNotes" select="count($bottomNotes)"/>
  <xsl:if test="$countBottomNotes > 0">
    <span class="notes">
      <xsl:for-each select="$bottomNotes">
        <xsl:variable name="noteSign"><xsl:value-of select="count(./preceding::*:note) + 1"/></xsl:variable>
        <span class="note">
          <span class="noteSign">
            <xsl:value-of select="$noteSign"/>
          </span>
          <span>
            <xsl:attribute name="class"><xsl:value-of select="'noteBody bottom'"/></xsl:attribute>
            <xsl:apply-templates mode="text"/>
          </span>
        </span>
      </xsl:for-each>
    </span>
  </xsl:if>
</xsl:template>

<xsl:template match="*:div" mode="text">
  <div class="div">
    <xsl:apply-templates mode="text"/>
  </div>
</xsl:template>

<xsl:template match="*:p" mode="text">
  <xsl:variable name="type" select="string(@type)"/>
  <xsl:choose>
    <xsl:when test="$type = 'head'">
      <div class="head bf">
        <xsl:apply-templates mode="text"/>
      </div>
    </xsl:when>
    <xsl:otherwise>
      <div class="p">
        <xsl:apply-templates mode="text"/>
      </div>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*:pb" mode="text">
  <xsl:variable name="number">
    <xsl:choose>
      <xsl:when test="not(empty(@n))"><xsl:value-of select="@n"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="count(./preceding::*:pb) + 1"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="file">
    <xsl:choose>
      <xsl:when test="not(empty(@xlink:href))"><xsl:value-of select="@xlink:href"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="$number"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
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

<!-- MathML    -->
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

<xsl:template match="*:figure" mode="text">
  <xsl:variable name="xlinkHref" select="@xlink:href"/>
  <xsl:variable name="fileName" select="concat('figures/', $xlinkHref)"/>
  <span class="figure">
    <img>
      <xsl:attribute name="src"><xsl:value-of select="$fileName"/></xsl:attribute>
    </img>
    <xsl:if test="not(empty(@number))">
      <span>
        <xsl:attribute name="class"><xsl:value-of select="'figureNumber'"/></xsl:attribute>
        <span>
          <xsl:attribute name="class"><xsl:value-of select="'figureNum'"/></xsl:attribute>
          <xsl:value-of select="string(@number)"/>
        </span>
        <span>
          <xsl:attribute name="class"><xsl:value-of select="'figureNumText'"/></xsl:attribute>
          <xsl:value-of select="concat('[', 'Figure', ' ', string(@number), ']')"/>
        </span>
      </span>
    </xsl:if>
  </span>
</xsl:template>

<xsl:template match="lb" mode="text">
  <br/><xsl:apply-templates mode="text"/>
</xsl:template>

<xsl:template match="cb" mode="text">
  <br/><xsl:apply-templates mode="text"/>
</xsl:template>

<xsl:template match="note" mode="text">
  <xsl:variable name="place"><xsl:value-of select="string(@position)"/></xsl:variable>
  <xsl:variable name="noteSign"><xsl:value-of select="count(./preceding::*:note) + 1"/></xsl:variable>
  <span class="note">
    <span class="noteSign">
      <xsl:value-of select="$noteSign"/>
    </span>
    <xsl:if test="$place != '' and $place != 'bottom'">
      <span>
        <xsl:attribute name="class"><xsl:value-of select="concat('noteBody ', $place)"/></xsl:attribute>
        <xsl:apply-templates mode="text"/>
      </span>
    </xsl:if>
  </span>
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
