package dev.mert.tvassistant;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/** Read-only lookup of published Pamela releases. It never downloads or installs an APK. */
public final class UpdateChecker {
  private static final String RELEASES =
      "https://api.github.com/repos/roquemontoya/tv-assistant/releases?per_page=20";
  private static final int TIMEOUT_MS = 10000;

  public interface Listener {
    void onResult(Release release);
    void onError(String message);
  }

  public static final class Release {
    public final String tag;
    public final String name;
    public final String notes;
    public final String apkUrl;
    public final String sha256;
    public final long versionCode;
    public final boolean prerelease;

    Release(String tag, String name, String notes, String apkUrl, String sha256,
        long versionCode, boolean prerelease) {
      this.tag = tag;
      this.name = name;
      this.notes = notes;
      this.apkUrl = apkUrl;
      this.sha256 = sha256;
      this.versionCode = versionCode;
      this.prerelease = prerelease;
    }
  }

  private UpdateChecker() {}

  public static void check(Context context, boolean includePrereleases, Listener listener) {
    final Context app = context.getApplicationContext();
    new Thread(() -> {
      HttpURLConnection connection = null;
      try {
        connection = (HttpURLConnection) new URL(RELEASES).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestProperty("Accept", "application/vnd.github+json");
        connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
        int status = connection.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
          throw new IllegalStateException("GitHub respondió HTTP " + status);
        }
        StringBuilder body = new StringBuilder();
        try (InputStream in = connection.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
          String line;
          while ((line = reader.readLine()) != null) body.append(line);
        }
        Release newest = findCompatibleRelease(app, new JSONArray(body.toString()), includePrereleases);
        listener.onResult(newest);
      } catch (Exception error) {
        listener.onError(error.getMessage() == null ? "No se pudo consultar GitHub." : error.getMessage());
      } finally {
        if (connection != null) connection.disconnect();
      }
    }, "pamela-update-check").start();
  }

  private static Release findCompatibleRelease(Context context, JSONArray releases,
      boolean includePrereleases) throws Exception {
    PackageInfo installed = packageInfo(context);
    String currentVersion = installed.versionName == null ? "0" : installed.versionName;
    for (int i = 0; i < releases.length(); i++) {
      JSONObject item = releases.optJSONObject(i);
      if (item == null || item.optBoolean("draft", false)) continue;
      boolean prerelease = item.optBoolean("prerelease", false);
      if (prerelease && !includePrereleases) continue;
      JSONArray assets = item.optJSONArray("assets");
      if (assets == null) continue;
      String apkUrl = null;
      String checksum = null;
      for (int j = 0; j < assets.length(); j++) {
        JSONObject asset = assets.optJSONObject(j);
        if (asset == null) continue;
        String name = asset.optString("name", "");
        String url = asset.optString("browser_download_url", "");
        if (name.endsWith(".apk") && url.startsWith("https://")) apkUrl = url;
        if (name.equalsIgnoreCase("SHA256SUMS") || name.equalsIgnoreCase("SHA256SUMS.txt")) {
          checksum = url;
        }
      }
      if (apkUrl == null) continue;
      String tag = item.optString("tag_name", "");
      if (compareVersions(tag, currentVersion) <= 0) continue;
      return new Release(
          item.optString("tag_name", ""),
          item.optString("name", item.optString("tag_name", "Nueva versión")),
          item.optString("body", ""),
          apkUrl,
          checksum,
          0,
          prerelease);
    }
    return null;
  }


  private static int compareVersions(String candidate, String installed) {
    String[] left = candidate.replaceFirst("^[vV]", "").split("[-+]", 2)[0].split("\\\\.");
    String[] right = installed.replaceFirst("^[vV]", "").split("[-+]", 2)[0].split("\\\\.");
    int count = Math.max(left.length, right.length);
    for (int i = 0; i < count; i++) {
      int a = i < left.length ? leadingNumber(left[i]) : 0;
      int b = i < right.length ? leadingNumber(right[i]) : 0;
      if (a != b) return Integer.compare(a, b);
    }
    return 0;
  }

  private static int leadingNumber(String value) {
    String digits = value.replaceFirst("^([0-9]+).*$", "$1");
    try { return Integer.parseInt(digits); } catch (Exception ignored) { return 0; }
  }

  @SuppressWarnings("deprecation")
  private static PackageInfo packageInfo(Context context) throws Exception {
    PackageManager manager = context.getPackageManager();
    if (Build.VERSION.SDK_INT >= 33) {
      return manager.getPackageInfo(context.getPackageName(),
          PackageManager.PackageInfoFlags.of(0));
    }
    return manager.getPackageInfo(context.getPackageName(), 0);
  }
}
