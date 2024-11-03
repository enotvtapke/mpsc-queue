import kotlinx.atomicfu.*

class MPSCQueue<E> {
    private var head: Segment
    private val tail: AtomicRef<Segment>
    private val enqIdx = atomic(0L)
    private var deqIdx = 0L

    init {
        val firstNode = Segment(0L)
        head = firstNode
        tail = atomic(firstNode)
    }

    fun enqueue(element: E) {
        while (true) {
            val curTail = tail.value
            val i = enqIdx.getAndIncrement()
            val s = curTail.findSegment(i)
            if (s.id > curTail.id) {
                tail.compareAndSet(curTail, s)
            }
            if (s.compareAndSet((i % SEGMENT_SIZE).toInt(), null, element)) {
                return
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun dequeue(): E? {
        while (true) {
            if (deqIdx >= enqIdx.value) return null
            val curHead = head
            val i = deqIdx++

            val s = curHead.findSegment(i)
            if (s.id > curHead.id) {
                head = s
            }
            if (s.compareAndSet((i % SEGMENT_SIZE).toInt(), null, BROKEN)) continue
            return s[(i % SEGMENT_SIZE).toInt()] as E
        }
    }
}

private class Segment(val id: Long) {
    val next = atomic<Segment?>(null)
    val elements = atomicArrayOfNulls<Any>(SEGMENT_SIZE)

    operator fun get(i: Int) = elements[i].value
    fun compareAndSet(i: Int, expect: Any?, update: Any?) = elements[i].compareAndSet(expect, update)

    fun findSegment(index: Long): Segment {
        var curSeg = this
        for (i in curSeg.id until index / SEGMENT_SIZE) {
            var next = curSeg.next.value
            if (next == null) {
                val newSeg = Segment(i + 1)
                curSeg.next.compareAndSet(null, newSeg)
                next = curSeg.next.value
            }
            curSeg = next!!
        }
        return curSeg
    }
}

private const val SEGMENT_SIZE = 2

private val BROKEN = Any()
