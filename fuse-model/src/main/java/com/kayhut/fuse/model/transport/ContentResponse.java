package com.kayhut.fuse.model.transport;

import com.kayhut.fuse.model.results.TextContent;
import org.jooby.Status;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Created by lior on 19/02/2017.
 */
public class ContentResponse<T> implements Response, TextContent {
    public static <T> ContentResponse<T> notFound() {
        return Builder.<T>builder(Status.NOT_FOUND, Status.NOT_FOUND)
                .data(Optional.empty())
                .compose();
    }

    public static <T> ContentResponse<T> internalError(Exception ex) {
        return Builder.<T>builder(Status.SERVER_ERROR, Status.SERVER_ERROR)
                .data(Optional.empty())
                .error(ex)
                .compose();
    }

    //region Constructors
    public ContentResponse() {}
    //endregion

    //region Properties
    public String getRequestId() {
        return this.requestId;
    }

    public String getExternalRequestId() {
        return this.externalRequestId;
    }

    public long elapsed() {
        return this.elapsed;
    }

    public T getData() {
        return this.data;
    }

    public Status status() {
        return this.status;
    }

    public Exception error() {
        return this.error;
    }

    @Override
    public String content() {
        if(this.data != null && TextContent.class.isAssignableFrom(this.data.getClass())){
            return ((TextContent)this.data).content();
        }else{
            return this.toString();
        }
    }
    //endregion

    @Override
    public String toString() {
        return String.format("ContentResponse{status=%s, requestId=%s, externalRequestId=%s, elapsed=%d, data=%s}",
                status,
                requestId,
                externalRequestId,
                elapsed,
                data);
    }

    //region Fields
    private Status status = Status.NOT_FOUND;
    private String requestId;
    private String externalRequestId;
    private long elapsed;
    private T data;
    private Exception error;
    //endregion

    //region Builder
    public static class Builder<T> {
        public static <S> Builder<S> builder(Status success, Status fail) {
            return new Builder<>(success, fail);
        }

        public static <S> Builder<S> builder(ContentResponse<S> response) {
            return new Builder<>(response);
        }

        //region Constructors
        public Builder(Status success, Status fail) {
            this.response = new ContentResponse<>();
            this.success = success;
            this.fail = fail;

            this.successPredicate = response1 -> response1.data != null && response1.error == null;
        }

        public Builder(ContentResponse<T> response) {
            this.response = response;
            this.success = response.status;
            this.fail = response.status;

            this.successPredicate = response1 -> response1.data != null && response1.error == null;
        }
        //endregion

        //region Properties
        public Builder<T> requestId(String requestId) {
            this.response.requestId = requestId;
            return this;
        }

        public Builder<T> externalRequestId(String externalRequestId) {
            this.response.externalRequestId = externalRequestId;
            return this;
        }

        public Builder<T> elapsed(long elapsed) {
            this.response.elapsed = elapsed;
            return this;
        }

        public Builder<T> data(Optional<T> data) {
            this.response.data = data.orElse(null);
            return this;
        }

        public Builder<T> error(Exception ex) {
            this.response.error = ex;
            return this;
        }

        public Builder<T> successPredicate(Predicate<ContentResponse<T>> successPredicate) {
            this.successPredicate = successPredicate;
            return this;
        }

        public ContentResponse<T> compose() {
            if(this.successPredicate.test(this.response)) {
                response.status = success;
            } else {
                response.status = fail;
            }

            return response;
        }
        //endregion

        //region Fields
        private Status success;
        private Status fail;
        private ContentResponse<T> response;
        private Predicate<ContentResponse<T>> successPredicate;
        //endregion
    }
    //endregion
}
