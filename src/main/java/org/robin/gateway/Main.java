package org.robin.gateway;

import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.robin.gateway.server.GatewayServer;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Map config = initConfig();

        List<Map<String,String>> subServerList = initSubServerList(config);

        Map server = (Map) config.get("server");
        GatewayServer gatewayServer = new GatewayServer();
        if(server.get("port")!=null) {
            String port = String.valueOf(server.get("port"));
            gatewayServer.setPort(Integer.valueOf(port));
        }

        if(server.get("path")!=null){
            String path = String.valueOf(server.get("path"));
            gatewayServer.setPath(path);
        }

        for(Map<String,String> upstreamServer : subServerList){
            String weight = upstreamServer.get("weight");
            for(int i = 0;i<Integer.valueOf(weight);i++){
                String host = upstreamServer.get("host");
                String p = upstreamServer.get("port");
                String path = upstreamServer.get("path");
                if(host!=null && p!=null) {
                    gatewayServer.getSubServerList().add(new InetSocketAddress(host, Integer.valueOf(p)));
                }else if(path!=null){
                    gatewayServer.getSubServerList().add(new AFUNIXSocketAddress(new File(path)));
                }
            }
        }

        gatewayServer.start();
    }


    static Map initConfig() throws IOException {
        HashMap config = new HashMap();

        Yaml yaml = new Yaml();
        try {
            InputStream input = Main.class.getClassLoader().getResourceAsStream("application.yml");
            config = yaml.loadAs(input, HashMap.class);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Init yaml failed !");
            System.exit(1);
        }

        File directory = new File(".netty-gateway/");
        if(!directory.exists()){
            directory.mkdirs();
        }

        File userDefinedConfig = new File(directory,"application.yml");
        if(!userDefinedConfig.exists()){
            userDefinedConfig.createNewFile();
        }else{
            HashMap newConfig = yaml.loadAs(new FileInputStream(userDefinedConfig), HashMap.class);
            System.out.println(newConfig);
            if(newConfig!=null) {
                config.putAll(newConfig);
            }
        }
        return config;
    }


    static List<Map<String,String>> initSubServerList(Map config){
        List<Map<String,String>> result = new ArrayList();
        Map proxy = (Map) config.get("proxy");
        for(Map.Entry<String,Map> keyAndValue : (Set<Map.Entry<String,Map>>)proxy.entrySet()){
            Map<String,String> address = new HashMap();
            Map value = keyAndValue.getValue();
            if(value.get("host")!=null && value.get("port")!=null) {
                String host = String.valueOf(value.get("host"));
                String port = String.valueOf(value.get("port"));
                address.put("host",host);
                address.put("port",port);
            }else if(value.get("path")!=null) {
                String path = String.valueOf(value.get("path"));
                address.put("path",path);
            }

            String weight = value.get("weight")!=null?String.valueOf(value.get("weight")):"1";
            address.put("weight",weight);
            result.add(address);
        }
        return result;

    }
}
