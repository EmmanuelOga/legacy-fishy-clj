xquery version "3.1";

declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";

declare variable $xmldb as xs:string external;
declare variable $topic as xs:string external;
declare variable $xsl-topic as xs:string external;

declare variable $default-topic as xs:string external;
declare variable $default-triples as xs:string external;

declare option output:omit-xml-declaration "no";
declare option output:method "xml";

(: Turn whitespace chopping off. :)
declare option db:chop 'no';

let $in := if (db:exists($xmldb, $topic))
           then db:open($xmldb, $topic)
           else fn:parse-xml($default-topic)
let $html := $in => xslt:transform(fn:parse-xml($xsl-topic))
return
  <response status="200">
    <!-- Topic -->
    {$in}

    <!-- Meta -->
    <meta>{$default-triples}</meta>

    <!-- Preview -->
    {$html}
  </response>