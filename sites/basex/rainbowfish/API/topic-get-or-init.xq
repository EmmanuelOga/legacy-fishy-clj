xquery version "3.1";

declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";
declare namespace sd = "https://eoga.dev/sdoc";

declare variable $basepath as xs:string external;
declare variable $xmldb as xs:string external;
declare variable $topic as xs:string external;

declare option db:chop 'no';

let $in := if (db:exists($xmldb, $topic))
           then db:open($xmldb, $topic)
           else fn:parse-xml(file:read-text($basepath || "/API/default.topic"))
                transform with {
                  replace value of node sd:topic/sd:title with $topic
                }
let $html := $in => xslt:transform($basepath || "/API/topic-to-html-snippet.xsl")
let $json-options := map { 'format' : 'xquery', 'indent' : 'no' }
return (
  (: Data for the HTTPD :)
  json:serialize(map {
    'code' : 200,
    'content-type' : 'application/json'
  }, $json-options),

  "===BOUNDARY===",

  (: Client payload :)
  json:serialize(
    map {'meta': file:read-text($basepath || "/API/default.ttl"),
         'sdoc': serialize($in),
         'html': $html},
    $json-options)
)