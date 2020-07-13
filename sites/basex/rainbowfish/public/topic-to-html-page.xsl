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

  <!-- ******************************************************************************** -->

  <xsl:param name="xmldb" as="xs:string" />
  <xsl:param name="json-ld" as="xs:string" />

  <xsl:variable name="root" type="xsl:sequence" select="doc('basex://' || $xmldb || '/index.topic')" />

  <!-- ******************************************************************************** -->

  <xsl:mode on-no-match="shallow-copy"/><!-- Identity transform! -->

  <xsl:template match="sd:topic">
    <html lang="en">
      <head>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="stylesheet" href="/css/index.css" />
        <script type='application/ld+json'> 
          <xsl:value-of select="$json-ld" />
        </script>
      </head>

      <body>
        <div class="top-banner"></div>
        <div class="main-content">
          <header class="main-header clear">
            <xsl:apply-templates select="(sd:header, $root//sd:header)[1]/*" />
          </header>

          <div class="main-body clear">
            <aside class="nav-tree">
            </aside>

            <main class="content">
              <xsl:if test="sd:body/@title" expand-text="yes">
                <h2>{sd:body/@title}</h2>
              </xsl:if>
              <xsl:apply-templates select="(sd:body, sd:description)[1]/*" />
            </main>

            <aside class="current-tree">
            </aside>
          </div>
        </div>

        <footer class="main-footer clear">
          <xsl:apply-templates select="(sd:footer, $root//sd:footer)[1]/*" />
        </footer>
     </body>
    </html>
  </xsl:template>

  <xsl:template match="sd:ref" expand-text="yes">
    <xsl:choose>
      <xsl:when test="@class='logo'">
        <h1><a href="/{fn:lower-case((@topic, text())[1])}">{text()}</a></h1>
      </xsl:when>
      <xsl:otherwise>
        <a href="/{fn:lower-case((@topic, text())[1])}">{text()}</a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="sd:refs" expand-text="yes">
    <xsl:variable name="entries"
                  select="doc('basex://' || $xmldb || '/' || @prefix ||
                              '?list-topics=true&amp;limit=' || xs:string((@limit, 0)))" />
    <xsl:variable name="show-dates" select="not(@hide-dates)" />

    <xsl:for-each select="$entries//entry">
      <a class="topic-ref" href="{fn:replace(./@path, '.topic$', '')}">
        <xsl:if test="$show-dates">
          <span class="date">{./@date}</span>
        </xsl:if>
        <span class="title">{./@title}</span>
      </a>
    </xsl:for-each>
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
