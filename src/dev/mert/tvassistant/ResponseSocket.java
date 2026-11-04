package dev.mert.tvassistant;

import java.io.InterruptedIOException;
import java.util.Iterator;
import org.json.*;

/**
 * HTTPS/SSE-only Responses transport.
 *
 * Java-WebSocket 1.6.0 is temporarily excluded because CVE-2026-90584 affects its
 * fragmented-frame handling. Do not re-enable WebSocket until a fixed upstream release
 * is available and its frame-size limits are verified.
 */
final class ResponseSocket implements AutoCloseable {
  private volatile boolean closed;
  int requests, resets;
  String transport = "https-sse";

  ResponseSocket(String token) {}

  static JSONObject payload(JSONObject request, JSONArray full, int cursor, String previous)
      throws Exception {
    // Do not stringify/copy old screenshots and tool history just to discard them below.
    JSONObject body = new JSONObject();
    Iterator<String> keys = request.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      if (!key.equals("input") && !key.equals("stream") && !key.equals("background")
          && !key.equals("previous_response_id")) body.put(key, request.get(key));
    }
    body.put("type", "response.create");
    if (!previous.isEmpty()) {
      if (cursor < 0 || cursor > full.length())
        throw new IllegalArgumentException("Invalid continuation position");
      JSONArray delta = new JSONArray();
      for (int i = cursor; i < full.length(); i++) delta.put(full.get(i));
      body.put("input", delta).put("previous_response_id", previous);
    } else {
      body.remove("previous_response_id");
      body.put("input", full);
    }
    return body;
  }


  JSONObject create(String token, JSONObject request, Net.Events events) throws Exception {
    if (closed || events.cancelled()) throw new InterruptedIOException("Task stopped");
    requests++;
    transport = "https-sse";
    return Net.stream(token, request, events);
  }

  public void close() {
    closed = true;
  }
}
