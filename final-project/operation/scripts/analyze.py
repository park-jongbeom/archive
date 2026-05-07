"""
analyze.py — GitHub + Status JSON + PDF 폴더에서 지표 추출 + 신호등 부여

사용법:
    python scripts/analyze.py --week N

입력:
    snapshots/week-N/팀명_github.json    (Issue/PR 메타)
    snapshots/week-N/팀명_status.json    (Weekly Status Issue 파싱)
    snapshots/week-N/팀명_pdf/           (강사가 인쇄한 노션 PDF 폴더, 존재 여부만 점검)

출력:
    snapshots/week-N/_summary.json
    (Claude가 이걸 읽고 reports/week-N.md 작성)
"""
from __future__ import annotations

import argparse
import json
import sys
from datetime import datetime, timezone, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SNAPSHOTS_DIR = ROOT / "snapshots"

PDF_REQUIRED_PATTERNS = [
    "01_brief.pdf",
    "06_decision_log.pdf",
    "07_retro.pdf",
]
PDF_OPTIONAL_PREFIXES = [
    "02_flow",  # 02_flow_*.pdf
    "05_demo",  # 05_demo_*.pdf
]

EVERYONE_WORDS = ["모든 사람", "모든 사용자", "누구나", "전 국민"]


# ─────────────────────────────────────────────────────────
# PDF 폴더 점검
# ─────────────────────────────────────────────────────────

def check_pdf_folder(team_name: str, week: int) -> dict:
    pdf_dir = SNAPSHOTS_DIR / f"week-{week}" / f"{team_name}_pdf"
    if not pdf_dir.exists():
        return {
            "exists": False,
            "missing_required": PDF_REQUIRED_PATTERNS[:],
            "found": [],
        }
    found = sorted(p.name for p in pdf_dir.iterdir() if p.is_file() and p.suffix.lower() == ".pdf")
    missing = [r for r in PDF_REQUIRED_PATTERNS if r not in found]
    has_flow = any(name.startswith("02_flow") for name in found)
    return {
        "exists": True,
        "found": found,
        "missing_required": missing,
        "has_flow_pdf": has_flow,
    }


# ─────────────────────────────────────────────────────────
# GitHub 메트릭
# ─────────────────────────────────────────────────────────

def parse_iso(s: str | None):
    if not s:
        return None
    try:
        return datetime.fromisoformat(s.replace("Z", "+00:00"))
    except Exception:
        return None


def github_metrics(meta: dict) -> dict:
    issues = [i for i in meta.get("issues", []) if not i.get("is_weekly_status")]
    pulls = meta.get("pulls", [])

    ac_counts = [i.get("ac_count", 0) for i in issues]
    avg_ac = round(sum(ac_counts) / len(ac_counts), 2) if ac_counts else 0.0

    pr_total = len(pulls)
    pr_merged = sum(1 for p in pulls if p.get("merged"))
    pr_with_closes = sum(1 for p in pulls if p.get("has_closes_link"))
    closes_rate = round(pr_with_closes / pr_total * 100, 1) if pr_total else 0.0

    sla_total = 0
    sla_within = 0
    for p in pulls:
        created = parse_iso(p.get("created_at"))
        first = parse_iso(p.get("first_review_at"))
        if not created:
            continue
        sla_total += 1
        if first and (first - created) <= timedelta(hours=24):
            sla_within += 1
    sla_rate = round(sla_within / sla_total * 100, 1) if sla_total else None

    # WIP: assignee별 open issue 수 (weekly-status 제외)
    wip_count: dict[str, int] = {}
    for i in issues:
        if i.get("state") != "open":
            continue
        for a in i.get("assignees", []) or []:
            wip_count[a] = wip_count.get(a, 0) + 1
    wip_violators = {k: v for k, v in wip_count.items() if v >= 3}

    return {
        "issue_count": len(issues),
        "avg_ac": avg_ac,
        "pr_total": pr_total,
        "pr_merged": pr_merged,
        "closes_link_rate_pct": closes_rate,
        "sla_24h_rate_pct": sla_rate,
        "sla_sample_size": sla_total,
        "wip_violators": wip_violators,
    }


# ─────────────────────────────────────────────────────────
# 신호등
# ─────────────────────────────────────────────────────────

def signals_for(team_name: str, week: int, status: dict, gh: dict, pdf: dict) -> list[tuple[str, str]]:
    sig: list[tuple[str, str]] = []

    # 1. Status Issue 미작성
    if status.get("_missing"):
        sig.append(("🔴", "Weekly Status Issue 미작성 — 자기 점검 절차 멈춤 (가이드 § 12)"))
        # Status 없으면 자기보고 지표는 평가 불가, GitHub만으로 진단 진행

    parsed = status.get("parsed") or {}

    # 2. 자기보고 기반
    if status.get("issue_number"):
        if parsed.get("brief_written") is False and week >= 1:
            sig.append(("🔴", "Brief 미작성 (자기보고) — 가이드 § 4-4"))
        if parsed.get("wont_count") == 0:
            sig.append(("🔴", "Won't 항목 0개 — 범위 폭발 위험 (가이드 § 4-4)"))
        if (parsed.get("must_count") or 0) >= 8:
            sig.append(("🟡", f"Must {parsed.get('must_count')}개 — 과다 (가이드 § 4-4)"))
        elif (parsed.get("must_count") or 0) >= 6:
            sig.append(("🟡", f"Must {parsed.get('must_count')}개 — 다소 과다 (가이드 § 4-4)"))
        flow_n = parsed.get("flow_count")
        if flow_n is not None and flow_n >= 4:
            sig.append(("🟡", f"Flow {flow_n}개 — 1~2개로 좁혀야 (가이드 § 5-2-1)"))
        if parsed.get("demo_written") is False and week >= 1:
            sig.append(("🟡", "Demo Scenario 미작성 (자기보고) — 가이드 § 9"))
        target = parsed.get("target_line") or ""
        if any(w in target for w in EVERYONE_WORDS):
            sig.append(("🟡", "타깃에 '모든/누구나' 표현 — 타깃 미정의 (가이드 § 4-4)"))

    # 3. GitHub 기반
    if week >= 1 and gh.get("issue_count", 0) == 0:
        sig.append(("🟡", "GitHub Issue 0개 — 작업 분해 안 됨 (가이드 § 6)"))
    avg_ac = gh.get("avg_ac", 0)
    if gh.get("issue_count", 0) > 0:
        if avg_ac < 1:
            sig.append(("🔴", f"Issue 평균 AC {avg_ac}개 — AC 거의 없음 (가이드 § 6-1)"))
        elif avg_ac >= 8:
            sig.append(("🟡", f"Issue 평균 AC {avg_ac}개 — Issue 과대 (가이드 § 6-1)"))
    closes = gh.get("closes_link_rate_pct", 0)
    if gh.get("pr_total", 0) > 0 and closes < 80:
        sig.append(("🟡", f"PR Closes # 연결률 {closes}% — < 80% (가이드 § 7-1)"))
    sla = gh.get("sla_24h_rate_pct")
    if sla is not None and gh.get("sla_sample_size", 0) >= 2:
        if sla < 50:
            sig.append(("🔴", f"24h 1차 리뷰 SLA {sla}% — < 50% (가이드 § 7-1)"))
        elif sla < 80:
            sig.append(("🟡", f"24h 1차 리뷰 SLA {sla}% — < 80% (가이드 § 7-1)"))
    if week >= 2 and gh.get("pr_merged", 0) == 0:
        sig.append(("🔴", "PR 머지 0개 — PR 흐름 미가동 (가이드 § 7)"))
    elif week == 1 and gh.get("pr_merged", 0) == 0:
        sig.append(("🟡", "Week 1 PR 머지 0개 — 최소 데모 1개 점검 필요 (가이드 § 4)"))
    wip = gh.get("wip_violators") or {}
    if wip:
        names = ", ".join(f"{k}({v})" for k, v in wip.items())
        sig.append(("🟡", f"WIP 위반(동시 In Progress 3+): {names} (가이드 § 7-1)"))

    # 4. PDF 누락 (수동 인쇄 점검 — 강사 본인에게 알림)
    if not pdf.get("exists"):
        sig.append(("🟡", f"노션 PDF 폴더 미생성 — {team_name}_pdf/ 인쇄 필요"))
    else:
        miss = pdf.get("missing_required") or []
        if miss:
            sig.append(("🟡", f"필수 PDF 누락: {', '.join(miss)} — 강사 인쇄 필요"))
        if not pdf.get("has_flow_pdf"):
            sig.append(("🟡", "02_flow_*.pdf 누락 — Flow 문서 인쇄 필요"))

    return sig


def status_from_signals(sig: list[tuple[str, str]]) -> str:
    if any(s[0] == "🔴" for s in sig):
        return "🔴"
    if sig:
        return "🟡"
    return "🟢"


# ─────────────────────────────────────────────────────────
# Main
# ─────────────────────────────────────────────────────────

def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--week", type=int, required=True)
    args = parser.parse_args()

    week_dir = SNAPSHOTS_DIR / f"week-{args.week}"
    if not week_dir.exists():
        sys.exit(f"{week_dir} 없음. fetch_github.py 먼저 실행.")

    # 팀 목록은 _github.json 파일들로 추출
    team_files = sorted(week_dir.glob("*_github.json"))
    if not team_files:
        sys.exit("_github.json 파일 없음. fetch_github.py 먼저 실행.")

    teams_summary = []
    for gf in team_files:
        team_name = gf.name.replace("_github.json", "")
        meta = json.loads(gf.read_text(encoding="utf-8"))
        sf = week_dir / f"{team_name}_status.json"
        status = json.loads(sf.read_text(encoding="utf-8")) if sf.exists() else {"_missing": "status JSON 없음"}
        gh = github_metrics(meta)
        pdf = check_pdf_folder(team_name, args.week)
        sig = signals_for(team_name, args.week, status, gh, pdf)

        teams_summary.append({
            "team": team_name,
            "status": status_from_signals(sig),
            "signals": sig,
            "self_report": status.get("parsed") if status.get("issue_number") else None,
            "self_report_present": bool(status.get("issue_number")),
            "github": gh,
            "pdf": pdf,
        })

    summary = {
        "week": args.week,
        "analyzed_at": datetime.now(timezone.utc).isoformat(),
        "teams": teams_summary,
    }
    out = week_dir / "_summary.json"
    out.write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"[analyze] {len(teams_summary)}팀 → {out}")
    for t in teams_summary:
        sr = t["self_report"] or {}
        print(
            f"  {t['status']} {t['team']:8s} | "
            f"Status={'O' if t['self_report_present'] else 'X'} "
            f"Brief={'O' if sr.get('brief_written') else ('X' if sr.get('brief_written') is False else '?')} "
            f"Won't={sr.get('wont_count', '?')} "
            f"Flow={sr.get('flow_count', '?')} "
            f"Issues={t['github']['issue_count']} "
            f"PR머지={t['github']['pr_merged']} "
            f"PDF={'O' if t['pdf']['exists'] and not t['pdf'].get('missing_required') else 'X'}"
        )


if __name__ == "__main__":
    main()
