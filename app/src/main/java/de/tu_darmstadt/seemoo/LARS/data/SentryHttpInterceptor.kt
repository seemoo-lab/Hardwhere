package de.tu_darmstadt.seemoo.LARS.data

import android.util.Log
import io.sentry.core.Breadcrumb
import io.sentry.core.Sentry
import io.sentry.core.SentryLevel
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.StatusLine.Companion.HTTP_CONTINUE
import okhttp3.internal.toLongOrDefault
import okio.Buffer
import okio.GzipSource
import java.io.EOFException
import java.io.IOException
import java.net.HttpURLConnection.HTTP_NOT_MODIFIED
import java.net.HttpURLConnection.HTTP_NO_CONTENT
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * This class is derived from okhttp3.logging.HttpLoggingInterceptor as workaround for
 * the non-working sentry integration for okhttp3. Thus it contains copied code from
 * okhttp3 itself to use internal functions that HttpLoggingInterceptor uses and we
 * need to implement the sentry breadcrump.
 *
 * Thus much of this code is copyright by okhttp3 and its contributors.
 */

/**
 * We should start logging http requests in sentry to make issues more valuable.
 * This requires a custom logger class as the sentry integration for okhttp3 is still in alpha and doesn't produce any results currently.
 */
class SentryHttpInterceptor: Interceptor {

    fun Response.headersContentLength(): Long {
        return headers["Content-Length"]?.toLongOrDefault(-1L) ?: -1L
    }


    @Volatile private var headersToRedact = emptySet<String>()
    fun redactHeader(name: String) {
        val newHeadersToRedact = TreeSet(String.CASE_INSENSITIVE_ORDER)
        newHeadersToRedact += headersToRedact
        newHeadersToRedact += name
        headersToRedact = newHeadersToRedact
    }
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBody = request.body
        val connection = chain.connection()
        var requestStartMessage =
            ("--> ${request.method} ${request.url}${if (connection != null) " " + connection.protocol() else ""}")
        requestBody?.run {
            requestStartMessage += " (${this.contentLength()}-byte body)"
        }

        val breadcrumb = Breadcrumb()
        breadcrumb.category = "http"
        breadcrumb.message = requestStartMessage
        breadcrumb.level = SentryLevel.INFO

        val headersSend = request.headers

        if (requestBody != null) {
            // Request body headers are only present when installed as a network interceptor. When not
            // already present, force them to be included (if available) so their values are known.
            requestBody.contentType()?.let {
                if (headersSend["Content-Type"] == null) {
                    breadcrumb.setData("send-Content-Type","$it")
                }
            }
            if (requestBody.contentLength() != -1L) {
                if (headersSend["Content-Length"] == null) {
                    breadcrumb.setData("send-Content-Length",requestBody.contentLength())
                }
            }
        }

//        for (i in 0 until headersSend.size) {
//            logHeader(true,headersSend, i, breadcrumb)
//        }

        if (requestBody == null || bodyHasUnknownEncoding(request.headers) || requestBody.isDuplex()) {
            // do nothing
        } else {
            val buffer = Buffer()
            requestBody.writeTo(buffer)

            val contentType = requestBody.contentType()
            val charset: Charset = contentType?.charset(UTF_8) ?: UTF_8
            if (buffer.isProbablyUtf8()) {
                breadcrumb.setData("send-body",buffer.readString(charset))
            } else {
                breadcrumb.setData("send-body","byte body omitted")
            }
        }

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            breadcrumb.setData("failed","$e")
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body!!
        val contentLength = responseBody.contentLength()
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"

        breadcrumb.setData("response-code",response.code)
        breadcrumb.setData("response-message",response.message)
        breadcrumb.setData("response-url","$response.request.url")
        breadcrumb.setData("response-size",bodySize)
        breadcrumb.setData("tookMs",tookMs)

        val headersRecv = response.headers
//        for (i in 0 until headersRecv.size) {
//            logHeader(false,headersRecv, i,breadcrumb)
//        }
        // log API rate limits
        headersRecv.get("X-RateLimit-Remaining")?.run {
            breadcrumb.setData("X-RateLimit-Remaining",this)
        }

        if (!response.promisesBody() || bodyHasUnknownEncoding(response.headers)) {
            // do nothing
            Log.d(this@SentryHttpInterceptor::class.java.name,"going non-body for sentry")
        } else {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            var buffer = source.buffer

            var gzippedLength: Long? = null
            if ("gzip".equals(headersRecv["Content-Encoding"], ignoreCase = true)) {
                gzippedLength = buffer.size
                GzipSource(buffer.clone()).use { gzippedResponseBody ->
                    buffer = Buffer()
                    buffer.writeAll(gzippedResponseBody)
                }
            }

            val contentType = responseBody.contentType()
            val charset: Charset = contentType?.charset(UTF_8) ?: UTF_8

            if (!buffer.isProbablyUtf8()) {
                return response
            }

            if (contentLength != 0L) {
                breadcrumb.setData("recv-body",buffer.clone().readString(charset).take(30))
            }

            if (gzippedLength != null){
                breadcrumb.setData("recv-body-l","(${buffer.size}-byte, $gzippedLength-gzipped-byte body)")
            } else {
                breadcrumb.setData("recv-body-l","(${buffer.size}-byte body)")
            }
        }
        Sentry.addBreadcrumb(breadcrumb)
        Log.d(this@SentryHttpInterceptor::class.java.name,"logged breadcrumb")
        return response
    }

    /**
     * Copied from [okhttp3.logging.utf8]
     */
    internal fun Buffer.isProbablyUtf8(): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = size.coerceAtMost(64)
            copyTo(prefix, 0, byteCount)
            for (i in 0 until 16) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (_: EOFException) {
            return false // Truncated UTF-8 sequence.
        }
    }

    /**
     * Adapted from from okhttp3.logging.HttpLoggingInterceptor
     */
    private fun logHeader(isSend: Boolean, headers: Headers, i: Int, breadcrumb: Breadcrumb) {
        val value = if (headers.name(i) in headersToRedact) "██" else headers.value(i)
        val headerName = if(isSend) {
            "send-${headers.name(i)}"
        } else {
            "recv-${headers.name(i)}"
        }
        breadcrumb.setData(headerName,value)
    }
    /**
     * Copied from okhttp3.logging.HttpLoggingInterceptor
     */
    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"] ?: return false
        return !contentEncoding.equals("identity", ignoreCase = true) &&
                !contentEncoding.equals("gzip", ignoreCase = true)
    }

    /**
     * Copied from [okhttp3.internal.http.HttpHeaders]
     */
    fun Response.promisesBody(): Boolean {
        // HEAD requests never yield a body regardless of the response headers.
        if (request.method == "HEAD") {
            return false
        }

        val responseCode = code
        if ((responseCode < HTTP_CONTINUE || responseCode >= 200) &&
            responseCode != HTTP_NO_CONTENT &&
            responseCode != HTTP_NOT_MODIFIED) {
            return true
        }

        // If the Content-Length or Transfer-Encoding headers disagree with the response code, the
        // response is malformed. For best compatibility, we honor the headers.
        if (headersContentLength() != -1L ||
            "chunked".equals(header("Transfer-Encoding"), ignoreCase = true)) {
            return true
        }

        return false
    }
}