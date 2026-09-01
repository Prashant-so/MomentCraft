"""
Worker configuration.

Deliberately simple — a handful of settings read from a YAML file, with
sane defaults if the file or a key is missing. This mirrors the Java
plugin's own philosophy: don't build a generic config framework for a
small, known set of settings.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path

import yaml

DEFAULT_CONFIG_PATH = Path("config.yaml")


@dataclass(frozen=True)
class WorkerConfig:
    jobs_dir: Path
    poll_interval_seconds: float
    log_level: str

    @property
    def processing_dir(self) -> Path:
        return self.jobs_dir / "processing"

    @property
    def done_dir(self) -> Path:
        return self.jobs_dir / "done"

    @property
    def failed_dir(self) -> Path:
        return self.jobs_dir / "failed"


def load_config(path: Path = DEFAULT_CONFIG_PATH) -> WorkerConfig:
    data = {}
    if path.exists():
        with open(path, "r", encoding="utf-8") as f:
            data = yaml.safe_load(f) or {}

    jobs_dir = Path(data.get("jobs_dir", "../server/plugins/MomentCraft/jobs"))
    poll_interval = float(data.get("poll_interval_seconds", 2.0))
    log_level = str(data.get("log_level", "INFO")).upper()

    return WorkerConfig(
        jobs_dir=jobs_dir,
        poll_interval_seconds=poll_interval,
        log_level=log_level,
    )
