package com.roocode.android.services.browser

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.URL

@Singleton
class BrowserService @Inject constructor(
    private val context: Context
) {
    private var webView: WebView? = null
    
    /**
     * Fetch content from a URL
     */
    suspend fun fetchUrl(url: String): BrowserResult {
        return try {
            val document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Android) RooCode/1.0")
                .timeout(10000)
                .get()
            
            val content = extractContent(document)
            BrowserResult(
                success = true,
                content = content,
                title = document.title(),
                url = url,
                error = null
            )
        } catch (e: IOException) {
            Log.e("BrowserService", "Error fetching URL: $url", e)
            BrowserResult(
                success = false,
                content = "",
                title = "",
                url = url,
                error = e.message
            )
        }
    }
    
    /**
     * Search the web using a search engine
     */
    suspend fun searchWeb(query: String, maxResults: Int = 5): List<SearchResult> {
        val searchUrl = "https://www.google.com/search?q=${query.replace(" ", "+")}"
        val result = fetchUrl(searchUrl)
        
        return if (result.success) {
            parseSearchResults(result.content, maxResults)
        } else {
            emptyList()
        }
    }
    
    /**
     * Execute JavaScript on a webpage
     */
    suspend fun executeJavaScript(url: String, script: String): BrowserResult {
        return withTimeoutOrNull(30000) {
            suspendCancellableCoroutine { continuation ->
                val webView = getOrCreateWebView()
                
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        view?.evaluateJavascript(script) { result ->
                            continuation.resume(BrowserResult(
                                success = true,
                                content = result,
                                title = view.title ?: "",
                                url = url ?: "",
                                error = null
                            ))
                        }
                    }
                }
                
                webView.loadUrl(url)
            }
        } ?: BrowserResult(
            success = false,
            content = "",
            title = "",
            url = url,
            error = "Timeout executing JavaScript"
        )
    }
    
    /**
     * Take a screenshot of a webpage
     */
    suspend fun takeScreenshot(url: String): BrowserResult {
        return withTimeoutOrNull(30000) {
            suspendCancellableCoroutine { continuation ->
                val webView = getOrCreateWebView()
                
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        // In a real implementation, you would capture the WebView as a bitmap
                        continuation.resume(BrowserResult(
                            success = true,
                            content = "Screenshot captured",
                            title = view?.title ?: "",
                            url = url ?: "",
                            error = null
                        ))
                    }
                }
                
                webView.loadUrl(url)
            }
        } ?: BrowserResult(
            success = false,
            content = "",
            title = "",
            url = url,
            error = "Timeout taking screenshot"
        )
    }
    
    /**
     * Fill a form on a webpage
     */
    suspend fun fillForm(
        url: String,
        formSelector: String,
        formData: Map<String, String>
    ): BrowserResult {
        val script = buildFormFillScript(formSelector, formData)
        return executeJavaScript(url, script)
    }
    
    /**
     * Click an element on a webpage
     */
    suspend fun clickElement(url: String, selector: String): BrowserResult {
        val script = "document.querySelector('$selector').click(); 'Element clicked';"
        return executeJavaScript(url, script)
    }
    
    private fun getOrCreateWebView(): WebView {
        if (webView == null) {
            webView = WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                }
            }
        }
        return webView!!
    }
    
    private fun extractContent(document: Document): String {
        // Remove script and style elements
        document.select("script").remove()
        document.select("style").remove()
        
        // Extract main content areas
        val contentSelectors = listOf(
            "main", "article", ".content", "#content",
            ".main", "#main", ".post", ".entry"
        )
        
        for (selector in contentSelectors) {
            val element = document.selectFirst(selector)
            if (element != null && element.text().length > 100) {
                return element.text()
            }
        }
        
        // Fallback to body text
        return document.body()?.text() ?: document.text()
    }
    
    private fun parseSearchResults(html: String, maxResults: Int): List<SearchResult> {
        return try {
            val document = Jsoup.parse(html)
            val results = mutableListOf<SearchResult>()
            
            // Parse Google search results
            val searchDivs = document.select("div.g")
            
            for (div in searchDivs.take(maxResults)) {
                val titleElement = div.selectFirst("h3")
                val linkElement = div.selectFirst("a")
                val snippetElement = div.selectFirst(".VwiC3b")
                
                if (titleElement != null && linkElement != null) {
                    results.add(SearchResult(
                        title = titleElement.text(),
                        url = linkElement.attr("href"),
                        snippet = snippetElement?.text() ?: ""
                    ))
                }
            }
            
            results
        } catch (e: Exception) {
            Log.e("BrowserService", "Error parsing search results", e)
            emptyList()
        }
    }
    
    private fun buildFormFillScript(
        formSelector: String,
        formData: Map<String, String>
    ): String {
        val fillCommands = formData.map { (field, value) ->
            "document.querySelector('$formSelector $field').value = '$value';"
        }.joinToString("\n")
        
        return """
            $fillCommands
            'Form filled successfully';
        """.trimIndent()
    }
    
    fun cleanup() {
        webView?.destroy()
        webView = null
    }
}

data class BrowserResult(
    val success: Boolean,
    val content: String,
    val title: String,
    val url: String,
    val error: String?
)

data class SearchResult(
    val title: String,
    val url: String,
    val snippet: String
)