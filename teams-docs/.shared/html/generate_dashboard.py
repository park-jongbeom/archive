#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
generate_dashboard.py — 코호트 메타 대시보드 HTML 생성

사용법:
  python generate_dashboard.py [YYMMDD] [am|pm]
  (기본: 가장 최근 스냅샷 자동 검색)

기능:
  - 1·2·3팀 최신 스냅샷 4개 (AM·PM)을 한 화면에 비교
  - 정량 지표 비교 표 (회의 시간, 사전 통지 이행률, Carry-over 이행률, 신호등)
  - 팀별 한 줄 요약 카드
  - 팀별 "이번 회의에서 확인할 사항" 압축
  - 각 스냅샷 HTML로 링크
"""

import re
import sys
import html as html_lib
from pathlib import Path
from datetime import datetime

# md_to_html.py 의 함수들을 재사용
sys.path.insert(0, str(Path(__file__).parent))
from md_to_html import extract_metrics, extract_meta, render_inline


TEAMS = ['1team', '2team', '3team']
TEAM_NAMES = {
    '1team': '1팀 Scoffee',
    '2team': '2팀 Umma',
    '3team': '3팀 BBip (워터폴)',
}
TEAM_COLORS = {
    '1team': 'blue',
    '2team': 'green',
    '3team': 'purple',
}


def extract_one_line_summary(md_text: str) -> str:
    """§🟢 한 줄 요약 첫 줄만 추출 (180자)"""
    s = re.search(r'##\s*🟢\s*한 줄 요약\s*\n+\s*(.+?)(?=\n\n|\n##|\n---)', md_text, re.DOTALL)
    if not s:
        return '요약 없음'
    text = s.group(1).strip()
    # 첫 단락만
    first_para = text.split('\n\n')[0]
    # 너무 길면 자름
    if len(first_para) > 250:
        first_para = first_para[:250] + '…'
    return first_para


def extract_top_actions(md_text: str) -> list:
    """§🚨 강사 사전 통지 의제 3건 + 이행 결과 (요약)"""
    section = re.search(
        r'##\s*🚨\s*강사 사전 통지 의제(?:.*?)\n(.+?)(?=\n##\s|\n---)',
        md_text, re.DOTALL
    )
    if not section:
        return []

    items = re.findall(
        r'^\d+\.\s+(.+?)(?=\n\d+\.|\n→|\n---|\Z)',
        section.group(1), re.DOTALL | re.MULTILINE
    )

    result = []
    for item in items[:3]:
        # 첫 줄만
        first_line = item.split('\n')[0].strip()
        if len(first_line) > 130:
            first_line = first_line[:130] + '…'
        # 결과 마킹
        res = re.search(r'→\s*\*?\*?회의 결과[:\s]*([^\n]+)', item)
        status = '대기'
        badge_cls = 'bg-gray-200 text-gray-700'
        if res:
            res_text = res.group(1)
            if '✅' in res_text or '완전 이행' in res_text or 'MAJOR' in res_text:
                status = '✅ 이행'
                badge_cls = 'bg-green-100 text-green-800'
            elif '❌' in res_text or '미언급' in res_text:
                status = '❌ 미언급'
                badge_cls = 'bg-red-100 text-red-800'
            elif '△' in res_text or '부분' in res_text:
                status = '△ 부분'
                badge_cls = 'bg-yellow-100 text-yellow-800'
        result.append({'text': first_line, 'status': status, 'badge_cls': badge_cls})
    return result


def find_latest_snapshots(date: str, slot: str, base: Path) -> dict:
    """팀별 최신 스냅샷 경로 반환"""
    snapshots = {}
    for team in TEAMS:
        path = base / team / 'snapshots' / f'{date}_{slot}.md'
        if path.exists():
            snapshots[team] = path
    return snapshots


def generate(date: str, slot: str, base: Path, out_path: Path):
    snapshots = find_latest_snapshots(date, slot, base)
    if not snapshots:
        print(f'No snapshots found for {date}_{slot}')
        return

    # 데이터 수집
    team_data = {}
    for team, path in snapshots.items():
        md_text = path.read_text(encoding='utf-8')
        team_data[team] = {
            'meta': extract_meta(md_text, path),
            'metrics': extract_metrics(md_text),
            'summary': extract_one_line_summary(md_text),
            'actions': extract_top_actions(md_text),
            'snapshot_url': f'../{team}/snapshots/{date}_{slot}.html',
        }

    # HTML 생성
    yyyy_mm_dd = f'20{date[:2]}-{date[2:4]}-{date[4:6]}'
    title = f'코호트 대시보드 — {yyyy_mm_dd} {slot.upper()}'

    html = f'''<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>{title}</title>
<script src="https://cdn.tailwindcss.com"></script>
<style>
  body {{ font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Apple SD Gothic Neo", "Malgun Gothic", sans-serif; }}
  table {{ width: 100%; border-collapse: collapse; font-size: 0.85rem; }}
  th {{ background: #f3f4f6; padding: 0.75rem; text-align: left; border: 1px solid #d1d5db; font-weight: 600; }}
  td {{ padding: 0.75rem; border: 1px solid #e5e7eb; vertical-align: top; }}
  .team-card {{ transition: transform 0.15s; }}
  .team-card:hover {{ transform: translateY(-2px); box-shadow: 0 8px 16px rgba(0,0,0,0.08); }}
</style>
</head>
<body class="bg-gray-50 text-gray-900">

<div class="max-w-7xl mx-auto px-4 py-6">

  <header class="mb-6">
    <h1 class="text-3xl font-bold text-gray-900 mb-1">📊 코호트 대시보드</h1>
    <div class="text-sm text-gray-600">{yyyy_mm_dd} {slot.upper()} · 안드로이드 부트캠프 6기 · {len(snapshots)}/3팀 스냅샷 적재</div>
  </header>

  <!-- ===== 정량 지표 비교 표 ===== -->
  <section class="bg-white rounded-xl p-5 mb-6 shadow-sm border border-gray-200">
    <h2 class="text-xl font-bold mb-3 text-gray-900 border-b-2 border-gray-200 pb-2">📊 v2 정량 지표 비교</h2>
    <table>
      <thead>
        <tr>
          <th>지표</th>
'''
    for team in snapshots.keys():
        team_name = TEAM_NAMES[team]
        html += f'          <th>{team_name}</th>\n'
    html += '''        </tr>
      </thead>
      <tbody>
'''
    rows = [
        ('회의 시간', 'time'),
        ('사전 통지 이행률', 'notice'),
        ('Carry-over 이행률', 'carryover'),
        ('신호등 회고', 'light'),
    ]
    for label, key in rows:
        html += f'        <tr><td class="font-semibold">{label}</td>'
        for team in snapshots.keys():
            val = team_data[team]['metrics'].get(key, '-')
            html += f'<td>{html_lib.escape(val)}</td>'
        html += '</tr>\n'

    html += '''      </tbody>
    </table>
  </section>

  <!-- ===== 팀별 카드 ===== -->
  <section class="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-6">
'''
    for team, data in team_data.items():
        team_name = TEAM_NAMES[team]
        color = TEAM_COLORS[team]
        actions_html = ''
        if data['actions']:
            actions_html = '<div class="space-y-2 mb-3">'
            for i, act in enumerate(data['actions'], 1):
                actions_html += f'''
                <div class="text-xs bg-gray-50 rounded p-2 border-l-2 border-{color}-400">
                  <div class="flex items-start gap-2">
                    <span class="font-bold text-gray-500">{i}.</span>
                    <div class="flex-1">
                      <div class="text-gray-800">{html_lib.escape(act["text"])}</div>
                      <span class="inline-block mt-1 px-2 py-0.5 rounded text-xs font-semibold {act["badge_cls"]}">{html_lib.escape(act["status"])}</span>
                    </div>
                  </div>
                </div>'''
            actions_html += '</div>'

        summary_html = render_inline(data['summary'])

        html += f'''
    <div class="team-card bg-white rounded-xl p-5 shadow-sm border-t-4 border-{color}-500">
      <div class="flex items-baseline justify-between mb-3">
        <h3 class="text-lg font-bold text-{color}-900">{team_name}</h3>
        <a href="{data["snapshot_url"]}" class="text-xs text-{color}-600 hover:text-{color}-800 underline">상세 →</a>
      </div>
      <div class="text-xs text-gray-500 mb-3">{html_lib.escape(data["meta"]["week"])}</div>
      <div class="text-sm mb-4 text-gray-700 leading-relaxed">{summary_html}</div>
      <div class="text-xs font-semibold text-gray-500 mb-2 border-t pt-3">🚨 강사 사전 통지 의제 결과</div>
      {actions_html}
    </div>
'''

    html += f'''
  </section>

  <footer class="mt-8 pt-4 border-t border-gray-200 text-xs text-gray-500 text-center">
    생성: {datetime.now().strftime("%Y-%m-%d %H:%M")} · 데이터 source: <code>teams-docs/&lt;X&gt;team/snapshots/{date}_{slot}.md</code>
  </footer>

</div>

</body>
</html>
'''
    out_path.write_text(html, encoding='utf-8')
    print(f'OK  dashboard -> {out_path}')


if __name__ == '__main__':
    base = Path(__file__).parent.parent.parent  # teams-docs/
    date = sys.argv[1] if len(sys.argv) > 1 else datetime.now().strftime('%y%m%d')
    slot = sys.argv[2] if len(sys.argv) > 2 else ('pm' if datetime.now().hour >= 12 else 'am')

    out_dir = base / '.shared' / 'html'
    out_path = out_dir / f'dashboard_{date}_{slot}.html'
    generate(date, slot, base, out_path)

    # 가장 최신은 dashboard.html 로도 심볼릭 (Windows에서는 단순 복사)
    latest = out_dir / 'dashboard.html'
    latest.write_text(out_path.read_text(encoding='utf-8'), encoding='utf-8')
    print(f'OK  latest -> {latest}')
