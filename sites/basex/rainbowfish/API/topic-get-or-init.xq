xquery version "3.1";

declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";
declare namespace sd = "https://eoga.dev/sdoc";

declare variable $basepath as xs:string external;
declare variable $xmldb as xs:string external;
declare variable $topic as xs:string external;

declare option output:omit-xml-declaration "no";
declare option output:method "xml";

(: Turn whitespace chopping off. :)
declare option db:chop 'no';

let $in := if (db:exists($xmldb, $topic))
           then db:open($xmldb, $topic)
           else fn:parse-xml(file:read-text($basepath || "/API/default.topic")) transform with {
             replace value of node sd:topic/sd:title with $topic
           }
let $html := $in => xslt:transform($basepath || "/API/topic-to-html-snippet.xsl")
return
  <response status="200">
    <!-- Topic -->
    {$in}

    <!-- Meta -->
    <meta>{file:read-text($basepath || "/API/default.ttl")}</meta>

    <!-- Preview -->
    {$html}
  </response>