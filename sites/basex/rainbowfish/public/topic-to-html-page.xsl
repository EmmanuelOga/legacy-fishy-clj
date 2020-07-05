<?xml version='1.0' encoding='UTF-8'?>
<xsl:stylesheet version="3.1"
  xmlns:array="http://www.w3.org/2005/xpath-functions/array"
  xmlns:fn="http://www.w3.org/2005/xpath-functions"
  xmlns:map="http://www.w3.org/2005/xpath-functions/map"
  xmlns:saxon="http://saxon.sf.net/"
  xmlns:sd="https://eoga.dev/sdoc"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:err="http://www.w3.org/2005/xqt-errors"
  exclude-result-prefixes="#all">
  <xsl:output indent="yes" method="xhtml" />

  <xsl:mode on-no-match="shallow-copy"/><!-- Identity transform! -->

  <xsl:template match="sd:topic">
    <html lang="en">
      <head>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="stylesheet" href="/css/index.css" />
        <script type='application/ld+json'> 
        </script>
      </head>

      <body>
        <header class="clear">
          <xsl:apply-templates select="sd:header/*" />
        </header>

        <main class="clear">
          <xsl:if test="sd:body/@title" expand-text="yes">
            <h2>{sd:body/@title}</h2>
          </xsl:if>
          <xsl:apply-templates select="(sd:body, sd:description)[1]/*" />
        </main>

        <footer class="clear">
          <xsl:apply-templates select="sd:footer/*" />
        </footer>

        Example: comoga/about
        <xsl:copy-of select="doc('basex://comoga/about')" />

        Example: devoga/index
        <xsl:copy-of select="doc('basex://comoga/index')" />
     </body>
    </html>
  </xsl:template>

  <xsl:template match="sd:ref" expand-text="yes">
    <xsl:choose>
      <xsl:when test="@class = 'logo'">
        <h1><a href="{@topic | text()}">{text()}</a></h1>
      </xsl:when>
      <xsl:otherwise>
        <a href="{@topic | text()}">{text()}</a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="sd:section">
    <h3><xsl:value-of select="@title" /></h3>
    <xsl:apply-templates select="*" />
  </xsl:template>

  <xsl:template match="sd:*">
    <xsl:element name="{local-name()}">
      <xsl:apply-templates select="@*, node()"/>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
