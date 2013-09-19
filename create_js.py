#!/usr/bin/env python

import argparse
import json
import sys
import codecs
import os

allowed_fig_ext = [".png",".jpg",".jpeg", ".gif"]

def make_fid(abstract):
    identifier = "%03d" % abstract['figid']
    return identifier


def have_fig(figid, imglist):
    for ext in allowed_fig_ext:
        figpath = figid + ext
        if figpath in imglist:
            sys.stderr.write('Found: %s\n' % figpath)
            return figpath
    return None

def check_figure(abstract, imglist):
    if not abstract.has_key('caption') or len(abstract['caption']) == 0:
        return None

    figid = make_fid(abstract)
    img = have_fig(figid, imglist)
    if img is None:
        sys.stderr.write('MISSING FIG: %s' % figid)
    return img

def main():
    parser = argparse.ArgumentParser(description='Convert abstracts (json) to js')
    parser.add_argument('input', type=str, default=sys.stdin)

    args = parser.parse_args()
    fd = codecs.open(args.input, encoding='utf-8')
    data = fd.read()

    abstracts = json.loads(data)

    toRemoveList = ['dummy', 'state', 'frontid', 'frontsubid', 'submitter', 'session', 'nfigures']
    imglist = os.listdir('figures')
    for abstract in abstracts:
        for toRemove in toRemoveList:
            abstract.pop(toRemove, None)

        img = check_figure(abstract, imglist)
        if img:
            abstract.pop('figid', None)
            abstract['figpath'] = img


    js = u'abstracts = ' + json.dumps(abstracts) + ';'
    sys.stdout.write(js.encode('utf-8'))

if __name__ == '__main__':
    main()