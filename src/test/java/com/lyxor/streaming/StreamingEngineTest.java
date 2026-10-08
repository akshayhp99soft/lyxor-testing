package com.lyxor.streaming;

import com.lyxor.fulfillment.model.WalletAccount;
import com.lyxor.streaming.model.DeliveryStatus;
import com.lyxor.streaming.model.EventMessage;
import com.lyxor.streaming.model.ExchangeRate;
import com.lyxor.streaming.service.CircularBufferQueue;
import com.lyxor.streaming.service.DistributedLockCoordinator;
import com.lyxor.streaming.service.EventMessageBroker;
import com.lyxor.streaming.service.MultiCurrencyLedger;
import com.lyxor.streaming.service.TokenBucketRateLimiter;
import com.lyxor.streaming.service.WebhookDispatcher;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class StreamingEngineTest {

    @Test
    void testRateLimiterBasicAcquisition() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(10, 5);
        assertTrue(limiter.tryAcquire(2));
        assertTrue(limiter.getAvailableTokens() <= 8.0);
    }

    @Test
    void testLedgerCurrencyConversion() {
        MultiCurrencyLedger ledger = new MultiCurrencyLedger();
        ledger.registerRate(new ExchangeRate("USD", "EUR", BigDecimal.valueOf(0.92), Instant.now().plus(1, ChronoUnit.HOURS)));

        BigDecimal converted = ledger.convertCurrency(BigDecimal.valueOf(100), "USD", "EUR");
        assertEquals(BigDecimal.valueOf(92.00).setScale(2), converted);
    }

    @Test
    void testLedgerFundTransfer() {
        MultiCurrencyLedger ledger = new MultiCurrencyLedger();
        WalletAccount acc1 = new WalletAccount("ACC-1", BigDecimal.valueOf(200));
        WalletAccount acc2 = new WalletAccount("ACC-2", BigDecimal.valueOf(50));

        boolean success = ledger.transferFunds(acc1, acc2, BigDecimal.valueOf(100));
        assertTrue(success);
        assertEquals(BigDecimal.valueOf(100), acc1.getBalance());
        assertEquals(BigDecimal.valueOf(150), acc2.getBalance());
    }

    @Test
    void testCircuitBreakerSuccessPath() {
        WebhookDispatcher dispatcher = new WebhookDispatcher(3, 2);
        boolean dispatched = dispatcher.dispatchWithRetry("https://api.example.com/webhook", "{}", 2, url -> 200);
        assertTrue(dispatched);
        assertEquals(WebhookDispatcher.CircuitState.CLOSED, dispatcher.getState());
    }

    @Test
    void testCircularBufferQueue() {
        CircularBufferQueue<String> queue = new CircularBufferQueue<>(4);
        long seq1 = queue.offer("msg-1");
        long seq2 = queue.offer("msg-2");

        assertEquals("msg-1", queue.get(seq1));
        assertEquals("msg-2", queue.get(seq2));
    }

    @Test
    void testEventBrokerPublishAndAck() throws InterruptedException {
        EventMessageBroker broker = new EventMessageBroker(10);
        EventMessage msg = new EventMessage("M1", "orders.topic", "{}", Map.of("version", "1.0"));

        assertTrue(broker.publishWithBackpressure(msg, 100));
        EventMessage polled = broker.pollMessage(100);
        assertNotNull(polled);
        assertEquals(DeliveryStatus.IN_FLIGHT, polled.getStatus());

        assertTrue(broker.acknowledgeMessage("M1"));
        assertEquals(DeliveryStatus.ACKNOWLEDGED, polled.getStatus());
    }

    @Test
    void testDistributedLockLifecycle() {
        DistributedLockCoordinator coordinator = new DistributedLockCoordinator();
        assertTrue(coordinator.acquireLock("resource-1", "node-1", 5000));
        assertTrue(coordinator.isLocked("resource-1"));
        assertFalse(coordinator.acquireLock("resource-1", "node-2", 5000));

        assertTrue(coordinator.releaseLock("resource-1", "node-1"));
        assertFalse(coordinator.isLocked("resource-1"));
    }
}
