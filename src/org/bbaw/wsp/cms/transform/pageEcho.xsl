<?xml version="1.0"?>
<xsl:stylesheet version="2.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:functx="http://www.functx.com"
  xmlns:saxon="http://saxon.sf.net/"
  xmlns:dc="http://purl.org/dc/elements/1.1/" 
  xmlns:dcterms="http://purl.org/dc/terms"
  xmlns:echo="http://www.mpiwg-berlin.mpg.de/ns/echo/1.0/" 
  xmlns:math="http://www.w3.org/1998/Math/MathML"
  xmlns:mml="http://www.w3.org/1998/Math/MathML"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xsl xlink xs functx saxon dc dcterms echo math mml svg xhtml"
  >

<xsl:output method="xhtml" encoding="utf-8"/>

<xsl:param name="docId"></xsl:param>
<xsl:param name="mode"></xsl:param>  <!-- tokenized or untokenized -->
<xsl:param name="page"></xsl:param>
<xsl:param name="normalization"></xsl:param>

<xsl:variable name="dictionaryServiceName" select="'http://mpdl-service.mpiwg-berlin.mpg.de/mpiwg-mpdl-lt-web/lt/GetDictionaryEntries'"/>

<xsl:template match="*:echo">
  <xsl:apply-templates mode="text"/>
</xsl:template>

<xsl:template match="*:text" mode="text">
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

<xsl:template match="*:head" mode="text">
  <xsl:variable name="style" select="@style"/>
  <div class="head bf">
    <xsl:choose>
      <xsl:when test="not(empty($style))">
        <span>
          <xsl:attribute name="class"><xsl:value-of select="$style"/></xsl:attribute>
          <xsl:apply-templates mode="text"/>
        </span>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="text"/>
      </xsl:otherwise>
    </xsl:choose>
  </div>
</xsl:template>

<xsl:template match="*:figure|*:handwritten" mode="text">
  <xsl:variable name="class">
    <xsl:choose>
      <xsl:when test="empty(@position)"><xsl:value-of select="name()"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="concat(name(), ' ', string(@position))"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="image" select="*:image"/>
  <xsl:variable name="caption" select="*:caption"/>
  <xsl:variable name="description" select="*:description"/>
  <xsl:variable name="variables" select="*:variables"/>
  <span>
    <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
    <xsl:if test="not(empty($image))">
      <xsl:variable name="fileName" select="concat('figures/', string($image/@file))"/>
      <img>
        <xsl:attribute name="src"><xsl:value-of select="concat('figures/', string($image/@file))"/></xsl:attribute>
      </img>
    </xsl:if>
    <xsl:if test="not(empty(@number))">
      <xsl:variable name="type">
        <xsl:choose>
          <xsl:when test="name() = 'figure'"><xsl:value-of select="'Figure'"/></xsl:when>
          <xsl:when test="name() = 'handwritten'"><xsl:value-of select="'Handwritten'"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="''"/></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <span>
        <xsl:attribute name="class"><xsl:value-of select="'figureNumber'"/></xsl:attribute>
        <span>
          <xsl:attribute name="class"><xsl:value-of select="'figureNum'"/></xsl:attribute>
          <xsl:value-of select="string(@number)"/>
        </span>
        <span>
          <xsl:attribute name="class"><xsl:value-of select="'figureNumText'"/></xsl:attribute>
          <xsl:value-of select="concat('[', $type, ' ', string(@number), ']')"/>
        </span>
      </span>
    </xsl:if>
    <xsl:if test="not(empty($caption))">
      <xsl:for-each select="$caption/*">
        <span class="caption"><xsl:apply-templates mode="text"/></span>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="not(empty($description))">
      <xsl:for-each select="$description/*">
        <span class="description">
          <xsl:apply-templates mode="text"/>
        </span>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="not(empty($variables))">
      <xsl:for-each select="$variables/*">
        <span class="variables">
          <xsl:apply-templates mode="text"/>
        </span>
      </xsl:for-each>
    </xsl:if>
  </span>
</xsl:template>

<!-- MathML    -->
<xsl:template match="math:*" mode="text">
  <xsl:element name="{name()}" namespace="">
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates mode="text"/>
  </xsl:element>
</xsl:template>

<xsl:template match="mml:*" mode="text">
  <xsl:element name="{name()}" namespace="">
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates mode="text"/>
  </xsl:element>
</xsl:template>

<!-- XHTML: remove the xhtml namespace   -->
<xsl:template match="xhtml:*" mode="text">
  <xsl:variable name="hasLabel" select="not(empty(@xhtml:label))"/>
  <xsl:variable name="isTable" select="local-name() = 'table'"/>
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

<!-- SVG    -->
<xsl:template match="svg:*" mode="text">
  <xsl:element name="{name()}" namespace="">
    <xsl:copy-of select="@*"/>
    <xsl:apply-templates mode="text"/>
  </xsl:element>
</xsl:template>

<xsl:template match="*:place|*:person" mode="text">
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
  <xsl:variable name="style" select="@style"/>
  <xsl:variable name="class" select="'p'"/>
  <xsl:choose>
    <xsl:when test="not(empty($style))">
      <div>
        <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
        <span>
          <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
          <xsl:apply-templates mode="text"/>
        </span>
      </div>
    </xsl:when>
    <xsl:otherwise>
      <div>
        <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
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
      <xsl:when test="not(empty(@file))"><xsl:value-of select="@file"/></xsl:when>
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
      <xsl:if test="not(empty(@o))">
        <span>
          <xsl:attribute name="class"><xsl:value-of select="'o'"/></xsl:attribute>
          <xsl:value-of select="@o"/>
        </span>
      </xsl:if>
      <xsl:if test="not(empty(@rhead))">
        <span>
          <xsl:attribute name="class"><xsl:value-of select="'rhead'"/></xsl:attribute>
          <xsl:value-of select="@rhead"/>
        </span>
      </xsl:if>
    </span>
  </div>
</xsl:template>

<xsl:template match="*:lb" mode="text">
  <br/><xsl:apply-templates mode="text"/>
</xsl:template>

<xsl:template match="*:cb" mode="text">
  <br/><xsl:apply-templates mode="text"/>
</xsl:template>

<xsl:template match="*:expan" mode="text">
  <span class="expan"><xsl:apply-templates mode="text"/></span>
</xsl:template>

<xsl:template match="*:note" mode="text">
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

<xsl:template match="*:emph" mode="text">
  <xsl:variable name="style" select="@style"/>
  <xsl:variable name="styleWithoutSC" select="replace($style, 'sc ', '')"/>
  <xsl:variable name="text" select="string-join(., '')"/>
  <xsl:variable name="length" select="string-length($text)"/>
  <xsl:variable name="firstChar" select="substring($text, 1, 1)"/>
  <xsl:variable name="first2Chars" select="substring($text, 1, 2)"/>
  <xsl:variable name="restChars" select="substring($text, 2, $length)"/>
  <xsl:variable name="first2CharsAreUppercase" select="upper-case($first2Chars) = $first2Chars"/>
  <!-- an emph as first element in p and also an emph in s in p is recognized  --> 
  <xsl:variable name="isFirstElementInP" select="(not(empty(./parent::*/parent::p)) and (empty(./parent::*/preceding-sibling::node()) and empty(./preceding-sibling::node()))) or (not(empty(./parent::p)) and empty(./preceding-sibling::node()))"/>
  <xsl:variable name="rest">
    <xsl:choose>
      <xsl:when test="$length &lt; 2 or empty($length)"><xsl:value-of select="''"/></xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="not(empty(w))">
            <a class="dictionary" href="{$dictionaryServiceName}?query={w/@form}&amp;queryDisplay={$text}&amp;language={w/@lang}&amp;outputFormat=html&amp;outputType=morphCompact&amp;outputType=dictFull"><xsl:value-of select="$restChars"/></a>
          </xsl:when>
          <xsl:otherwise><xsl:value-of select="$restChars"/></xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:choose>
    <xsl:when test="not(contains($style, 'sc'))">
      <span class="{$style}"><xsl:apply-templates mode="text"/></span>
    </xsl:when>
    <xsl:when test="$style = 'sc' and $length = 1">
      <span class="sc"><xsl:value-of select="$firstChar"/></span>
    </xsl:when>
    <xsl:when test="$style = 'sc' and not($isFirstElementInP)">
      <span class="sc"><xsl:apply-templates mode="text"/></span>
    </xsl:when>
    <!-- special case (see text() node below): first char dc, rest sc  -->
    <xsl:when test="$style = 'dc sc logic' and $first2CharsAreUppercase">
      <span class="dc"><xsl:value-of select="$firstChar"/></span><span class="sc"><xsl:sequence select="$rest"/></span>
    </xsl:when>
    <xsl:when test="$style = 'sc' and $first2CharsAreUppercase">
      <span class="dc"><xsl:value-of select="$firstChar"/></span><span class="sc"><xsl:sequence select="$rest"/></span>
    </xsl:when>
    <xsl:when test="contains($style, 'sc') and $first2CharsAreUppercase">
      <span class="dc"><span class="{$styleWithoutSC}"><xsl:value-of select="$firstChar"/></span></span><span class="sc"><span class="{$styleWithoutSC}"><xsl:sequence select="$rest"/></span></span>
    </xsl:when>
    <xsl:when test="$style = 'sc' and not($first2CharsAreUppercase)">
      <span class="sc"><xsl:apply-templates mode="text"/></span>
    </xsl:when>
    <xsl:when test="contains($style, 'sc') and not($first2CharsAreUppercase)">
      <span class="sc"><span class="{$styleWithoutSC}"><xsl:apply-templates mode="text"/></span></span>
    </xsl:when>
    <xsl:when test="$style != ''">
      <span class="{$style}"><xsl:apply-templates mode="text"/></span>
    </xsl:when>
    <xsl:otherwise>
      <span class="emph"><xsl:apply-templates mode="text"/></span>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*:foreign" mode="text">
  <xsl:variable name="lang" select="@lang"/>
  <xsl:variable name="xmllang" select="@xml:lang"/>
  <xsl:variable name="language">
    <xsl:choose>
      <xsl:when test="not(empty($xmllang))"><xsl:value-of select="$xmllang"/></xsl:when>
      <xsl:when test="not(empty($lang))"><xsl:value-of select="$lang"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="''"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>  
  <xsl:variable name="class">
    <xsl:choose>
      <xsl:when test="$language = ''"><xsl:value-of select="'foreign'"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="concat('foreign ', $language)"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>  
  <span>
    <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
    <xsl:apply-templates mode="text"/>
  </span>
</xsl:template>

<xsl:template match="*:q" mode="text">
  <span class="q"><xsl:apply-templates mode="text"/></span>
</xsl:template>

<xsl:template match="*:quote" mode="text">
  <span class="quote"><xsl:apply-templates mode="text"/></span>
</xsl:template>

<xsl:template match="*:blockquote" mode="text">
  <span class="blockquote"><xsl:apply-templates mode="text"/></span>
</xsl:template>

<xsl:template match="*:set-off" mode="text">
  <span class="set-off"><xsl:apply-templates mode="text"/></span>
</xsl:template>

<xsl:template match="*:reg" mode="text">
  <span class="reg"><xsl:apply-templates mode="text"/></span>
</xsl:template>

<xsl:template match="*:var" mode="text">
  <xsl:variable name="class">
    <xsl:choose>
      <xsl:when test="empty(@type)"><xsl:value-of select="'var'"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="concat('var ', string(@type))"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>  
  <span>
    <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
    <xsl:apply-templates mode="text"/>
  </span>
</xsl:template>

<xsl:template match="*:num" mode="text">
  <span class="num"><xsl:apply-templates mode="text"/></span>
</xsl:template>

<xsl:template match="*:gap" mode="text">
  <span class="gap">
    <xsl:if test="not(empty(@extent))"><span class="extent"><xsl:value-of select="string(@extent)"/></span></xsl:if>
    <xsl:apply-templates mode="text"/>
  </span>
</xsl:template>

<!-- words  -->
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

</xsl:stylesheet>
