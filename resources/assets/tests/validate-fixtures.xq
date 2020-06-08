xquery version "3.1";

declare variable $schema as xs:string external;
declare variable $fixtures-path as xs:string external;

<results>{
  (: Find fixtures recursively. :)
  for $path in file:list($fixtures-path, true())
  let $in := fn:parse-xml(file:read-text($fixtures-path || "/" || $path))
  (: Validate using a RelaxNG Compact schema. :)
  return validate:rng-report($in, $schema, true()) update {
    insert node attribute {'file'} { $path } into .
  }
}</results>
