package com.semicolon.ds.core;

import com.semicolon.ds.Constants;
import com.semicolon.ds.comms.BSClient;
import com.semicolon.ds.comms.ftp.DataSendingOperation;
import com.semicolon.ds.comms.ftp.FTPClient;
import com.semicolon.ds.comms.ftp.FTPServer;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class GNode {

    private final Logger LOG = Logger.getLogger(GNode.class.getName());

    private BSClient bsClient;

    private String userName;
    private String ipAddress;
    private int port;
    private MessageBroker messageBroker;
    private SearchManager searchManager;
    private FTPServer ftpServer;

    public GNode (String userName) throws Exception {

        try (final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            this.ipAddress = socket.getLocalAddress().getHostAddress();

        } catch (Exception e){
            throw new RuntimeException("Could not find host address");
        }
        this.ftpServer = new FTPServer(Constants.FTP_PORT);
        Thread t = new Thread(ftpServer);
        t.start();
        this.userName = userName;
        this.port = getFreePort();

        this.bsClient = new BSClient();
        this.messageBroker = new MessageBroker(ipAddress, port);

        this.searchManager = new SearchManager(this.messageBroker);

        messageBroker.start();

        LOG.fine("Gnode initiated on IP :" + ipAddress + " and Port :" + port);

    }

    public void init() {
        List<InetSocketAddress> targets = this.register();
        if(targets != null) {
            for (InetSocketAddress target: targets) {
                messageBroker.sendPing(target.getAddress().toString().substring(1), target.getPort());
            }
        }
    }

    private List<InetSocketAddress> register() {
        List<InetSocketAddress> targets = null;

        try{
            targets = this.bsClient.register(this.userName, this.ipAddress, this.port);

        } catch (IOException e) {
            LOG.severe("Registering Gnode failed");
            e.printStackTrace();
        }
        return targets;

    }

    public void unRegister() {
        try{
            this.bsClient.unRegister(this.userName, this.ipAddress, this.port);

        } catch (IOException e) {
            LOG.severe("Un-Registering Gnode failed");
            e.printStackTrace();
        }
    }

    public int doSearch(String keyword){
        return this.searchManager.doSearch(keyword);
    }

    public void getFile(int fileOption) throws Exception {

        SearchResult fileDetail = this.searchManager.getFileDetails(fileOption);
        System.out.println("The file you requested is " + fileDetail.getFileName());
        FTPClient ftpClient = new FTPClient(fileDetail.getAddress(),
                Constants.FTP_PORT, fileDetail.getFileName());
    }


    public String getUserName() {
        return userName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort(){
        return port;
    }

    private int getFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            int port = socket.getLocalPort();
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore IOException on close()
            }
            return port;
        } catch (IOException e) {
            LOG.severe("Getting free port failed");
            throw new RuntimeException("Getting free port failed");
        }
    }

    public void printRoutingTable(){
        this.messageBroker.getRoutingTable().print();
    }
}
