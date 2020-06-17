xquery version "3.1";

declare namespace sd = 'http://eoga.dev/sdoc';
declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";

declare variable $assets-path as xs:string external;
declare variable $browse as xs:boolean external;
declare variable $format as xs:string external;
declare variable $host as xs:string external;
declare variable $topic as xs:string external;
declare variable $xmldb as xs:string external;
declare variable $xsl-topic as xs:string external;

declare variable $default-topic as xs:string external;
declare variable $default-triples as xs:string external;

(:
declare option output:method 'html';
declare option output:html-version '5.0';
:)
declare option output:omit-xml-declaration "no";
declare option output:method "xml";

(: Turn whitespace chopping off. :)
declare option db:chop 'no';

if ($browse) then
  let $in := if (db:exists($xmldb, $topic))
             then db:open($xmldb, $topic)
             else fn:parse-xml($default-topic)
  let $html := xslt:transform($in, fn:parse-xml($xsl-topic))
  return <response>
           {$in}
           <meta>{$default-triples}</meta>
           {$html}
         </response>
else if (db:exists($xmldb, $topic)) then
  let $in := db:open($xmldb, $topic)
  return xslt:transform($in, fn:parse-xml($xsl-topic))
  else
    let $info := map {
      "assets-path" : $assets-path,
      "browse" : $browse,
      "format" : $format,
      "host" : $host,
      "topic" : $topic,
      "xmldb" : $xmldb
    }
    return
      <html>
        <style>
        <![CDATA[
          body {
            font-family: "Segoe UI";
            font-size: 2rem;
          }
          .info {
            border: 1px solid black;
            min-width: 30rem;
          }
          .info tr:nth-child(odd) {
            background-color: #efe;
          }
          .info td {
            padding: 1rem
          }
          .param {
            font-weight: bold;
          }
        ]]>
        </style>
        <body>
          <h1>404</h1>

          <table class="info">
            {
              for $key in map:keys($info)
              return <tr>
                       <td class="param">{$key}</td>
                       <td class="value">{$info($key)}</td>
                      </tr>
            }
          </table>
        </body>
    </html>