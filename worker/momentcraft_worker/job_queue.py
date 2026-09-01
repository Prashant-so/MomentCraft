"""
Polls the jobs directory, safely claims job files one at a time, and hands
each parsed Job off to a processing callback.

"Safely claims" means: the moment a .json file is seen, it's moved into
processing/ before anything else happens. That move is what prevents two
runs of the worker (or a restart mid-loop) from picking up the same file
twice — once it's out of jobs/, nothing else will see it there again.
"""

from __future__ import annotations

import json
import logging
import shutil
import time
from pathlib import Path
from typing import Callable

from .config import WorkerConfig
from .models import Job

logger = logging.getLogger("momentcraft.queue")


class JobQueue:
    def __init__(self, config: WorkerConfig, handler: Callable[[Job, Path], None]):
        self._config = config
        self._handler = handler
        self._running = False

        for directory in (
            self._config.jobs_dir,
            self._config.processing_dir,
            self._config.done_dir,
            self._config.failed_dir,
        ):
            directory.mkdir(parents=True, exist_ok=True)

    def run_forever(self) -> None:
        self._running = True
        logger.info("Watching %s (poll every %.1fs)", self._config.jobs_dir, self._config.poll_interval_seconds)

        while self._running:
            self._poll_once()
            time.sleep(self._config.poll_interval_seconds)

    def stop(self) -> None:
        self._running = False

    def _poll_once(self) -> None:
        try:
            candidates = sorted(self._config.jobs_dir.glob("*.json"))
        except OSError as e:
            logger.error("Failed to list jobs directory: %s", e)
            return

        for job_file in candidates:
            self._claim_and_process(job_file)

    def _claim_and_process(self, job_file: Path) -> None:
        claimed_path = self._config.processing_dir / job_file.name

        try:
            # Atomic on the same filesystem — this is the actual claim.
            # If this succeeds, this worker (and only this worker) owns
            # the job from here on.
            job_file.rename(claimed_path)
        except OSError as e:
            # Most likely: file already moved by another cause, or a
            # transient race. Not fatal — just skip it this cycle.
            logger.warning("Could not claim %s: %s", job_file.name, e)
            return

        try:
            with open(claimed_path, "r", encoding="utf-8") as f:
                data = json.load(f)
            job = Job.from_dict(data)
        except (json.JSONDecodeError, KeyError, OSError) as e:
            logger.error("Failed to parse %s: %s", claimed_path.name, e)
            self._move_to(claimed_path, self._config.failed_dir)
            return

        try:
            self._handler(job, claimed_path)
        except Exception:
            logger.exception("Handler raised while processing job %s", job.job_id)
            self._move_to(claimed_path, self._config.failed_dir)
            return

        self._move_to(claimed_path, self._config.done_dir)

    def _move_to(self, path: Path, destination_dir: Path) -> None:
        try:
            shutil.move(str(path), str(destination_dir / path.name))
        except OSError as e:
            logger.error("Failed to move %s to %s: %s", path.name, destination_dir, e)
