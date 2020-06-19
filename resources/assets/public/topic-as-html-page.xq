xquery version "3.1";

declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";

declare variable $xmldb as xs:string external;
declare variable $topic as xs:string external;
declare variable $xsl-topic as xs:string external;

declare option output:method 'html';
declare option output:html-version '5.0';

db:open($xmldb, $topic) => xslt:transform(fn:parse-xml($xsl-topic))