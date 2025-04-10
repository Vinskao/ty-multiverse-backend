package tw.com.tymbackend.core.observation;

import io.micrometer.observation.Observation;

public interface ObservationHandler<T extends Observation.Context> {
    default void onStart(T context) {}
    default void onError(T context) {}
    default void onEvent(Observation.Event event, T context) {}
    default void onStop(T context) {}
    boolean supportsContext(Observation.Context context);
} 