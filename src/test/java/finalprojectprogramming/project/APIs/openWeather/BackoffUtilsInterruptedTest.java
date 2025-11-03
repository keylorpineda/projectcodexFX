package finalprojectprogramming.project.APIs.openWeather;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class BackoffUtilsInterruptedTest {

    @Test
    void sleep_restores_interrupted_flag_when_interrupted() throws Exception {
        AtomicBoolean interruptedFlagObserved = new AtomicBoolean(false);

        Thread t = new Thread(() -> {
            // Dormir lo suficiente para permitir interrupt
            BackoffUtils.sleep(Duration.ofMillis(100));
            // Al salir del catch, el flag de interrupción debe estar encendido de nuevo
            interruptedFlagObserved.set(Thread.currentThread().isInterrupted());
        });

        t.start();
        // Interrumpimos inmediatamente el hilo para forzar InterruptedException en sleep
        t.interrupt();
        t.join();

        assertThat(interruptedFlagObserved.get()).isTrue();
    }
}
