package `in`.francl.cam.infrastructure.monitoring.instrumentation.observation

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.observation.MeterObservationHandler
import io.micrometer.observation.Observation

class MeterObservationHandler(
    private val meterRegistry: MeterRegistry,
) : MeterObservationHandler<Observation.Context> {

    override fun onStart(context: Observation.Context) {
        val sample = Timer.start(meterRegistry)
        context.put(Timer.Sample::class.java, sample)
    }

    override fun onStop(context: Observation.Context) {
        val tags: MutableList<Tag> = createTags(context)
        tags.add(Tag.of("error", getErrorValue(context)))
        val sample = context.getRequired<Timer.Sample>(Timer.Sample::class.java)
        sample.stop(
            Timer.builder(context.name)
                .tags(tags)
                .publishPercentiles(0.5, 0.90, 0.95, 0.99)
                .register(this.meterRegistry)
        )
    }

    override fun onEvent(event: Observation.Event, context: Observation.Context) {
        Counter.builder(context.name + "." + event.name)
            .tags(createTags(context))
            .register(meterRegistry)
            .increment()
    }

    private fun getErrorValue(context: Observation.Context): String {
        val error = context.error
        return if (error != null) error.javaClass.simpleName else "none"
    }

    private fun createTags(context: Observation.Context): MutableList<Tag> {
        val tags: MutableList<Tag> = ArrayList()
        for (keyValue in context.lowCardinalityKeyValues) {
            tags.add(Tag.of(keyValue.key, keyValue.value))
        }
        return tags
    }

}
