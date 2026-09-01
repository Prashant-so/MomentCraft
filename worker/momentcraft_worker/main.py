from __future__ import annotations

import logging
import signal

from .config import load_config
from .job_queue import JobQueue
from .logging_setup import configure_logging
from .pipeline import process_job

logger = logging.getLogger("momentcraft.main")


def main() -> None:
    config = load_config()
    configure_logging(config.log_level)

    logger.info("MomentCraft worker starting up")

    queue = JobQueue(config, handler=process_job)

    def handle_shutdown(signum, frame):
        logger.info("Shutdown signal received, finishing current cycle then stopping")
        queue.stop()

    signal.signal(signal.SIGINT, handle_shutdown)
    signal.signal(signal.SIGTERM, handle_shutdown)

    queue.run_forever()

    logger.info("MomentCraft worker stopped")


if __name__ == "__main__":
    main()
