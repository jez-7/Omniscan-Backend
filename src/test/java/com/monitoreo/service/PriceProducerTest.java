package com.monitoreo.service;

import com.monitoreo.model.dto.PriceEvent;
import com.monitoreo.model.entity.Subscription;
import com.monitoreo.repository.SubscriptionRepository;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class PriceProducerTest {

    @Spy
    @InjectMocks
    private PriceProducer priceProducer;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    /**
     * Genera un Document HTML simulando la estructura de HardGamers
     */
    private Document createMockDocument(String productId, String title, String price, String store) {
        String html = """
                <html><body>
                <article itemscope itemtype="http://schema.org/Product" class="col-6 col-sm-4 col-md-4 py-2 px-4 product">
                    <div class="product-image">
                        <a href="/product/%s" title="Ver detalles">
                            <img itemprop="image" class="img-fluid" src="https://example.com/img.jpg" alt="%s">
                        </a>
                    </div>
                    <div class="product-description">
                        <a itemprop="url" href="https://www.hardgamers.com.ar/product/%s" title="Ver detalles">
                            <h3 itemprop="name" class="product-title line-clamp">%s</h3>
                        </a>
                        <h4 class="subtitle">%s</h4>
                        <div itemprop="offers" itemscope itemtype="http://schema.org/Offer">
                            <h2 itemprop="priceCurrency" content="ARS" class="product-price">$</h2>
                            <h2 itemprop="price" content="%s" class="product-price">%s</h2>
                            <div id="%s" class="favourite">
                                <i class="far fa-heart"></i>
                            </div>
                        </div>
                    </div>
                </article>
                </body></html>
                """.formatted(productId, title, productId, title, store, price, price, productId);

        return Parser.htmlParser().parseInput(html, "https://www.hardgamers.com.ar");
    }

    private Document createEmptyDocument() {
        return Parser.htmlParser().parseInput("<html><body></body></html>", "https://www.hardgamers.com.ar");
    }

    private Document createMultiProductDocument(int count) {
        StringBuilder html = new StringBuilder("<html><body>");
        for (int i = 0; i < count; i++) {
            html.append("""
                <article itemscope itemtype="http://schema.org/Product" class="product">
                    <div class="product-image">
                        <a href="/product/PROD%d"><img itemprop="image" src="https://example.com/img%d.jpg" alt="Product %d"></a>
                    </div>
                    <div class="product-description">
                        <a itemprop="url" href="https://www.hardgamers.com.ar/product/PROD%d">
                            <h3 itemprop="name" class="product-title">Notebook Test %d</h3>
                        </a>
                        <h4 class="subtitle">Tienda %d</h4>
                        <div itemprop="offers"><h2 itemprop="price" content="%d">%d</h2></div>
                    </div>
                </article>
                """.formatted(i, i, i, i, i, i, (i + 1) * 100000, (i + 1) * 100000));
        }
        html.append("</body></html>");
        return Parser.htmlParser().parseInput(html.toString(), "https://www.hardgamers.com.ar");
    }

    @Test
    void testScanMarket_WithActiveSubscriptions() throws IOException {
        Subscription sub = Subscription.builder()
                .chatId(12345L)
                .keyword("notebook")
                .createdAt(LocalDateTime.now())
                .build();
        when(subscriptionRepository.findAll()).thenReturn(List.of(sub));

        Document mockDoc = createMockDocument("MLA123", "Notebook Lenovo IdeaPad", "450000", "CompraGamer");
        doReturn(mockDoc).when(priceProducer).fetchDocument(anyString());

        priceProducer.scanMarket();

        verify(kafkaTemplate, times(1)).send(eq("prices-topic"), anyString(), any(PriceEvent.class));
    }

    @Test
    void testScanMarket_NoSubscriptions() {
        when(subscriptionRepository.findAll()).thenReturn(List.of());

        priceProducer.scanMarket();

        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    void testScanMarket_ScrapeError() throws IOException {
        Subscription sub = Subscription.builder()
                .chatId(12345L)
                .keyword("gpu")
                .createdAt(LocalDateTime.now())
                .build();
        when(subscriptionRepository.findAll()).thenReturn(List.of(sub));
        doThrow(new IOException("Connection refused")).when(priceProducer).fetchDocument(anyString());

        priceProducer.scanMarket();

        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    void testScanMarket_EmptyResults() throws IOException {
        Subscription sub = Subscription.builder()
                .chatId(12345L)
                .keyword("producto inexistente")
                .createdAt(LocalDateTime.now())
                .build();
        when(subscriptionRepository.findAll()).thenReturn(List.of(sub));

        Document emptyDoc = createEmptyDocument();
        doReturn(emptyDoc).when(priceProducer).fetchDocument(anyString());

        priceProducer.scanMarket();

        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    void testScanMarket_DuplicateKeywordsScannedOnce() throws IOException {
        Subscription sub1 = Subscription.builder().chatId(11111L).keyword("notebook").createdAt(LocalDateTime.now()).build();
        Subscription sub2 = Subscription.builder().chatId(22222L).keyword("notebook").createdAt(LocalDateTime.now()).build();
        when(subscriptionRepository.findAll()).thenReturn(List.of(sub1, sub2));

        Document mockDoc = createMockDocument("PROD1", "Notebook Test", "300000", "Tienda");
        doReturn(mockDoc).when(priceProducer).fetchDocument(anyString());

        priceProducer.scanMarket();

        // Solo debe scrapear 1 vez porque "notebook" es la misma keyword
        verify(priceProducer, times(1)).fetchDocument(anyString());
    }

    @Test
    void testScanMarket_MultipleKeywords() throws IOException {
        Subscription sub1 = Subscription.builder().chatId(11111L).keyword("notebook").createdAt(LocalDateTime.now()).build();
        Subscription sub2 = Subscription.builder().chatId(22222L).keyword("gpu").createdAt(LocalDateTime.now()).build();
        when(subscriptionRepository.findAll()).thenReturn(List.of(sub1, sub2));

        Document mockDoc = createMockDocument("PROD1", "Product Test", "200000", "Tienda");
        doReturn(mockDoc).when(priceProducer).fetchDocument(anyString());

        priceProducer.scanMarket();

        // Debe scrapear 2 veces: una por "notebook" y otra por "gpu"
        verify(priceProducer, times(2)).fetchDocument(anyString());
        verify(kafkaTemplate, times(2)).send(eq("prices-topic"), anyString(), any(PriceEvent.class));
    }

    @Test
    void testScrapeProducts_RespectsResultsLimit() throws IOException {
        Document multiDoc = createMultiProductDocument(10);
        doReturn(multiDoc).when(priceProducer).fetchDocument(anyString());

        List<PriceEvent> events = priceProducer.scrapeProducts("notebook");

        // resultsLimit default es 5, así que solo debe retornar 5 máximo
        assertEquals(5, events.size());
    }

    @Test
    void testParseProductCard() {
        Document doc = createMockDocument("12345", "RTX 4070 Ti SUPER", "850000", "CompraGamer");
        Element card = doc.selectFirst("article");

        PriceEvent event = priceProducer.parseProductCard(card);

        assertNotNull(event);
        assertEquals("RTX 4070 Ti SUPER", event.getProductName());
        assertEquals(850000.0, event.getPrice());
        assertEquals("https://www.hardgamers.com.ar/product/12345", event.getPermalink());
        assertEquals("https://example.com/img.jpg", event.getThumbnail());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void testParseProductCard_ExtractsProductIdFromLink() {
        Document doc = createMockDocument("ABC789", "RAM Kingston 16GB", "45000", "Maximus");
        Element card = doc.selectFirst("article");

        PriceEvent event = priceProducer.parseProductCard(card);

        assertNotNull(event);
        assertEquals("ABC789", event.getProductId());
    }

    @Test
    void testParseProductCard_NoTitle() {
        String html = """
                <html><body>
                <article class="product">
                    <div itemprop="offers"><h2 itemprop="price" content="1000">1000</h2></div>
                </article>
                </body></html>
                """;
        Document doc = Parser.htmlParser().parseInput(html, "");
        Element card = doc.selectFirst("article");

        PriceEvent event = priceProducer.parseProductCard(card);

        assertNull(event);
    }

    @Test
    void testParseProductCard_ZeroPrice() {
        String html = """
                <html><body>
                <article class="product">
                    <a itemprop="url" href="/product/999">
                        <h3 itemprop="name">Producto sin precio</h3>
                    </a>
                    <div itemprop="offers"><h2 itemprop="price" content="0">0</h2></div>
                </article>
                </body></html>
                """;
        Document doc = Parser.htmlParser().parseInput(html, "");
        Element card = doc.selectFirst("article");

        PriceEvent event = priceProducer.parseProductCard(card);

        // Precio 0 debería ser filtrado por scrapeProducts (no por parseProductCard)
        assertNotNull(event);
        assertEquals(0.0, event.getPrice());
    }

    @Test
    void testParseProductCard_FallbackPriceFromText() {
        String html = """
                <html><body>
                <article class="product">
                    <a itemprop="url" href="/product/555">
                        <h3 itemprop="name">Producto fallback</h3>
                    </a>
                    <div itemprop="offers"><h2 itemprop="price" content="">150.000</h2></div>
                </article>
                </body></html>
                """;
        Document doc = Parser.htmlParser().parseInput(html, "");
        Element card = doc.selectFirst("article");

        PriceEvent event = priceProducer.parseProductCard(card);

        assertNotNull(event);
        assertEquals(150000.0, event.getPrice());
    }

    @Test
    void testGetActiveKeywords() {
        Subscription sub1 = Subscription.builder().chatId(1L).keyword("notebook").createdAt(LocalDateTime.now()).build();
        Subscription sub2 = Subscription.builder().chatId(2L).keyword("ram").createdAt(LocalDateTime.now()).build();
        Subscription sub3 = Subscription.builder().chatId(3L).keyword("notebook").createdAt(LocalDateTime.now()).build();

        when(subscriptionRepository.findAll()).thenReturn(List.of(sub1, sub2, sub3));

        Set<String> keywords = priceProducer.getActiveKeywords();

        assertEquals(2, keywords.size());
        assertTrue(keywords.contains("notebook"));
        assertTrue(keywords.contains("ram"));
    }

    @Test
    void testParseProductCard_NoImage() {
        String html = """
                <html><body>
                <article class="product">
                    <a itemprop="url" href="/product/111">
                        <h3 itemprop="name">Producto sin imagen</h3>
                    </a>
                    <div itemprop="offers"><h2 itemprop="price" content="5000">5.000</h2></div>
                </article>
                </body></html>
                """;
        Document doc = Parser.htmlParser().parseInput(html, "");
        Element card = doc.selectFirst("article");

        PriceEvent event = priceProducer.parseProductCard(card);

        assertNotNull(event);
        assertEquals("", event.getThumbnail());
    }
}
