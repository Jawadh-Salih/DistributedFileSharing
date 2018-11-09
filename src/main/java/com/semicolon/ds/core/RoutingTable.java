package com.semicolon.ds.core;


import com.semicolon.ds.Constants;

import java.util.ArrayList;
import java.util.logging.Logger;


public class RoutingTable {

    private final Logger LOG = Logger.getLogger(RoutingTable.class.getName());
    private ArrayList<Neighbour> neighbours;
    private final String address;
    private final int port;

    public RoutingTable(String address, int port) {
        this.address = address;
        this.port = port;
        this.neighbours = new ArrayList<>();
    }

    public synchronized int addNeighbour(String address, int port) {
        for (Neighbour n: neighbours) {
            if (n.equals(address, port)){
                n.Ping();
                return neighbours.size();
            }
        }
        if (neighbours.size() >= Constants.MAX_NEIGHBOURS) {
            return 0;
        }
        neighbours.add(new Neighbour(address, port));

        LOG.info("Adding neighbour : " + address + ":" + port);
        return neighbours.size();
    }

    public synchronized int removeNeighbour(String address, int port) {
        Neighbour toRemove = null;
        for (Neighbour n: neighbours) {
            if (n.equals(address, port)) {
                toRemove = n;
            }
        }
        if (toRemove != null) {
            neighbours.remove(toRemove);
            return neighbours.size();
        }
        return 0;
    }
    public synchronized int getCount() {
        return neighbours.size();
    }

    public synchronized void print() {
        System.out.println("Total neighbours: " + neighbours.size());
        System.out.println("++++++++++++++++++++++++++");
        for (Neighbour n :neighbours) {
            System.out.println(
                    "Address: " + n.getAddress()
                    + " Port: " + n.getPort()
                    + " Pings: " + n.getPingPongs()
            );
        }
    }

    public synchronized ArrayList<String> toList() {
        ArrayList<String> list = new ArrayList<>();
        for (Neighbour n: neighbours) {
            list.add(n.toString());
        }
        return list;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public ArrayList<Neighbour> getNeighbours() {
        return neighbours;
    }
}
