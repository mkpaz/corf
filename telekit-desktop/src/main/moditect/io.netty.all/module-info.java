module io.netty.all {

    requires java.logging;
    requires java.naming;
    requires java.xml;

    requires transitive jdk.sctp;
    requires transitive jdk.unsupported;

    // optional dependencies
    requires static org.apache.commons.logging;
    requires static org.bouncycastle.pkix;
    requires static org.bouncycastle.provider;
    requires static org.slf4j;
    requires static jzlib;

    // io.netty.channel.rxtx                  -> gnu.io
    // io.netty.channel.udt                   -> com.barchart.udt
    // io.netty.handler.codec.compression     -> com.aayushatharva.brotli4j
    // io.netty.handler.codec.compression     -> net.jpountz.lz4
    // io.netty.handler.codec.compression     -> com.ning.compress
    // io.netty.handler.codec.compression     -> lzma.sdk
    // io.netty.handler.codec.compression     -> com.github.luben.zstd
    // io.netty.handler.codec.marshalling     -> org.jboss.marshalling
    // io.netty.handler.codec.protobuf        -> com.google.protobuf
    // io.netty.handler.codec.xml             -> com.fasterxml.aalto
    // io.netty.handler.ssl                   -> org.conscrypt
    // io.netty.handler.ssl                   -> io.netty.internal.tcnative
    // io.netty.handler.ssl                   -> org.eclipse.jetty.alpn
    // io.netty.util                          -> com.oracle.svm.core.annotate
    // io.netty.util.internal                 -> reactor.blockhound
    // io.netty.util.internal.logging         -> org.apache.logging.log4j
    // io.netty.util.internal.svm             -> com.oracle.svm.core.annotate

    exports io.netty.bootstrap;
    exports io.netty.buffer;
    exports io.netty.buffer.search;
    exports io.netty.channel;
    exports io.netty.channel.embedded;
    exports io.netty.channel.epoll;
    exports io.netty.channel.group;
    exports io.netty.channel.internal;
    exports io.netty.channel.kqueue;
    exports io.netty.channel.local;
    exports io.netty.channel.nio;
    exports io.netty.channel.oio;
    exports io.netty.channel.pool;
    exports io.netty.channel.rxtx;
    exports io.netty.channel.sctp;
    exports io.netty.channel.sctp.nio;
    exports io.netty.channel.sctp.oio;
    exports io.netty.channel.socket;
    exports io.netty.channel.socket.nio;
    exports io.netty.channel.socket.oio;
    exports io.netty.channel.udt;
    exports io.netty.channel.udt.nio;
    exports io.netty.channel.unix;
    exports io.netty.handler.address;
    exports io.netty.handler.codec;
    exports io.netty.handler.codec.base64;
    exports io.netty.handler.codec.bytes;
    exports io.netty.handler.codec.compression;
    exports io.netty.handler.codec.dns;
    exports io.netty.handler.codec.haproxy;
    exports io.netty.handler.codec.http;
    exports io.netty.handler.codec.http.cookie;
    exports io.netty.handler.codec.http.cors;
    exports io.netty.handler.codec.http.multipart;
    exports io.netty.handler.codec.http.websocketx;
    exports io.netty.handler.codec.http.websocketx.extensions;
    exports io.netty.handler.codec.http.websocketx.extensions.compression;
    exports io.netty.handler.codec.http2;
    exports io.netty.handler.codec.json;
    exports io.netty.handler.codec.marshalling;
    exports io.netty.handler.codec.memcache;
    exports io.netty.handler.codec.memcache.binary;
    exports io.netty.handler.codec.mqtt;
    exports io.netty.handler.codec.protobuf;
    exports io.netty.handler.codec.redis;
    exports io.netty.handler.codec.rtsp;
    exports io.netty.handler.codec.sctp;
    exports io.netty.handler.codec.serialization;
    exports io.netty.handler.codec.smtp;
    exports io.netty.handler.codec.socks;
    exports io.netty.handler.codec.socksx;
    exports io.netty.handler.codec.socksx.v4;
    exports io.netty.handler.codec.socksx.v5;
    exports io.netty.handler.codec.spdy;
    exports io.netty.handler.codec.stomp;
    exports io.netty.handler.codec.string;
    exports io.netty.handler.codec.xml;
    exports io.netty.handler.flow;
    exports io.netty.handler.flush;
    exports io.netty.handler.ipfilter;
    exports io.netty.handler.logging;
    exports io.netty.handler.pcap;
    exports io.netty.handler.proxy;
    exports io.netty.handler.ssl;
    exports io.netty.handler.ssl.ocsp;
    exports io.netty.handler.ssl.util;
    exports io.netty.handler.stream;
    exports io.netty.handler.timeout;
    exports io.netty.handler.traffic;
    exports io.netty.resolver;
    exports io.netty.resolver.dns;
    exports io.netty.resolver.dns.macos;
    exports io.netty.util;
    exports io.netty.util.collection;
    exports io.netty.util.concurrent;
    exports io.netty.util.internal;
    exports io.netty.util.internal.logging;
    exports io.netty.util.internal.shaded.org.jctools.queues;
    exports io.netty.util.internal.shaded.org.jctools.queues.atomic;
    exports io.netty.util.internal.shaded.org.jctools.util;
    exports io.netty.util.internal.svm;
}
