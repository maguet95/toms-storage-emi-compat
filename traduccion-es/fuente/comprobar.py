import json, re, sys
sys.path.insert(0, '.')
from es import ES
B = chr(92)
en = json.load(open('en_us.json', encoding='utf-8'))
faltan = [k for k in en if k not in ES]; sobran = [k for k in ES if k not in en]
print('claves en:', len(en), 'es:', len(ES), 'faltan:', faltan, 'sobran:', sobran)
ph = lambda s: sorted(re.findall(r'%(?:\d+\$)?[sd%]', s))
for k in en:
    if k in ES:
        if ph(en[k]) != ph(ES[k]): print('MARCADORES', k, ph(en[k]), ph(ES[k]))
        if en[k].count(B) != ES[k].count(B): print('SALTOS-B', k, en[k].count(B), ES[k].count(B))
        if en[k].count(chr(10)) != ES[k].count(chr(10)): print('SALTOS-N', k)
