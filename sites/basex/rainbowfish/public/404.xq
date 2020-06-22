xquery version "3.1";

declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";

declare variable $assets-path as map(*) external;
declare variable $debug as xs:boolean external;

declare option output:method 'html';
declare option output:html-version '5.0';

return
  <html>
    <style>
    <![CDATA[
    body {
      font-family: "Segoe UI";
      font-size: 2rem;
    }
    .info {
      border: 1px solid black;
      min-width: 30rem;
    }
    .info tr:nth-child(odd) {
      background-color: #efe;
    }
    .info td {
      padding: 1rem
    }
    .param {
      font-weight: bold;
    }
    ]]>
    </style>
    <body>
      <h1>404</h1>
      {
        if ($debug) then
        return
          <table class="info">
          {
            for $key in map:keys($info)
            return
              <tr>
                <td class="param">{$key}</td>
                <td class="value">{$info($key)}</td>
              </tr>
          }
          </table>
      }
    </body>
  </html>