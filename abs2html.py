#!/usr/bin/env python

import argparse
import json
import sys
import codecs

abs_header_str = u'''
<div id ="aid">%(id)d</div>
<div id="topic">%(topic)s</div>
<div><h1 id="title">%(title)s</h1></div>
'''

doc_header_str = u'''
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<link rel=\"stylesheet\" type=\"text/css\" href=\"Abstract.css\" />
<meta name='viewport' content='initial-scale=1.0,maximum-scale=10.0'/>
</head><body>
'''

def format_abs(abstract):
#    html  = u'<div id ="aid">%(id)d</div>' % abstract
#    html += u'<div id="topic">%(topic)s</div>' % abstract
#    html += u'<div><h1 id="title">%@</h2></div>' % abstract
    html = abs_header_str % (abstract)

    af_map = { int(af['index']) : af['address'] for af in abstract['affiliations']}

    html += u'<div><h2 id=\"author\">'
    for idx, author in enumerate(abstract['authors']):
        html += author['name']
        html += u'<sup id="epithat">%s</sup><br/>' % author['epithet']
        #for afidx, af in enumerate(abstract['affiliations']):
        #    html += '%s%s' % ([',', ' '][afidx == 0], af['index'])
        #html += '>'

    html += u'</h2></div><div id=\"affiliations\"><ol>'
    for idx in xrange(0, len(af_map)):
        html += u'<li>%s</li>' % af_map[idx+1]

    html += u'</ol>'
    html += u'</div><br/>'

    html += u'</h2></div><br/>'
    html += u'<div><p id=\"abstract\">%(abstract)s</p></div>' % abstract

    if abstract.has_key('acknowledgements'):
        html += u'<div class=\"appendix\"><h4>Acknowledgements</h4><p>%s</p></div>' % abstract['acknowledgements']

    if abstract.has_key('refs'):
        html += u'<div class=\"appendix\"><p><h4>References</h4><p>%s</p></div>' % abstract['refs']

    return html

def main():
    parser = argparse.ArgumentParser(description='Convert abstracts (json) to HTML')
    parser.add_argument('input', type=str, default=sys.stdin)

    args = parser.parse_args()
    fd = codecs.open(args.input, encoding='utf-8')
    data = fd.read()

    abstracts = json.loads(data)

    doc = doc_header_str
    for abstract in abstracts:
        doc += format_abs(abstract)
    doc += u'</body></html>'

    sys.stdout.write(doc.encode('utf-8'))

if __name__ == '__main__':
    main()
