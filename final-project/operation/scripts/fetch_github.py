"""
fetch_github.py — 팀별 GitHub 메타데이터 + Weekly Status Issue 수집

사용법:
    python scripts/fetch_github.py --week N
    python scripts/fetch_github.py --week N --team 1팀

출력:
    snapshots/week-N/팀명_github.json    (Issue/PR 메타데이터)
    snapshots/week-N/팀명_status.json    (해당 주차 Weekly Status Issue 파싱)

요구:
    .env 에 GITHUB_TOKEN
    teams.yaml 의 각 팀에 github.repo
"""
from __future__ import annotations

import argparse
import json
import os
import re
import sys
from datetime import datetime, timezone
from pathlib import Path

import yaml
from dotenv import load_dotenv

ROOT = Path(__file__).resolve().parent.parent
TEAMS_FILE = ROOT / "teams.yaml"
SNAPSHOTS_DIR = ROOT / "snapshots"

CLOSES_PATTERN = re.compile(r"(?i)\b(closes|fixes|resolves)\s+#\d+")
AC_CHECKBOX_PATTERN = re.compile(r"^\s*-\s*\[[ xX]\]", re.MULTILINE)
STATUS_TITLE_PATTERN = re.compile(r"\[Status\].*?Week\s*(\d+)", re.IGNORECASE)


def load_teams() -> list[dict]:
    if not TEAMS_FILE.exists():
        sys.exit(f"{TEAMS_FILE} 없음.")
    with open(TEAMS_FILE, "r", encoding="utf-8") as f:
        data = yaml.safe_load(f) or {}
    return data.get("teams") or []


# ─────────────────────────────────────────────────────────
# Status Issue 본문 파싱
# ─────────────────────────────────────────────────────────

def normalize(s: str) -> str:
    return (s or "").replace("’", "'").replace("‘", "'")


def extract_checkbox(body: str, label_kw: list[str]) -> bool | None:
    """본문에서 체크박스의 체크 여부 추출. 항목 자체가 없으면 None."""
    body = normalize(body)
    for line in body.split("\n"):
        s = line.strip()
        if not s.startswith("- ["):
            continue
        if any(kw in s for kw in label_kw):
            return s[3].lower() == "x"
    return None


def extract_number(body: str, label_kw: list[str]) -> int | None:
    """'- Must: 4' 같은 숫자 필드 추출. '__' 또는 미작성은 None."""
    body = normalize(body)
    for line in body.split("\n"):
        s = line.strip()
        if not (s.startswith("- ") or s.startswith("* ")):
            continue
        if any(kw in s for kw in label_kw):
            m = re.search(r":\s*(\d+|__|\?)\s*$", s)
            if not m:
                continue
            v = m.group(1)
            if v in ("__", "?"):
                return None
            return int(v)
    return None


def extract_text_after(body: str, label_kw: list[str]) -> str | None:
    """'- 타깃 한 줄: ...' 같은 한 줄 텍스트 필드 추출."""
    body = normalize(body)
    for line in body.split("\n"):
        s = line.strip()
        if not (s.startswith("- ") or s.startswith("* ")):
            continue
        if any(kw in s for kw in label_kw):
            m = re.search(r":\s*(.+)$", s)
            if not m:
                continue
            text = m.group(1).strip()
            if text in ("", "__", "?"):
                return None
            return text
    return None


def parse_status_body(body: str) -> dict:
    """Weekly Status Issue 본문 → 정규화된 dict."""
    body = body or ""
    return {
        "brief_written": extract_checkbox(body, ["Brief 작성"]),
        "demo_written": extract_checkbox(body, ["데모 시나리오 작성"]),
        "must_count": extract_number(body, ["Must"]),
        "should_count": extract_number(body, ["Should"]),
        "wont_count": extract_number(body, ["Won't", "Wont", "Will not"]),
        "flow_count": extract_number(body, ["확정 Flow 수"]),
        "decision_count": extract_number(body, ["이번 주 결정 수"]),
        "target_line": extract_text_after(body, ["타깃 한 줄"]),
        "core_goal": extract_text_after(body, ["핵심 목표 한 줄"]),
        "biggest_decision": extract_text_after(body, ["가장 큰 결정"]),
        "demo_one_line": extract_text_after(body, ["이번 주 데모 한 줄"]),
        "weekly_one_liner": extract_text_after(body, ["이번 주 한 줄"]),
    }


# ─────────────────────────────────────────────────────────
# GitHub Fetch
# ─────────────────────────────────────────────────────────

def fetch_team_github(gh, team: dict, week: int) -> tuple[dict, dict]:
    """팀별로 (메타 dict, status dict) 반환."""
    g_cfg = team.get("github") or {}
    repo_full = g_cfg.get("repo")
    if not repo_full:
        return ({"team": team.get("name"), "_skip": "github.repo 미등록"}, {})

    repo = gh.get_repo(repo_full)
    issues_all = list(repo.get_issues(state="all"))
    issues = [i for i in issues_all if i.pull_request is None]
    pulls = [i for i in issues_all if i.pull_request is not None]

    issue_data = []
    status_issue = None
    for i in issues:
        body = i.body or ""
        ac_count = len(AC_CHECKBOX_PATTERN.findall(body))
        labels = [l.name for l in i.labels]
        is_status = "weekly-status" in labels
        if is_status:
            m = STATUS_TITLE_PATTERN.search(i.title or "")
            if m and int(m.group(1)) == week:
                status_issue = i  # 해당 주차 Status

        issue_data.append({
            "number": i.number,
            "title": i.title,
            "state": i.state,
            "labels": labels,
            "assignees": [a.login for a in i.assignees],
            "ac_count": ac_count,
            "is_weekly_status": is_status,
            "created_at": i.created_at.isoformat() if i.created_at else None,
            "closed_at": i.closed_at.isoformat() if i.closed_at else None,
        })

    pr_data = []
    for p in pulls:
        body = p.body or ""
        has_closes = bool(CLOSES_PATTERN.search(body))
        pr = repo.get_pull(p.number)
        first_review_at = None
        try:
            reviews = list(pr.get_reviews())
            review_times = [r.submitted_at for r in reviews if r.submitted_at]
            if review_times:
                first_review_at = min(review_times).isoformat()
        except Exception:
            pass
        pr_data.append({
            "number": p.number,
            "title": p.title,
            "state": p.state,
            "merged": pr.merged,
            "has_closes_link": has_closes,
            "created_at": p.created_at.isoformat() if p.created_at else None,
            "first_review_at": first_review_at,
            "merged_at": pr.merged_at.isoformat() if pr.merged_at else None,
            "author": p.user.login if p.user else None,
        })

    meta = {
        "team": team.get("name"),
        "repo": repo_full,
        "fetched_at": datetime.now(timezone.utc).isoformat(),
        "issues": issue_data,
        "pulls": pr_data,
    }

    if status_issue:
        parsed = parse_status_body(status_issue.body or "")
        status = {
            "team": team.get("name"),
            "week": week,
            "issue_number": status_issue.number,
            "issue_url": status_issue.html_url,
            "created_at": status_issue.created_at.isoformat() if status_issue.created_at else None,
            "raw_body": status_issue.body,
            "parsed": parsed,
        }
    else:
        status = {
            "team": team.get("name"),
            "week": week,
            "issue_number": None,
            "_missing": "weekly-status Issue 미작성",
        }

    return meta, status


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--week", type=int, required=True)
    parser.add_argument("--team", type=str, default=None)
    args = parser.parse_args()

    load_dotenv(ROOT / ".env")
    token = os.environ.get("GITHUB_TOKEN")
    if not token:
        sys.exit("GITHUB_TOKEN 미설정.")

    try:
        from github import Github
    except ImportError:
        sys.exit("PyGithub 미설치. pip install PyGithub")

    gh = Github(token)
    teams = load_teams()
    if args.team:
        teams = [t for t in teams if t.get("name") == args.team]
    if not teams:
        sys.exit("teams.yaml 비어있거나 매칭 팀 없음.")

    out_dir = SNAPSHOTS_DIR / f"week-{args.week}"
    out_dir.mkdir(parents=True, exist_ok=True)

    for team in teams:
        name = team.get("name", "unknown")
        print(f"[fetch_github] {name} ...", flush=True)
        try:
            meta, status = fetch_team_github(gh, team, args.week)
            (out_dir / f"{name}_github.json").write_text(
                json.dumps(meta, ensure_ascii=False, indent=2),
                encoding="utf-8",
            )
            (out_dir / f"{name}_status.json").write_text(
                json.dumps(status, ensure_ascii=False, indent=2),
                encoding="utf-8",
            )
            tag = "✓ status" if status.get("issue_number") else "✗ status missing"
            print(f"  -> {name}_github.json + {name}_status.json ({tag})")
        except Exception as e:
            print(f"  !! 실패: {e}")


if __name__ == "__main__":
    main()
