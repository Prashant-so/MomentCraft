"""
The actual moment-to-clip pipeline.

Right now this is intentionally a stub — Phase 8's job is proving jobs can
be reliably picked up and parsed, nothing more. Phase 9 (environment
analysis) is where real logic starts landing in here: classifying the
scene, computing the center of action, and eventually handing off to the
camera director, render client, and encoder in the phases after that.
"""

from __future__ import annotations

import logging
from pathlib import Path

from .models import Job

logger = logging.getLogger("momentcraft.pipeline")


def process_job(job: Job, job_file: Path) -> None:
    logger.info(
        "Received job %s: %s by %s (score %d, %d snapshot(s)) in %s",
        job.job_id,
        job.moment_type,
        job.primary_player.name,
        job.score,
        len(job.snapshots),
        job.world,
    )

    # Phase 9+ will replace this with real work:
    #   scene = classify_environment(job)
    #   plan = build_camera_plan(job, scene)
    #   render(job, plan)
    #   encode(...)
    #   deliver_to_discord(...)
