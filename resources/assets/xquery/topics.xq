xquery version "3.1";

declare namespace sd = 'http://eoga.dev/sdoc';

declare variable $assets-path as xs:string external;
declare variable $browse as xs:boolean external;
declare variable $format as xs:string external;
declare variable $host as xs:string external;
declare variable $topic as xs:string external;
declare variable $xmldb as xs:string external;
declare variable $xsl-topic as xs:string external;
declare variable $default-triples as xs:string external;

(: Outputs the result as html. :)
declare option output:method 'html';
declare option output:html-version '5.0';

(: Turn whitespace chopping off. :)
declare option db:chop 'no';

(: Return a topic by name, by first looking it up on the current
database, then on the export folder. Returns empty seq if can't be
found. :)
declare function sd:read-topic($topic as xs:string) {
  let $topic-path := $assets-path || "/export" || $topic || ".topic"
  return if (db:exists($xmldb, $topic))
  then db:open($xmldb, $topic)
  else if (file:exists($topic-path))
  then fn:parse-xml(file:read-text($topic-path))
  else ()
};

let $in := sd:read-topic($topic)
let $html := if ($in)
             then xslt:transform($in, fn:parse-xml($xsl-topic)) update {
               insert node <p>{$topic}</p> into .
             }
             else
               <html>
                 <body>
                   404
                 </body>
               </html>

return if ($browse) then
<response>
  {$in}
  <meta>
  {$default-triples}
  </meta>
  {$html}
</response>
else $html