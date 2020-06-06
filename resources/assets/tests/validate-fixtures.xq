xquery version "3.1";

declare namespace sd = 'http://eoga.dev/sdoc';

declare function sd:read-string($path as xs:string) as xs:string {
  convert:binary-to-string(db:retrieve("rainbowfish", $path), 'UTF-8')
};

declare function sd:read-xml($path as xs:string) as item() {
  fn:parse-xml(sd:read-string($path))
};


let
  $schema := sd:read-string("assets/schemas/sdoc.rnc"),
  $compact := true()
  return <results>{
    for $path in db:list("rainbowfish", "/assets/tests/fixtures")
    let $in := sd:read-xml($path)
    return validate:rng-report($in, $schema, $compact) update {
      insert node attribute {'file'} { $path } into .
    }
  }</results>
