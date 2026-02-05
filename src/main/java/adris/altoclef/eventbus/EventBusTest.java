package adris.altoclef.eventbus;

public class EventBusTest {

    public static void test() {
        Subscription<TestEvent> a = ClefEventBus.subscribe(TestEvent.class, evt -> System.out.println("A: " + evt.val));
        ClefEventBus.publish(new TestEvent(1));
        Subscription<TestEvent> b = ClefEventBus.subscribe(TestEvent.class, evt -> System.out.println("B: " + evt.val));
        ClefEventBus.publish(new TestEvent(2));
        ClefEventBus.unsubscribe(a);
        ClefEventBus.publish(new TestEvent(3));
        ClefEventBus.unsubscribe(b);
        ClefEventBus.publish(new TestEvent(4));
    }

    static class TestEvent {
        public int val;

        public TestEvent(int val) {
            this.val = val;
        }
    }
}
