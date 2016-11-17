package com.vmware.corfukv;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.corfudb.runtime.CorfuRuntime;
import org.corfudb.runtime.collections.SMRMap;
import org.corfudb.runtime.view.ObjectsView;

import java.io.IOException;
import java.util.Map;

/**
 * Created by jrosoff on 11/16/16.
 */
public class CorfuKVServer {

    private final Server server;
    private final CorfuRuntime corfu;
    private final Map<String, String> store;

    public CorfuKVServer() {

        corfu = new CorfuRuntime("localhost:9000").connect();
        store = corfu.getObjectsView()
                .build()
                .setStreamName("store")
                .setType(SMRMap.class)
                .open();

        server = ServerBuilder.forPort(8080)
                .addService(new CorfuKVService(store))
                .build();
    }

    public void start() throws IOException {
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** Shutting down since jvm is shutting down");
                CorfuKVServer.this.stop();
                System.err.println("*** Server shut down");
            }
        });
    }

    public void stop() {
        if( server != null ) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if(server != null ) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws Exception {
        CorfuKVServer server = new CorfuKVServer();
        server.start();
        server.blockUntilShutdown();
    }

    public static class CorfuKVService extends KeyValueGrpc.KeyValueImplBase {

        private Map<String, String> store;

        public CorfuKVService(Map<String,String> store) {
            this.store = store;
        }
        @Override
        public void get(GetRequest request, StreamObserver<GetReply> responseObserver) {
            System.out.println("Get!");
            String key = request.getKey();
            String value = store.get( key );
            GetReply reply = GetReply.newBuilder()
                    .setKey(key)
                    .setValue(value)
                    .build();

            responseObserver.onNext( reply );
            responseObserver.onCompleted();
        }

        @Override
        public void set(SetRequest request, StreamObserver<SetReply> responseObserver) {
            System.out.println("Set!");
            String key = request.getKey();
            String value = store.put( key, request.getValue() );
            SetReply reply = SetReply.newBuilder().setOk(true).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
