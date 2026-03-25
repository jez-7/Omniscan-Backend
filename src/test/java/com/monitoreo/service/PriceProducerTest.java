package com.monitoreo.service;

import com.monitoreo.model.dto.PriceEvent;
import com.monitoreo.model.entity.Subscription;
import com.monitoreo.repository.SubscriptionRepository;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class PriceProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private PriceProducer priceProducer;

    @BeforeEach
    void setUp() {
        // @Value no se inyecta en tests unitarios puros → seteamos manualmente
        ReflectionTestUtils.setField(priceProducer, "resultsLimit", 5);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private Document buildDocument(String productId, String title, String price, String store) {
        String html = """
                <html><body>
                <article itemscope itemtype="http://schema.org/Product" class="product">
                    <div class="product-image">
                        <a href="/product/%s"><img itemprop="image" src="https://example.com/img.jpg" alt="%s"></a>
                    </div>
                    <div class="product-description">
                        <a itemprop="url" href="https://www.hardgamers.com.ar/product/%s">
                            <h3 itemprop="name" class="product-title">%s</h3>
                        </a>
                        <h4 class="subtitle">%s</h4>
                        <div itemprop="offers" itemscope itemtype="http://schema.org/Offer">
                            <h2 itemprop="priceCurrency" content="ARS">$</h2>
                            <h2 itemprop="price" content="%s">%s</h2>
                            <div id="%s" class="favourite"><i class="far fa-heart"></i></div>
                        </div>
                    </div>
                </article>
                </body></html>
                """.formatted(productId, title, productId, title, store, price, price, productId);
        return Parser.htmlParser().parseInput(html, "https://www.hardgamers.com.ar");
    }

    private Document buildMultiProductDocument(int count) {
        StringBuilder html = new StringBuilder("<html><body>");
        for (int i = 0; i < count; i++) {
            html.append("""
                <article itemscope itemtype="http://schema.org/Product" class="product">
                    <div class="product-image">
                        <a href="/product/PROD%d"><img itemprop="image" src="img%d.jpg" alt="p%d"></a>
                    </div>
                    <div class="product-description">
                        <a itemprop="url" href="https://www.hardgamers.com.ar/product/PROD%d">
                            <h3 itemprop="name" class="product-title">Notebook Test %d</h3>
                        </a>
                        <div itemprop="offers"><h2 itemprop="price" content="%d">%d</h2></div>
                    </div>
                </article>
                """.formatted(i, i, i, i, i, (i + 1) * 100000, (i + 1) * 100000));
        }
        html.append("</body></html>");
        return Parser.htmlParser().parseInput(html.toString(), "https://www.hardgamers.com.ar");
    }

    private Document buildEmptyDocument() {
        return Parser.htmlParser().parseInput("<html><body></body></html>", "https://www.hardgamers.com.ar");
    }

    // ─── Tests de scanMarket ────────────────────────────────────────────────────

    @Test
    void testScanMarket_WithActiveSubscriptions() throws IOException {
        // Usamos spy directo sobre el priceProducer que ya tiene mocks inyectados
        PriceProducer spy = spy(priceProducer);
        Document doc = buildDocument("MLA123", "Notebook Lenovo", "450000", "CompraGamer");
        doReturn(doc).when(spy).fetchDocument(anyString());

        Subscription sub = Subscription.builder()
                .chatId(12345L).keyword("notebook").createdAt(LocalDateTime.now()).build();
        when(subscriptionRepository.findAll()).thenReturn(List.of(sub));

        spy.scanMarket();

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
        PriceProducer spy = spy(priceProducer);
        doThrow(new IOException("Connection refused")).when(spy).fetchDocument(anyString());

        Subscription sub = Subscription.builder()
                .chatId(12345L).keyword("gpu").createdAt(LocalDateTime.now()).build();
        when(subscriptionRepository.findAll()).thenReturn(List.of(sub));

        spy.scanMarket();

        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    void testScanMarket_EmptyResults() throws IOException {
        PriceProducer spy = spy(priceProducer);
        doReturn(buildEmptyDocument()).when(spy).fetchDocument(anyString());

        Subscription sub = Subscription.builder()
                .chatId(12345L).keyword("producto inexistente").createdAt(LocalDateTime.now()).build();
        when(subscriptionRepository.findAll()).thenReturn(List.of(sub));

        spy.scanMarket();

        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    void testScanMarket_DuplicateKeywordsScannedOnce() throws IOException {
        PriceProducer spy = spy(priceProducer);
        doReturn(buildDocument("P1", "Notebook Test", "300000", "Tienda")).when(spy).fetchDocument(anyString());

        Subscription sub1 = Subscription.builder().chatId(11111L).keyword("notebook").createdAt(LocalDateTime.now()).build();
        Subscription sub2 = Subscription.builder().chatId(22222L).keyword("notebook").createdAt(LocalDateTime.now()).build();
        when(subscriptionRepository.findAll()).thenReturn(List.of(sub1, sub2));

        spy.scanMarket();

        // keyword duplicada → solo 1 llamada HTTP
        verify(spy, times(1)).fetchDocument(anyString());
    }

    @Test
    void testScanMarket_MultipleKeywords() throws IOException {
        PriceProducer spy = spy(priceProducer);
        doReturn(buildDocument("P1", "Product Test", "200000", "Tienda")).when(spy).fetchDocument(anyString());

        Subscription sub1 = Subscription.builder().chatId(11111L).keyword("notebook").createdAt(LocalDateTime.now()).build();
        Subscription sub2 = Subscription.builder().chatId(22222L).keyword("gpu").createdAt(LocalDateTime.now()).build();
        when(subscriptionRepository.findAll()).thenReturn(List.of(sub1, sub2));

        spy.scanMarket();

        // 2 keywords distintas → 2 llamadas HTTP
        verify(spy, times(2)).fetchDocument(anyString());
        verify(kafkaTemplate, times(2)).send(eq("prices-topic"), anyString(), any(PriceEvent.class));
    }

    // ─── Tests de scrapeProducts ────────────────────────────────────────────────

    @Test
    void testScrapeProducts_RespectsResultsLimit() throws IOException {
        PriceProducer spy = spy(priceProducer);
        doReturn(buildMultiProductDocument(10)).when(spy).fetchDocument(anyString());

        List<PriceEvent> events = spy.scrapeProducts("notebook");

        // resultsLimit = 5
        assertEquals(5, events.size());
    }

    @Test
    void testScrapeProducts_ReturnsAllIfFewer() throws IOException {
        PriceProducer spy = spy(priceProducer);
        doReturn(buildMultiProductDocument(3)).when(spy).fetchDocument(anyString());

        List<PriceEvent> events = spy.scrapeProducts("notebook");

        assertEquals(3, events.size());
    }

    @Test
    void testScrapeProducts_SkipsZeroPriceItems() throws IOException {
        PriceProducer spy = spy(priceProducer);

        String htmlWithZeroPrice = """
                <html><body>
                <article itemscope itemtype="http://schema.org/Product" class="product">
                    <a itemprop="url" href="/product/AAA">
                        <h3 itemprop="name">Producto sin precio</h3>
                    </a>
                    <div itemprop="offers"><h2 itemprop="price" content="0">0</h2></div>
                </article>
                </body></html>
                """;
        doReturn(Parser.htmlParser().parseInput(htmlWithZeroPrice, "")).when(spy).fetchDocument(anyString());

        List<PriceEvent> events = spy.scrapeProducts("algo");

        assertTrue(events.isEmpty());
    }

    // ─── Tests de parseProductCard ──────────────────────────────────────────────

    @Test
    void testParseProductCard_Complete() {
        Document doc = buildDocument("12345", "RTX 4070 Ti SUPER", "850000", "CompraGamer");
        Element card = doc.selectFirst("article");

        PriceEvent event = priceProducer.parseProductCard(card);

        assertNotNull(event);
        assertEquals("12345", event.getProductId());
        assertEquals("RTX 4070 Ti SUPER", event.getProductName());
        assertEquals(850000.0, event.getPrice());
        assertEquals("https://www.hardgamers.com.ar/product/12345", event.getPermalink());
        assertEquals("https://example.com/img.jpg", event.getThumbnail());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void testParseProductCard_NoTitle_ReturnsNull() {
        String html = """
                <html><body>
                <article class="product">
                    <div itemprop="offers"><h2 itemprop="price" content="1000">1000</h2></div>
                </article>
                </body></html>
                """;
        Element card = Parser.htmlParser().parseInput(html, "").selectFirst("article");

        PriceEvent event = priceProducer.parseProductCard(card);

        assertNull(event);
    }

    @Test
    void testParseProductCard_NoImage_ThumbnailEmpty() {
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
        Element card = Parser.htmlParser().parseInput(html, "").selectFirst("article");

        PriceEvent event = priceProducer.parseProductCard(card);

        assertNotNull(event);
        assertEquals("", event.getThumbnail());
    }

    @Test
    void testParseProductCard_FallbackPriceFromText() {
        // content="" → debe intentar parsear el texto visible "150.000"
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
        Element card = Parser.htmlParser().parseInput(html, "").selectFirst("article");

        PriceEvent event = priceProducer.parseProductCard(card);

        assertNotNull(event);
        assertEquals(150000.0, event.getPrice());
    }

    @Test
    void testParseProductCard_IdFromFavouriteDiv() {
        // Sin link itemprop=url → el ID viene del div.favourite
        String html = """
                <html><body>
                <article class="product">
                    <h3 itemprop="name">Producto sin link</h3>
                    <div itemprop="offers"><h2 itemprop="price" content="9999">9999</h2></div>
                    <div id="FAVID123" class="favourite"></div>
                </article>
                </body></html>
                """;
        Element card = Parser.htmlParser().parseInput(html, "").selectFirst("article");

        PriceEvent event = priceProducer.parseProductCard(card);

        assertNotNull(event);
        assertEquals("FAVID123", event.getProductId());
    }

    // ─── Tests de getActiveKeywords ─────────────────────────────────────────────

    @Test
    void testGetActiveKeywords_Deduplication() {
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
    void testGetActiveKeywords_Empty() {
        when(subscriptionRepository.findAll()).thenReturn(List.of());

        Set<String> keywords = priceProducer.getActiveKeywords();

        assertTrue(keywords.isEmpty());
    }
}
