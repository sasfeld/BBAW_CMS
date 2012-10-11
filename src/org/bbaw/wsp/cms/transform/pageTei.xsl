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

<xsl:template match="*:TEI">
  <xsl:apply-templates mode="text"/>
</xsl:template>

<xsl:template match="*:expan|*:emph|*:q|*:quote|*:reg|*:num" mode="text">
  <xsl:element name="span">
    <xsl:attribute name="class"><xsl:value-of select="name()"/></xsl:attribute>
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates mode="text"/>
  </xsl:element>
</xsl:template>

<xsl:template match="*:text" mode="text">
  <xsl:apply-templates mode="text"/>
  <!--   Dictionary                      -->
  <xsl:if test="not(empty(//entry))">
    <xsl:for-each select="//*:entry">
      <xsl:sort select="*:form/*:orth"/>
      <xsl:variable name="position" select="position()"/>
      <span class="entry">
        <xsl:apply-templates mode="text" select="*:form"/>
        <xsl:apply-templates mode="text" select="*:sense"/>
        <xsl:if test="not(empty(figure))">
          <span class="entryDiv">
            <span class="bf"><xsl:value-of select="'Figures: '"/></span>
            <span class="entryDiv">
              <xsl:for-each select="*:figure">
                <xsl:variable name="href" select="*:graphic/@url"/>
                <xsl:variable name="head" select="*:head"/>
                <xsl:variable name="figDesc" select="string(*:figDesc)"/>
                <xsl:if test="$href != ''">
                  <div class="figure" style="margin-left:10px;">
                    <a href="{$href}"><img alt="Figure: {$figDesc}" src="{$href}" width="200" height="200"/></a>
                    <br/>
                    <xsl:value-of select="'[Figure]: '"/><xsl:value-of select="$head"/>
                  </div>
                </xsl:if>
              </xsl:for-each>
            </span>
          </span>
        </xsl:if>
        <xsl:if test="not(empty(*:xr))">
          <span class="entryDiv">
            <span class="bf"><xsl:value-of select="'References: '"/></span>
            <span class="entryDiv">
              <xsl:for-each select="xr/ref">
                <li><xsl:apply-templates mode="text" select="."/></li>
              </xsl:for-each>
            </span>
          </span>
        </xsl:if>
      </span>
    </xsl:for-each>  
  </xsl:if>
  <!--   Notes                      -->
  <xsl:variable name="bottomNotes" select="//*:note[contains(@place, 'bottom') or empty(string(@place))]"/>
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
  <div class="p">
    <xsl:apply-templates mode="text"/>
  </div>
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
      <xsl:when test="not(empty(@facs))"><xsl:value-of select="@facs"/></xsl:when>
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

<xsl:template match="*:figure" mode="text">
  <xsl:variable name="src">
    <xsl:choose>
      <xsl:when test="not(empty(@facs))">
        <xsl:value-of select="string(@facs)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="string(graphic/@url)"/>
      </xsl:otherwise>
   </xsl:choose>
  </xsl:variable>
  <span class="figure">
    <img>
      <xsl:attribute name="src"><xsl:value-of select="$src"/></xsl:attribute>
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
    <xsl:if test="not(empty(*:head))">
      <xsl:for-each select="*:head/*">
        <span class="caption"><xsl:apply-templates mode="text"/></span>
      </xsl:for-each>
    </xsl:if>
  </span>
</xsl:template>

<!-- segmentation   -->
<xsl:template match="*:seg" mode="text">
  <xsl:choose>
    <xsl:when test="@rend = 'highlight'">
      <span class="seg highlight"><xsl:apply-templates mode="text"/></span>
    </xsl:when>
    <xsl:when test="@rend = 'highlightPoint'">
      <span class="seg highlightPoint"><xsl:apply-templates mode="text"/></span>
    </xsl:when>
    <xsl:otherwise>
      <span class="seg"><xsl:apply-templates mode="text"/></span>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- choice   -->
<xsl:template match="*:choice" mode="text">
  <xsl:if test="not(empty(*:orig))">
    <xsl:apply-templates select="*:orig" mode="text"/>
  </xsl:if>
  <xsl:if test="not(empty(*:abbr))">
    <xsl:apply-templates select="*:abbr" mode="text"/>
  </xsl:if>
  <xsl:if test="not(empty(*:am))">
    <xsl:apply-templates select="*:am" mode="text"/>
  </xsl:if>
  <xsl:if test="not(empty(*:sic))">
    <xsl:apply-templates select="*:sic" mode="text"/>
  </xsl:if>
</xsl:template>

<!-- hi (highlighted)  -->
<xsl:template match="*:hi" mode="text">
  <xsl:choose>
    <xsl:when test="not(empty(@type)) and @type = 'elem'">
      <div>
        <xsl:attribute name="class"><xsl:value-of select="concat('highlight ', @type)"/></xsl:attribute>
        <xsl:apply-templates mode="text"/>
      </div>
    </xsl:when>
    <xsl:when test="not(empty(@type)) and @type != 'elem'">
      <span>
        <xsl:attribute name="class"><xsl:value-of select="concat('highlight ', @type)"/></xsl:attribute>
        <xsl:apply-templates mode="text"/>
      </span>
    </xsl:when>
    <xsl:when test="@rend = 'initial'">
      <span class="dc-unmodified"><xsl:apply-templates mode="text"/></span>
    </xsl:when>
    <xsl:when test="@rend = 'bold'">
      <span class="bf"><xsl:apply-templates mode="text"/></span>
    </xsl:when>
    <xsl:otherwise>
      <span>
        <xsl:attribute name="class"><xsl:value-of select="@rend"/></xsl:attribute>
        <xsl:apply-templates mode="text"/>
      </span>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- name (of type: place, person, ...)   -->
<xsl:template match="*:name" mode="text">
  <xsl:choose>
    <xsl:when test="@type = 'place'">
      <span class="place" title="Place"><xsl:apply-templates mode="text"/></span>
    </xsl:when>
    <xsl:when test="@type = 'person'">
      <span class="person" title="Person"><xsl:apply-templates mode="text"/></span>
    </xsl:when>
    <xsl:when test="@type = 'org'">
      <span class="organization" title="Organization"><xsl:apply-templates mode="text"/></span>
    </xsl:when>
    <xsl:otherwise>
      <span>
        <xsl:attribute name="class"><xsl:value-of select="@type"/></xsl:attribute>
        <xsl:apply-templates mode="text"/>
      </span>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- place, person, ...)   -->
<xsl:template match="*:placeName" mode="text">
  <span class="place" title="Place: {@type}"><xsl:apply-templates mode="text"/></span>
</xsl:template>
<xsl:template match="*:persName" mode="text">
  <span class="person" title="Person"><xsl:apply-templates mode="text"/></span>
</xsl:template>

<!-- term    -->
<xsl:template match="*:term" mode="text">
  <span class="term" title="Terminology entry"><xsl:apply-templates mode="text"/></span>
</xsl:template>

<!-- line    -->
<xsl:template match="*:lg" mode="text">
  <div class="lg"><xsl:apply-templates mode="text"/></div>
</xsl:template>

<xsl:template match="*:l" mode="text">
  <span class="l"><xsl:apply-templates mode="text"/></span><br/>
</xsl:template>

<!-- reference    -->
<xsl:template match="*:ref" mode="text">
  <span class="ref">
    <xsl:if test="not(empty(@target))">
      <a class="ref">
        <xsl:attribute name="href"><xsl:value-of select="@target"/></xsl:attribute>
        <xsl:apply-templates mode="text"/>
      </a>
    </xsl:if>
    <xsl:if test="empty(@target)">
      <xsl:apply-templates mode="text"/>
    </xsl:if>
  </span>
</xsl:template>

<!-- table    -->
<xsl:template match="*:table" mode="text">
  <table>
    <xsl:if test="not(empty(head))">
      <caption align="top"><xsl:apply-templates mode="text" select="*:head"/></caption>
    </xsl:if>
    <xsl:apply-templates mode="text" select="*:row"/>
  </table>
</xsl:template>

<xsl:template match="*:row" mode="text">
  <xsl:choose>
    <xsl:when test="@role = 'label'">
      <tr style="font-weight:bold;"><xsl:apply-templates mode="text"/></tr>
    </xsl:when>
    <xsl:when test="@role = 'data' or empty(@role)">
      <tr><xsl:apply-templates mode="text"/></tr>
    </xsl:when>
    <xsl:otherwise>
      <tr><xsl:apply-templates mode="text"/></tr>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*:cell" mode="text">
  <xsl:choose>
    <xsl:when test="@role = 'label' and empty(@cols)">
      <td style="font-weight:bold;"><xsl:apply-templates mode="text"/></td>
    </xsl:when>
    <xsl:when test="@role = 'label' and not(empty(@cols))">
      <td colspan="{@cols}" style="font-weight:bold;"><xsl:apply-templates mode="text"/></td>
    </xsl:when>
    <xsl:when test="(@role = 'data' or empty(@role)) and empty(@cols)">
      <td><xsl:apply-templates mode="text"/></td>
    </xsl:when>
    <xsl:when test="(@role = 'data' or empty(@role)) and not(empty(@cols))">
      <td colspan="{@cols}"><xsl:apply-templates mode="text"/></td>
    </xsl:when>
    <xsl:otherwise>
      <td><xsl:apply-templates mode="text"/></td>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- dictionary    -->
<xsl:template match="*:entry" mode="text">
  <!-- empty: handled in text tag    -->
</xsl:template>

<xsl:template match="*:form" mode="text">
  <span class="form">
    <xsl:apply-templates mode="text"/>
  </span>
</xsl:template>

<xsl:template match="*:orth" mode="text">
  <span class="orth">
    <xsl:apply-templates mode="text"/>
  </span>
</xsl:template>

<xsl:template match="*:sense" mode="text">
  <span class="entryDiv"><xsl:apply-templates mode="text" select="*:def"/></span>
  <span class="entryDiv"><xsl:apply-templates mode="text" select="*:etym"/></span>
</xsl:template>

<xsl:template match="*:def" mode="text">
  <span class="def">
    <span class="bf"><xsl:value-of select="'Definition: '"/></span>
    <xsl:apply-templates mode="text"/>
  </span>
</xsl:template>

<xsl:template match="*:mentioned" mode="text">
  <span class="mentioned">
    <xsl:apply-templates mode="text"/>
  </span>
</xsl:template>

<xsl:template match="*:etym" mode="text">
  <span class="etym">
    <span class="bf"><xsl:value-of select="'Etymology: '"/></span>
    <span class="entryDiv">
      <xsl:for-each select="*:cit">
        <li><xsl:value-of select="*:quote"/><xsl:value-of select="' ('"/><xsl:value-of select="*:def"/><xsl:value-of select="')'"/></li>
      </xsl:for-each>
    </span>
  </span>
</xsl:template>

<xsl:template match="*:cit" mode="text">
  <span class="cit">
    <xsl:apply-templates mode="text"/>
  </span>
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

<xsl:template match="*:lb" mode="text">
  <br/><xsl:apply-templates mode="text"/>
</xsl:template>

<xsl:template match="*:cb" mode="text">
  <br/><xsl:apply-templates mode="text"/>
</xsl:template>

<xsl:template match="*:note" mode="text">
  <xsl:variable name="place"><xsl:value-of select="string(@place)"/></xsl:variable>
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

<xsl:template match="*:foreign" mode="text">
  <xsl:variable name="xmllang" select="@xml:lang"/>
  <xsl:variable name="language">
    <xsl:choose>
      <xsl:when test="not(empty($xmllang))"><xsl:value-of select="$xmllang"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="''"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>  
  <span>
    <xsl:attribute name="class"><xsl:value-of select="concat('foreign ', $language)"/></xsl:attribute>
    <xsl:apply-templates mode="text"/>
  </span>
</xsl:template>

<xsl:template match="*:gap" mode="text">
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

<xsl:template match="*:s" mode="text">
  <span class="s">
    <xsl:apply-templates mode="text"/>
  </span>
</xsl:template>

<xsl:template match="text()" mode="text">
  <xsl:value-of select="."/>
</xsl:template>

</xsl:stylesheet>
