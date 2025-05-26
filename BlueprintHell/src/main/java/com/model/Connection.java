package com.model;

import javafx.geometry.Point2D;
import java.util.UUID;

public class Connection {

    private final String id;
    private final Port startPort;
    private final Port endPort;
    private final double length;

    public Connection(Port startPort, Port endPort) {
        if (startPort == null || endPort == null) {
            throw new IllegalArgumentException("Start and End ports cannot be null");
        }
        if (startPort.getType() != PortType.OUTPUT || endPort.getType() != PortType.INPUT) {
            throw new IllegalArgumentException("Connection must be from OUTPUT to INPUT port");
        }

        this.id = UUID.randomUUID().toString();
        this.startPort = startPort;
        this.endPort = endPort;
        this.length = new Point2D(startPort.getAbsoluteX(), startPort.getAbsoluteY())
                .distance(endPort.getAbsoluteX(), endPort.getAbsoluteY());

        startPort.setConnectedConnection(this);
        endPort.setConnectedConnection(this);
    }

    public String getId() {
        return id;
    }

    public Port getStartPort() {
        return startPort;
    }

    public Port getEndPort() {
        return endPort;
    }

    public double getLength() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}