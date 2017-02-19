package com.kayhut.fuse.model;

import com.kayhut.fuse.model.transport.UrlContent;

/**
 * Created by lior on 19/02/2017.
 */
public class Path extends UrlContent {
    private String data;

    public Path() {}

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public long getResults() {
        return data.length();
    }

    @Override
    public String getData() {
        return data;
    }

    public static class PathBuilder {
        private Path path;

        public PathBuilder(String id) {
            path = new Path();
            path.setId(id);
        }

        public static PathBuilder builder(String id) {
            PathBuilder builder = new PathBuilder(id);
            return builder;
        }

        public PathBuilder data(String data) {
            this.path.setData(data);
            this.path.setCompleted(true);
            return this;
        }

        public PathBuilder url(String url) {
            this.path.setUrl(url);
            this.path.setUrl("http://localhost:8080/fuse"+url +"/"+path.getId());
            return this;
        }

        public Path compose() {
            return path;
        }
    }

}
