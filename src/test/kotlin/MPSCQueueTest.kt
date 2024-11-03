import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import kotlin.test.Test

class MPSCQueueTest {
    private val q = MPSCQueue<Int>()

    @Operation
    fun enqueue(x: Int): Unit = q.enqueue(x)

    @Operation(nonParallelGroup = "consumers")
    fun dequeue(): Int? = q.dequeue()

    @Test
    fun modelCheckingTest() =
        ModelCheckingOptions()
            .iterations(100)
            .invocationsPerIteration(10_000)
            .actorsBefore(1)
            .threads(3)
            .actorsPerThread(3)
            .actorsAfter(0)
            .checkObstructionFreedom()
            .sequentialSpecification(IntQueueSequential::class.java)
            .check(this::class)

    @Test
    fun stressTest() =
        StressOptions()
            .iterations(100)
            .invocationsPerIteration(50_000)
            .actorsBefore(1)
            .threads(3)
            .actorsPerThread(3)
            .actorsAfter(0)
            .sequentialSpecification(IntQueueSequential::class.java)
            .check(this::class)

}

class IntQueueSequential {
    private val q = ArrayDeque<Int>()

    fun enqueue(x: Int) {
        q.addLast(x)
    }

    fun dequeue(): Int? = q.removeFirstOrNull()
}
