package com.vmware.corfukv;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.grpc.ManagedChannel;
import com.vmware.corfukv.KeyValueGrpc.KeyValueBlockingStub;
import io.grpc.ManagedChannelBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * Created by jrosoff on 11/16/16.
 */
public class CorfuKVClient {

    private final ManagedChannel channel;
    private final KeyValueBlockingStub blockingStub;


    public CorfuKVClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host,port).usePlaintext(true).build();
        blockingStub = KeyValueGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public String get(String key) {
        GetRequest req = GetRequest.newBuilder().setKey(key).build();
        GetReply reply = blockingStub.get(req);
        return reply.getValue();
    }

    public boolean set(String key, String value ) {
        SetRequest req = SetRequest.newBuilder().setKey(key).setValue(value).build();
        SetReply reply = blockingStub.set(req);
        return reply.getOk();
    }

    // CLI Commands
    // get - get a key
    // set - set a key value pair

    @Parameters(commandDescription = "Get the value of a key")
    public static class CommandGet {

        @Parameter(names="-key", description = "Key to get")
        private String key;
    }

    @Parameters(commandDescription = "Set a key,value pair")
    public static class CommandSet {
        @Parameter(names="-key", description="Key to set")
        private String key;

        @Parameter(names="-value",description = "Value to set")
        private String value;

    }


    public static void main(String[] args) throws InterruptedException {

        CommandGet get = new CommandGet();
        CommandSet set = new CommandSet();

        JCommander cmd = new JCommander();
        cmd.addCommand("get", get);
        cmd.addCommand("set", set);
        cmd.parse(args);

        CorfuKVClient client = new CorfuKVClient("localhost", 8080);

        try {
            switch (cmd.getParsedCommand()) {
                case "get":
                    String value = client.get(get.key);
                    System.out.println(value);
                    break;
                case "set":
                    boolean result = client.set(set.key, set.value);
                    System.out.println(result);
                    break;
                default:
                    System.out.println("Whoops. not a command");
            }
        } finally {
            client.shutdown();
        }
    }

}
