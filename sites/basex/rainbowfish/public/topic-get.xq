xquery version "3.1";

declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";

declare variable $basepath as xs:string external;
declare variable $xmldb as xs:string external;
declare variable $topic-name as xs:string external;
declare variable $topic-json-ld as xs:string external;
declare variable $topic-content-type as xs:string external;

declare option output:method 'html';
declare option output:html-version '5.0';

let $in := (db:open($xmldb)[db:path(.)=$topic-name], db:open($xmldb, '404.topic'))[1]
let $json-options := map { 'format' : 'xquery', 'indent' : 'no' }
return
  if ($in) then
    let $xsl := $basepath || "/public/topic-to-html-page.xsl"
    let $out := if ($topic-content-type = "text/html")
                then xslt:transform($in, $xsl, map {'xmldb' : $xmldb,
                                                    'json-ld' : $topic-json-ld})
                else $in
    return (json:serialize(map {'code' : 200}, $json-options),
            "===BOUNDARY===",
            $out)
  else (json:serialize(map {'code' : 404}, $json-options),
        "===BOUNDARY===",
        "<error code='404'>Resource not found</error>")
