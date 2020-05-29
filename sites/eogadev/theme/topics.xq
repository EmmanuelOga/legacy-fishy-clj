xquery version "3.1";

declare namespace sd = 'http://eoga.dev/sdoc';

(: Root of the site, to access with file:* functions. :)
declare variable $root-path as xs:string external;

(: Name of the main database for the site. :)
declare variable $database-name as xs:string external;

(: Topic corresponds to the current topic being rendered :)
declare variable $topic-name as xs:string external;

(: Whether we are previewing or rendering the actual result :)
declare variable $previewing as xs:boolean external;

(: Outputs the result as html. :)
declare option output:method 'html';
declare option output:html-version '5.0';

(: Turn whitespace chopping off. :)
declare option db:chop 'no';

(: Return a topic by name, by first looking it up on the current
database, then on the export folder. Returns empty seq if can't be
found. :)
declare function sd:read-topic($name as xs:string) {
  let $topic-path := $root-path || "/export" || $topic-name
  return if (db:exists($database-name, $topic-name))
  then db:open($database-name, $topic-name)
  else if (file:exists($topic-path))
  then fn:parse-xml(file:read-text($root-path || "/export" || $topic-name))
  else ()
};

let $xsl := fn:parse-xml(file:read-text($root-path || "/theme/topic.xsl"))
let $in := sd:read-topic($topic-name)
return if ($in)
then xslt:transform($in, $xsl)
else <html>404</html>
