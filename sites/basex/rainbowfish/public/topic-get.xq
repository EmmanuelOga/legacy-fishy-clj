xquery version "3.1";

declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";

declare variable $basepath as xs:string external;
declare variable $xmldb as xs:string external;
declare variable $topic as xs:string external;
declare variable $content-type as xs:string external;

declare option output:method 'html';
declare option output:html-version '5.0';

let $json-options := map { 'format' : 'xquery', 'indent' : 'no' }
return
if (db:exists($xmldb, $topic))
then
   let $in := db:open($xmldb, $topic)
   let $out := if ($content-type = "text/html")
               then xslt:transform($in, $basepath || "/public/topic-to-html-page.xsl")
               else $in
   return (
            json:serialize(map {'code' : 200}, $json-options),
            "===BOUNDARY===",
            $out
          )
   else (
          json:serialize(map {'code' : 404}, $json-options),
          "===BOUNDARY===",
          "Resource not found"
        )
