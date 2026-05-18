#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
md_to_html.py — 팀 스냅샷 MD → HTML 변환기 (외부 의존성 0)

사용법:
  python md_to_html.py <input.md> [output.html]

설계:
  - 공통 템플릿 snapshot_template.html을 변수 치환으로 채움
  - MD 표 / 헤더 / 리스트 / 인용 / 코드 블록 직접 파싱
  - 최상단 "이번 회의에서 확인할 사항" 자동 추출:
      1. §🚨 강사 사전 통지 의제 3건 (✅/❌/△ 마킹 포함)
      2. §🚨 즉시 조치 (있을 때)
      3. §🔒 회의 외 강사 1:1 항목 (있을 때)
  - 정량 지표 자동 추출:
      - 회의 시간 (양식 헤더에서 "실제 시간: M'S\"" 추출)
      - 사전 통지 이행률 (§강사 사전 통지 의제에서)
      - Carry-over 이행률 (§Carry-over에서)
      - 신호등 회고 (§신호등 또는 헤더에서)
"""

import re
import sys
import html
from pathlib import Path
from datetime import datetime


# ----- 정량 지표 추출 -----

def extract_metrics(md_text: str) -> dict:
    """양식 헤더 + 본문에서 v2 정량 지표 추출"""
    m = {
        'time': '미기록',
        'notice': '미측정',
        'carryover': '미측정',
        'light': '미시행',
    }
    # 회의 시간
    t = re.search(r'실제 시간[:\s]*([0-9\']+(?:["\']\d+)?)\s*[\'"]?', md_text)
    if not t:
        t = re.search(r"실제 시간[:\s]*\*\*([0-9'\"]+\s*[가-힣]*)", md_text)
    if t:
        m['time'] = t.group(1).strip()
    if '__분 __초' in m['time'] or '미기록' in m['time']:
        m['time'] = '미기록 (회의 후)'

    # 사전 통지 이행률
    n = re.search(r'사전\s*통지\s*이행률[:\s]*\*?\*?\s*([0-9./]+\s*=?\s*[0-9.%]*[⭐❌]*)', md_text)
    if not n:
        n = re.search(r'이행률[:\s]*\*?\*?\s*([0-9./]+\s*=\s*[0-9.%]+)\s*[⭐❌✅]*[^\n]*사전', md_text)
    if n:
        m['notice'] = n.group(1).strip()[:30]

    # Carry-over 이행률 (회의-내 또는 전체)
    c = re.search(r'(?:회의-내\s*)?(?:이행률|진척률)[:\s]*\*?\*?\s*([0-9./]+\s*=\s*[0-9.%]+)\s*[⭐❌✅]*[^\n]*[Cc]arry', md_text)
    if not c:
        c = re.search(r'(?:Carry-over|carry-over)[^\n]{0,80}([0-9]+/[0-9]+\s*=\s*[0-9.%]+)', md_text)
    if c:
        m['carryover'] = c.group(1).strip()[:30]

    # 신호등 — 매우 좁게: "❌ N일 연속" 또는 "✅" 같은 패턴만
    l = re.search(r'신호등\s*(?:회고|align)?\s*[:\s]*\*?\*?\s*((?:❌|✅|△)\s*(?:\d+일\s*연속)?(?:\s*[가-힣]{0,10})?)', md_text)
    if l:
        m['light'] = l.group(1).strip()[:20]
    else:
        # 신호등 회고 ❌ 패턴 별도 검색
        l2 = re.search(r'신호등[^\n]{0,30}(❌|미시행|시도\s*\d일차)', md_text)
        if l2:
            m['light'] = l2.group(1).strip()[:20]

    return m


# ----- "이번 회의에서 확인할 사항" 자동 추출 -----

def extract_todo_block(md_text: str) -> str:
    """최상단 강조 박스에 들어갈 핵심 액션 추출 — HTML 반환"""
    parts = []

    # 1. 사전 통지 의제 3건
    notice_section = re.search(
        r'##\s*🚨\s*강사 사전 통지 의제(?:.*?)\n(.+?)(?=\n##\s|\n---)',
        md_text, re.DOTALL
    )
    if notice_section:
        items = re.findall(r'^\d+\.\s+(.+?)(?=\n\d+\.|\n→|\n---|\Z)', notice_section.group(1), re.DOTALL | re.MULTILINE)
        if items:
            parts.append('<div><h3 class="font-bold text-red-800 mb-2">🚨 강사 사전 통지 의제 3건 (회의 5분 전 전달)</h3><ol class="list-decimal pl-5 space-y-2">')
            for i, item in enumerate(items[:3], 1):
                # 회의 결과 마킹 추출 (✅/❌/△)
                first_line = item.split('\n')[0].strip()
                # 너무 길면 자름
                if len(first_line) > 200:
                    first_line = first_line[:200] + '…'
                # → 회의 결과 추출
                result = re.search(r'→\s*\*?\*?회의 결과[:\s]*([^\n]+)', item)
                result_html = ''
                if result:
                    res_text = result.group(1)[:150]
                    if '✅' in res_text or '완전 이행' in res_text:
                        badge = '<span class="badge-green">✅ 이행</span>'
                    elif '❌' in res_text or '미언급' in res_text:
                        badge = '<span class="badge-red">❌ 미언급</span>'
                    elif '△' in res_text or '부분' in res_text:
                        badge = '<span class="badge-yellow">△ 부분</span>'
                    else:
                        badge = '<span class="badge-yellow">대기</span>'
                    result_html = f'<div class="mt-1 text-xs">{badge} <span class="text-gray-700">{html.escape(res_text)}</span></div>'
                parts.append(f'<li><div class="text-sm">{html.escape(first_line)}</div>{result_html}</li>')
            parts.append('</ol></div>')

    # 2. 🚨 즉시 조치
    immediate = re.search(
        r'##\s*🚨\s*즉시 조치\s*(?:필요)?(?:.*?)\n(.+?)(?=\n##\s|\n---)',
        md_text, re.DOTALL
    )
    if immediate:
        body = immediate.group(1).strip()
        items = [l.strip() for l in body.split('\n') if l.strip() and not l.strip().startswith('#')]
        items = items[:4]
        if items:
            parts.append('<div><h3 class="font-bold text-red-800 mb-2">⚡ 즉시 조치 필요</h3><ul class="space-y-1">')
            for it in items:
                # 일정 길이 자름
                line = it[:250]
                if len(it) > 250:
                    line += '…'
                parts.append(f'<li class="text-sm">{render_inline(line)}</li>')
            parts.append('</ul></div>')

    # 3. 회의 외 1:1 항목
    one_on_one = re.search(
        r'(?:🔒\s*회의 외 강사 1:1|\#\#\s*회의 외)(?:.*?)\n(.+?)(?=\n##\s|\n---|\Z)',
        md_text, re.DOTALL
    )
    if one_on_one:
        body = one_on_one.group(1).strip()
        items = re.findall(r'^\s*-\s*\[\s*\]\s*(.+?)$', body, re.MULTILINE)
        items = items[:5]
        if items:
            parts.append('<div><h3 class="font-bold text-purple-900 mb-2">🔒 회의 외 강사 1:1 (오늘 17:00 이후 30분)</h3><ul class="list-disc pl-5 space-y-1">')
            for it in items:
                parts.append(f'<li class="text-sm">{render_inline(it[:200])}</li>')
            parts.append('</ul></div>')

    if not parts:
        return '<div class="text-sm text-gray-600">상세 항목 없음 — 본문 펼침 섹션 참조</div>'

    return ''.join(parts)


# ----- "한 줄 요약" + 핵심 인용 추출 -----

def extract_summary(md_text: str) -> str:
    """§🟢 한 줄 요약 섹션의 본문을 HTML로"""
    s = re.search(
        r'##\s*🟢\s*한 줄 요약\s*\n(.+?)(?=\n##\s|\n---)',
        md_text, re.DOTALL
    )
    if not s:
        return '<p class="text-gray-500">요약 없음</p>'
    return md_block_to_html(s.group(1).strip())


# ----- 펼침 섹션 (나머지) -----

def extract_details_sections(md_text: str) -> str:
    """헤딩 별로 details/summary 블록 생성 — 사전 통지/한 줄 요약/즉시 조치는 이미 최상단으로 갔으므로 제외"""

    # 모든 ## 헤더 찾기 (## 뒤에 텍스트)
    headers = list(re.finditer(r'^##\s+(.+?)$', md_text, re.MULTILINE))

    skip_keywords = [
        '강사 사전 통지', '한 줄 요약', '즉시 조치', '저장', '근거 인용',
    ]

    blocks = []
    for i, hdr in enumerate(headers):
        title = hdr.group(1).strip()
        if any(k in title for k in skip_keywords):
            continue

        start = hdr.end()
        end = headers[i+1].start() if i+1 < len(headers) else len(md_text)
        body = md_text[start:end].strip()

        # `---` 만 있으면 건너뜀
        if not body or body == '---':
            continue
        # 끝의 --- 제거
        body = re.sub(r'\n?---\s*$', '', body)

        if not body.strip():
            continue

        # 일부 섹션은 기본 펼침
        default_open = any(k in title for k in ['🎤 회의 시 이렇게', '🌙 내일 AM carry-over', '💡 보조강사 권장 액션', '📊', '✅ 오전'])
        open_attr = ' open' if default_open else ''

        html_body = md_block_to_html(body)
        title_safe = html.escape(title)
        blocks.append(f'<details{open_attr}><summary>{title_safe}</summary><div>{html_body}</div></details>')

    return ''.join(blocks)


# ----- MD 본문 → HTML 변환 -----

def md_block_to_html(text: str) -> str:
    """MD 단위 블록을 HTML로 변환 (헤더 / 표 / 리스트 / 인용 / 코드 / 단락)"""

    lines = text.split('\n')
    out = []
    i = 0
    while i < len(lines):
        line = lines[i]
        # 코드 블록
        if line.strip().startswith('```'):
            lang = line.strip()[3:].strip()
            code_lines = []
            i += 1
            while i < len(lines) and not lines[i].strip().startswith('```'):
                code_lines.append(lines[i])
                i += 1
            i += 1  # 닫는 ``` 건너뜀
            code_html = html.escape('\n'.join(code_lines))
            out.append(f'<pre><code>{code_html}</code></pre>')
            continue

        # 표 (헤더 다음 줄에 |---|---|---| 형태)
        if line.strip().startswith('|') and i+1 < len(lines) and re.match(r'^\s*\|[\s\-|:]+\|\s*$', lines[i+1]):
            table_lines = [line]
            i += 1  # separator
            i += 1
            while i < len(lines) and lines[i].strip().startswith('|'):
                table_lines.append(lines[i])
                i += 1
            out.append(render_table(table_lines))
            continue

        # 헤더
        h_match = re.match(r'^(#{1,4})\s+(.+)$', line)
        if h_match:
            level = len(h_match.group(1))
            text_inline = render_inline(h_match.group(2))
            out.append(f'<h{level}>{text_inline}</h{level}>')
            i += 1
            continue

        # 인용
        if line.strip().startswith('>'):
            quote_lines = []
            while i < len(lines) and (lines[i].strip().startswith('>') or lines[i].strip() == ''):
                if lines[i].strip().startswith('>'):
                    quote_lines.append(lines[i].strip()[1:].strip())
                elif quote_lines:
                    break
                i += 1
            quote_html = render_inline(' '.join(quote_lines))
            out.append(f'<div class="quote">{quote_html}</div>')
            continue

        # 리스트 (- / * / 숫자.)
        ul_match = re.match(r'^\s*[-*]\s+(.+)$', line)
        ol_match = re.match(r'^\s*(\d+)\.\s+(.+)$', line)
        if ul_match or ol_match:
            list_type = 'ol' if ol_match else 'ul'
            list_lines = []
            while i < len(lines):
                if re.match(r'^\s*[-*]\s+(.+)$', lines[i]) or re.match(r'^\s*\d+\.\s+(.+)$', lines[i]):
                    list_lines.append(lines[i])
                    i += 1
                elif lines[i].strip() == '':
                    break
                elif lines[i].startswith('  '):
                    # 들여쓰기 — 이전 라인에 합침
                    if list_lines:
                        list_lines[-1] += ' ' + lines[i].strip()
                    i += 1
                else:
                    break
            items_html = []
            for l in list_lines:
                m = re.match(r'^\s*[-*]\s+(.+)$', l) or re.match(r'^\s*\d+\.\s+(.+)$', l)
                if m:
                    items_html.append(f'<li>{render_inline(m.group(1))}</li>')
            out.append(f'<{list_type}>{"".join(items_html)}</{list_type}>')
            continue

        # 수평선
        if line.strip() == '---':
            out.append('<hr>')
            i += 1
            continue

        # 빈 줄
        if line.strip() == '':
            i += 1
            continue

        # 단락 (다음 빈 줄 또는 특수 줄 만날 때까지)
        para_lines = [line]
        i += 1
        while i < len(lines) and lines[i].strip() != '' and not lines[i].strip().startswith(('#', '|', '>', '-', '*', '```', '---')) and not re.match(r'^\s*\d+\.\s', lines[i]):
            para_lines.append(lines[i])
            i += 1
        para_text = ' '.join(para_lines).strip()
        if para_text:
            out.append(f'<p>{render_inline(para_text)}</p>')

    return ''.join(out)


def render_table(table_lines: list) -> str:
    """MD 표 → HTML 표"""
    rows = []
    for line in table_lines:
        # | col | col | col |
        cells = [c.strip() for c in line.split('|')[1:-1]]
        rows.append(cells)
    if not rows:
        return ''

    out = ['<table>']
    out.append('<thead><tr>')
    for cell in rows[0]:
        out.append(f'<th>{render_inline(cell)}</th>')
    out.append('</tr></thead>')

    if len(rows) > 1:
        out.append('<tbody>')
        for row in rows[1:]:
            out.append('<tr>')
            for cell in row:
                out.append(f'<td>{render_inline(cell)}</td>')
            out.append('</tr>')
        out.append('</tbody>')

    out.append('</table>')
    return ''.join(out)


def render_inline(text: str) -> str:
    """inline MD → HTML: bold, italic, code, link"""
    # 우선 HTML escape 하면 < > 가 사라지므로 직접 처리
    # 1. 코드 (가장 먼저, 그 안의 ** 등은 변환 X)
    code_placeholders = []
    def code_replace(m):
        code_placeholders.append(m.group(1))
        return f'\x00CODE{len(code_placeholders)-1}\x00'
    text = re.sub(r'`([^`]+)`', code_replace, text)

    # 2. 링크 [text](url)
    link_placeholders = []
    def link_replace(m):
        link_placeholders.append((m.group(1), m.group(2)))
        return f'\x00LINK{len(link_placeholders)-1}\x00'
    text = re.sub(r'\[([^\]]+)\]\(([^)]+)\)', link_replace, text)

    # 3. HTML escape
    text = html.escape(text, quote=False)

    # 4. bold **text**
    text = re.sub(r'\*\*([^*]+)\*\*', r'<strong>\1</strong>', text)

    # 5. italic *text* (단 **text** 뒤에 처리)
    text = re.sub(r'(?<!\*)\*([^*\s][^*]*?)\*(?!\*)', r'<em>\1</em>', text)

    # 6. 코드 복원
    for i, code in enumerate(code_placeholders):
        text = text.replace(f'\x00CODE{i}\x00', f'<code>{html.escape(code)}</code>')

    # 7. 링크 복원
    for i, (label, url) in enumerate(link_placeholders):
        label_esc = html.escape(label)
        url_esc = html.escape(url, quote=True)
        text = text.replace(f'\x00LINK{i}\x00', f'<a href="{url_esc}">{label_esc}</a>')

    return text


# ----- 메타 추출 (팀명 / 날짜 / Week) -----

def extract_meta(md_text: str, md_path: Path) -> dict:
    """헤더 H1 + 양식에서 메타 추출"""
    meta = {
        'team': 'Unknown',
        'date': '',
        'slot': '',
        'week': '',
        'window': '',
    }

    # H1: "# 1팀 (Scoffee) PM 일일 체크 — 2026-05-18 PM (Week 3 D1, 월요일)"
    h1 = re.search(r'^#\s+(.+?)$', md_text, re.MULTILINE)
    if h1:
        title = h1.group(1)
        team_match = re.search(r'^([0-9]?팀\s*\([^)]+\)|[^—\-(]+팀)', title)
        if team_match:
            meta['team'] = team_match.group(1).strip()
        date_match = re.search(r'(\d{4}-\d{2}-\d{2})', title)
        if date_match:
            meta['date'] = date_match.group(1)
        slot_match = re.search(r'(AM|PM)', title)
        if slot_match:
            meta['slot'] = slot_match.group(1)
        week_match = re.search(r'(Week \d+(?:\s*D\d+)?[^,)]*)', title)
        if week_match:
            meta['week'] = week_match.group(1).strip()

    # 분석 윈도우
    window = re.search(r'분석 윈도우[:\s]*([^\n]+)', md_text)
    if window:
        meta['window'] = window.group(1).strip()[:80]
    else:
        baseline = re.search(r'\*\*베이스라인\*\*[:\s]*([^\n]+)', md_text)
        if baseline:
            meta['window'] = '베이스라인: ' + baseline.group(1).strip()[:80]

    # 파일명에서 fallback
    if not meta['date']:
        date_match = re.search(r'(\d{6})_(am|pm)', md_path.name)
        if date_match:
            d = date_match.group(1)
            meta['date'] = f'20{d[:2]}-{d[2:4]}-{d[4:6]}'
            meta['slot'] = date_match.group(2).upper()

    return meta


# ----- 메인 변환 함수 -----

def convert(md_path: Path, html_path: Path, template_path: Path):
    md_text = md_path.read_text(encoding='utf-8')
    template = template_path.read_text(encoding='utf-8')

    meta = extract_meta(md_text, md_path)
    metrics = extract_metrics(md_text)
    todo_html = extract_todo_block(md_text)
    summary_html = extract_summary(md_text)
    details_html = extract_details_sections(md_text)

    # 양식 치환
    rendered = template
    rendered = rendered.replace('{{TITLE}}', f"{meta['team']} {meta['date']} {meta['slot']}")
    rendered = rendered.replace('{{TEAM_NAME}}', html.escape(meta['team']))
    rendered = rendered.replace('{{DATE}}', html.escape(meta['date']))
    rendered = rendered.replace('{{SLOT}}', html.escape(meta['slot']))
    rendered = rendered.replace('{{WEEK_INFO}}', html.escape(meta['week']))
    rendered = rendered.replace('{{WINDOW_INFO}}', render_inline(meta['window']))
    rendered = rendered.replace('{{METRIC_TIME}}', html.escape(metrics['time']))
    rendered = rendered.replace('{{METRIC_NOTICE}}', html.escape(metrics['notice']))
    rendered = rendered.replace('{{METRIC_CARRYOVER}}', html.escape(metrics['carryover']))
    rendered = rendered.replace('{{METRIC_LIGHT}}', html.escape(metrics['light']))
    rendered = rendered.replace('{{TODO_BLOCK}}', todo_html)
    rendered = rendered.replace('{{SUMMARY_BLOCK}}', summary_html)
    rendered = rendered.replace('{{DETAILS_BLOCKS}}', details_html)
    rendered = rendered.replace('{{MD_PATH}}', str(md_path.name))
    rendered = rendered.replace('{{GENERATED_AT}}', datetime.now().strftime('%Y-%m-%d %H:%M'))

    html_path.write_text(rendered, encoding='utf-8')
    print(f'OK  {md_path.name} -> {html_path.name}', flush=True)


# ----- CLI -----

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('Usage: python md_to_html.py <input.md> [output.html]')
        sys.exit(1)

    md_path = Path(sys.argv[1])
    if not md_path.exists():
        print(f'File not found: {md_path}')
        sys.exit(1)

    html_path = Path(sys.argv[2]) if len(sys.argv) > 2 else md_path.with_suffix('.html')
    template_path = Path(__file__).parent / 'snapshot_template.html'

    if not template_path.exists():
        print(f'Template not found: {template_path}')
        sys.exit(1)

    convert(md_path, html_path, template_path)
