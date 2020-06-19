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
    <html>
      <header class="clear">
        <xsl:apply-templates select="sd:header/*" />
      </header>

      <main class="clear">
        <xsl:apply-templates select="(sd:body, sd:description)[1]/*" />
      </main>

      <footer class="clear">
        <xsl:apply-templates select="//sd:footer/*" />
      </footer>
    </html>
  </xsl:template>

</xsl:stylesheet>
