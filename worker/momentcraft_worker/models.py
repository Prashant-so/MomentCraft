"""
Typed representations of a MomentCraft job file.

These field names and shapes are a direct mirror of what JobWriter.java
writes out — if that Java class changes its output shape, this file needs
to change too. Keeping them in one place means the rest of the worker never
touches raw dicts.
"""

from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class Location:
    x: float
    y: float
    z: float

    @staticmethod
    def from_dict(data: dict) -> "Location":
        return Location(x=data["x"], y=data["y"], z=data["z"])


@dataclass(frozen=True)
class PrimaryPlayer:
    uuid: str
    name: str

    @staticmethod
    def from_dict(data: dict) -> "PrimaryPlayer":
        return PrimaryPlayer(uuid=data["uuid"], name=data["name"])


@dataclass(frozen=True)
class SnapshotPlayer:
    uuid: str
    name: str
    x: float
    y: float
    z: float
    yaw: float
    pitch: float
    health: float
    food_level: int

    @staticmethod
    def from_dict(data: dict) -> "SnapshotPlayer":
        return SnapshotPlayer(
            uuid=data["uuid"],
            name=data["name"],
            x=data["x"],
            y=data["y"],
            z=data["z"],
            yaw=data["yaw"],
            pitch=data["pitch"],
            health=data["health"],
            food_level=data["food_level"],
        )


@dataclass(frozen=True)
class Snapshot:
    timestamp_millis: int
    players: list[SnapshotPlayer]

    @staticmethod
    def from_dict(data: dict) -> "Snapshot":
        return Snapshot(
            timestamp_millis=data["timestamp_millis"],
            players=[SnapshotPlayer.from_dict(p) for p in data["players"]],
        )


@dataclass(frozen=True)
class Job:
    job_id: str
    created_at_millis: int
    moment_type: str
    score: int
    world: str
    location: Location
    primary_player: PrimaryPlayer
    health_fraction: float
    killstreak: int
    nearby_player_count: int
    dangerous_environment: bool
    event_timestamp_millis: int
    snapshots: list[Snapshot]

    @staticmethod
    def from_dict(data: dict) -> "Job":
        return Job(
            job_id=data["job_id"],
            created_at_millis=data["created_at_millis"],
            moment_type=data["moment_type"],
            score=data["score"],
            world=data["world"],
            location=Location.from_dict(data["location"]),
            primary_player=PrimaryPlayer.from_dict(data["primary_player"]),
            health_fraction=data["health_fraction"],
            killstreak=data["killstreak"],
            nearby_player_count=data["nearby_player_count"],
            dangerous_environment=data["dangerous_environment"],
            event_timestamp_millis=data["event_timestamp_millis"],
            snapshots=[Snapshot.from_dict(s) for s in data["snapshots"]],
        )
