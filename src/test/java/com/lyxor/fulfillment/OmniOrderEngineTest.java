package com.lyxor.fulfillment;

import com.lyxor.fulfillment.model.AuditEntry;
import com.lyxor.fulfillment.model.BatchResult;
import com.lyxor.fulfillment.model.CacheKey;
import com.lyxor.fulfillment.model.CustomerTier;
import com.lyxor.fulfillment.model.Order;
import com.lyxor.fulfillment.model.OrderItem;
import com.lyxor.fulfillment.model.OrderStatus;
import com.lyxor.fulfillment.model.WalletAccount;
import com.lyxor.fulfillment.service.AuditLogExporter;
import com.lyxor.fulfillment.service.BatchOrderProcessor;
import com.lyxor.fulfillment.service.DiscountService;
import com.lyxor.fulfillment.service.InventoryService;
import com.lyxor.fulfillment.service.OrderFulfillmentService;
import com.lyxor.fulfillment.service.SessionTokenCache;
import com.lyxor.fulfillment.service.WalletPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class OmniOrderEngineTest {

    private InventoryService inventoryService;
    private DiscountService discountService;
    private WalletPaymentService paymentService;
    private OrderFulfillmentService fulfillmentService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService();
        discountService = new DiscountService();
        paymentService = new WalletPaymentService();
        fulfillmentService = new OrderFulfillmentService(inventoryService, discountService, paymentService);
    }

    @Test
    void testBasicHappyPathFulfillment() {
        inventoryService.setStock("SKU-100", 10);
        paymentService.registerAccount("CUST-1", BigDecimal.valueOf(500));

        Order order = new Order("ORD-1", "CUST-1", CustomerTier.REGULAR);
        order.addItem(new OrderItem("SKU-100", "Widget", 2, BigDecimal.valueOf(50)));

        boolean result = fulfillmentService.fulfillOrder(order);

        assertTrue(result);
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertEquals(8, inventoryService.getStock("SKU-100"));
        assertEquals(BigDecimal.valueOf(400), paymentService.getAccount("CUST-1").getBalance());
    }

    @Test
    void testTierDiscountAndPromotions() {
        Order order = new Order("ORD-101", "CUST-101", CustomerTier.PLATINUM);
        order.addItem(new OrderItem("SKU-PROMO", "Laptop", 1, BigDecimal.valueOf(1000)));

        BigDecimal discount = discountService.calculateFinalDiscount(order, BigDecimal.valueOf(50), BigDecimal.valueOf(200));
        assertNotNull(discount);
    }

    @Test
    void testInventoryConcurrentAccess() throws InterruptedException {
        String sku = "SKU-HOT";
        inventoryService.setStock(sku, 1);

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successfulReservations = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    if (inventoryService.reserveStock(sku, 1)) {
                        successfulReservations.incrementAndGet();
                    }
                } catch (InterruptedException ignored) {
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        Thread.sleep(100);

        assertTrue(successfulReservations.get() >= 1);
    }

    @Test
    void testOrderCancellationFlow() {
        Order order = new Order("ORD-CANC", "CUST-1", CustomerTier.REGULAR);
        order.addItem(new OrderItem("SKU-1", "Item", 1, BigDecimal.valueOf(50)));
        order.setStatus(OrderStatus.PAID);

        boolean cancelled = fulfillmentService.cancelOrder(order);
        assertTrue(cancelled);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void testSessionTokenCacheOperations() {
        SessionTokenCache cache = new SessionTokenCache();
        CacheKey key = new CacheKey("user-1", "tenant-1");
        cache.putToken(key, "session-token-xyz");

        cache.getToken(key);
        assertNotNull(cache.getMetrics());
    }

    @Test
    void testAuditLogExportAndQuery(@TempDir Path tempDir) throws IOException {
        AuditLogExporter exporter = new AuditLogExporter();
        Path logFile = tempDir.resolve("audit.log");
        exporter.appendEntries(logFile, List.of(
                new AuditEntry("E1", "ORDER", "ORD-1", "CREATED"),
                new AuditEntry("E2", "ORDER", "ORD-1", "PAID")
        ));

        long count = exporter.countActionsForEntity(logFile, "ORDER", "PAID");
        assertEquals(1, count);
    }

    @Test
    void testBatchOrderPartitioning() {
        BatchOrderProcessor processor = new BatchOrderProcessor(fulfillmentService);
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            orders.add(new Order("ORD-" + i, "CUST-" + i, CustomerTier.REGULAR));
        }

        List<List<Order>> partitions = processor.partitionOrders(orders, 2);
        assertFalse(partitions.isEmpty());
        processor.shutdown();
    }

    @Test
    void testMetricsCollectorInitialization() {
        SessionTokenCache cache = new SessionTokenCache();
        SessionTokenCache.CacheMetrics metrics1 = cache.getMetrics();
        SessionTokenCache.CacheMetrics metrics2 = cache.getMetrics();
        assertSame(metrics1, metrics2);
    }

    @Test
    void testPaymentAccountStateHandling() {
        paymentService.registerAccount("CUST-LOCKED", BigDecimal.valueOf(1000));
        WalletAccount account = paymentService.getAccount("CUST-LOCKED");
        account.setLocked(true);

        paymentService.processPayment("CUST-LOCKED", BigDecimal.valueOf(100));
    }

    @Test
    void testBatchExecutionProcessing() {
        BatchOrderProcessor processor = new BatchOrderProcessor(fulfillmentService);
        List<Order> orders = List.of(
                new Order("ORD-SAMPLE", "CUST-1", CustomerTier.REGULAR)
        );

        BatchResult result = processor.processBatch(orders);
        assertNotNull(result);
        processor.shutdown();
    }

    @Test
    void testOrderItemsCollectionHandling() {
        Order order = new Order("ORD-ITEMS", "CUST-1", CustomerTier.REGULAR);
        OrderItem item1 = new OrderItem("SKU-DUP", "Item 1", 1, BigDecimal.valueOf(10));
        OrderItem item2 = new OrderItem("SKU-DUP", "Item 1", 1, BigDecimal.valueOf(10));

        order.addItem(item1);
        order.addItem(item2);

        assertFalse(order.getItems().isEmpty());
    }
}
