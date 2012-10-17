package com.zaneli.qiita.model.response;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.zaneli.qiita.QiitaException;
import com.zaneli.qiita.QiitaExecutor;
import com.zaneli.qiita.model.response.QiitaResponse;

public class PageableResponse<T extends QiitaResponse> {

  private enum Rel {
    FIRST("first"), PREV("prev"), NEXT("next"), LAST("last");
    private final String value;
    private Rel(String value) {
      this.value = value;
    }
  }

  private final QiitaExecutor executor;
  private final Map<String, String> params;
  private final T[] contents;
  private final URI firstUrl;
  private final URI prevUrl;
  private final URI nextUrl;
  private final URI lastUrl;

  public PageableResponse(
      QiitaExecutor executor, Map<String, String> params, T[] contents, String[] linkHeaderValues) throws QiitaException {
    this.executor = executor;
    this.params = params;
    this.contents = contents;
    this.firstUrl = retrieveUrl(Rel.FIRST, linkHeaderValues);
    this.prevUrl = retrieveUrl(Rel.PREV, linkHeaderValues);
    this.nextUrl = retrieveUrl(Rel.NEXT, linkHeaderValues);
    this.lastUrl = retrieveUrl(Rel.LAST, linkHeaderValues);
  }

  public T[] getContents() {
    return contents;
  }

  @SuppressWarnings("unchecked")
  public PageableResponse<T> getFirst() throws IOException, QiitaException {
    if (firstUrl == null) {
      return new NullPageableResponse<T>(contents);
    }
    return executor.getPageableContents(firstUrl, params, (Class<T[]>) contents.getClass());
  }

  @SuppressWarnings("unchecked")
  public PageableResponse<T> getPrev() throws IOException, QiitaException {
    if (prevUrl == null) {
      return new NullPageableResponse<T>(contents);
    }
    return executor.getPageableContents(prevUrl, params, (Class<T[]>) contents.getClass());
  }

  @SuppressWarnings("unchecked")
  public PageableResponse<T> getNext() throws IOException, QiitaException {
    if (nextUrl == null) {
      return new NullPageableResponse<T>(contents);
    }
    return executor.getPageableContents(nextUrl, params, (Class<T[]>) contents.getClass());
  }

  @SuppressWarnings("unchecked")
  public PageableResponse<T> getLast() throws IOException, QiitaException {
    if (lastUrl == null) {
      return new NullPageableResponse<T>(contents);
    }
    return executor.getPageableContents(lastUrl, params, (Class<T[]>) contents.getClass());
  }

  private static URI retrieveUrl(Rel rel, String[] linkHeaderValues) throws QiitaException {
    Pattern pattern = Pattern.compile("^<(.+)>;\\s+rel=\"" + rel.value + "\"$");
    for (String linkHeaderValue : linkHeaderValues) {
      String[] splitedLinkHeaderValues = linkHeaderValue.split(",");
      for (String splitedLinkHeaderValue : splitedLinkHeaderValues) {
        Matcher matcher = pattern.matcher(splitedLinkHeaderValue.trim());
        if (matcher.matches()) {
          try {
            return new URI(matcher.group(1));
          } catch (URISyntaxException e) {
            throw new QiitaException(e);
          }
        }
      }
    }
    return null;
  }

  private static class NullPageableResponse<T extends QiitaResponse> extends PageableResponse<T> {
    private final T[] emptyContent;
    @SuppressWarnings("unchecked")
    private NullPageableResponse(T[] orgContents) throws QiitaException {
      super(null, Collections.<String, String>emptyMap(), null, new String[0]);
      this.emptyContent = (T[]) Array.newInstance(orgContents[0].getClass(), 0);
    }
    @Override
    public T[] getContents() {
      return emptyContent;
    }
    @Override
    public PageableResponse<T> getFirst() {
      return this;
    }
    @Override
    public PageableResponse<T> getPrev() {
      return this;
    }
    @Override
    public PageableResponse<T> getNext() {
      return this;
    }
    @Override
    public PageableResponse<T> getLast() {
      return this;
    }
  }
}
