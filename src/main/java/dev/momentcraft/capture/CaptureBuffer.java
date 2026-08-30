package dev.momentcraft.capture;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public final class CaptureBuffer {

    private final int capacity;
    private final Deque<Snapshot> snapshots = new ArrayDeque<>();

    public CaptureBuffer(int capacity) {
        this.capacity = capacity;
    }

    public void add(Snapshot snapshot) {
        snapshots.addLast(snapshot);
        while (snapshots.size() > capacity) {
            snapshots.removeFirst();
        }
    }

    public int size() {
        return snapshots.size();
    }

    public List<Snapshot> snapshots() {
        return Collections.unmodifiableList(new ArrayList<>(snapshots));
    }
}
