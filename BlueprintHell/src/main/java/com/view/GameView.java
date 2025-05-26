package com.view;

import com.blueprinthell.Config;
import com.model.*;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GameView extends Pane {

    private final Map<Packet, Node> packetNodes = new HashMap<>();
    private final Map<Port, Shape> portShapes = new HashMap<>();

    public GameView(double windowWidth, double gameAreaHeight) {
        this.setStyle("-fx-background-color: #" + Config.COLOR_BACKGROUND.toString().substring(2) + ";");
        this.setPrefSize(windowWidth, gameAreaHeight);
        this.setMinWidth(Math.max(0, windowWidth - Config.SHOP_VIEW_DEFAULT_WIDTH - 50));
        this.setMaxSize(windowWidth, gameAreaHeight);
        System.out.println("GameView constructed. PrefWidth: " + getPrefWidth() + " MinWidth: " + getMinWidth());
    }

    public void redrawAll(GameState gameState) {
        this.getChildren().clear();
        packetNodes.clear();
        portShapes.clear();
        drawConnections(gameState.getConnections());
        drawSystems(gameState.getSystems());
        drawPackets(gameState.getPackets());
    }

    private void drawConnections(List<Connection> connections) {
        for (Connection connection : connections) {
            drawSingleWire(connection);
        }
    }

    private void drawSystems(List<SystemNode> systems) {
        for (SystemNode system : systems) {
            drawSingleSystem(system);
        }
    }

    private void drawPackets(List<Packet> packets) {
        for (Packet packet : packets) {
            drawSinglePacket(packet);
        }
    }

    private void drawSingleWire(Connection connection) {
        Line wireLine = new Line(
                connection.getStartPort().getAbsoluteX(),
                connection.getStartPort().getAbsoluteY(),
                connection.getEndPort().getAbsoluteX(),
                connection.getEndPort().getAbsoluteY()
        );
        wireLine.setStroke(Config.COLOR_WIRE_NORMAL);
        wireLine.setStrokeWidth(Config.WIRE_THICKNESS);
        wireLine.setMouseTransparent(true);
        this.getChildren().add(wireLine);
    }

    private void drawSingleSystem(SystemNode system) {
        Rectangle systemBody = new Rectangle(system.getX(), system.getY(), system.getWidth(), system.getHeight());
        systemBody.setFill(Config.COLOR_SYSTEM_RECT);
        systemBody.setStroke(Color.BLACK);
        systemBody.setStrokeWidth(1);
        systemBody.setArcWidth(Config.SYSTEM_CORNER_ARC);
        systemBody.setArcHeight(Config.SYSTEM_CORNER_ARC);
        this.getChildren().add(systemBody);

        Line divider = new Line(
                system.getX() + Config.SYSTEM_DIVIDER_X_OFFSET, system.getY() + 3,
                system.getX() + Config.SYSTEM_DIVIDER_X_OFFSET, system.getY() + system.getHeight() - 3
        );
        divider.setStroke(Config.COLOR_SYSTEM_DIVIDER);
        divider.setStrokeWidth(1);
        this.getChildren().add(divider);

        drawIndicatorForSystem(system);
        drawPortsForSystem(system.getInputPorts());
        drawPortsForSystem(system.getOutputPorts());

        Label storedCountLabel = new Label();
        String labelText = system.getStoredPacketCount() + "/" + Config.SYSTEM_PACKET_STORAGE_CAPACITY;
        storedCountLabel.setText(labelText);
        storedCountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        storedCountLabel.setTextFill(Color.WHITE);

        double labelX = system.getX() + Config.SYSTEM_DIVIDER_X_OFFSET / 2.0 - storedCountLabel.prefWidth(-1) / 2.0;
        double labelY = system.getY() + Config.INDICATOR_Y_POS + Config.INDICATOR_HEIGHT + 2;

        labelX = system.getX() + 3;
        labelY = system.getY() + system.getHeight() - 12 - 3;

        storedCountLabel.setLayoutX(labelX);
        storedCountLabel.setLayoutY(labelY);
        storedCountLabel.setMouseTransparent(true);

        this.getChildren().add(storedCountLabel);
    }

    private void drawPortsForSystem(List<Port> ports) {
        for (Port port : ports) {
            Shape portShape = createPortShape(port);
            if (portShape != null) {
                portShape.setStroke(Color.BLACK);
                portShape.setStrokeWidth(0.5);
                port.setVisualShape(portShape);
                this.getChildren().add(portShape);
            }
        }
    }

    private Shape createPortShape(Port port) {
        double size;
        double cx = port.getAbsoluteX();
        double cy = port.getAbsoluteY();

        if (port.getPacketType() == PacketType.SQUARE) {
            size = Config.PORT_VISUAL_SIZE_SQUARE;
            Rectangle squarePort = new Rectangle(cx - size, cy - size, size * 2, size * 2);
            squarePort.setFill(Config.COLOR_PORT_SQUARE);
            return squarePort;
        } else if (port.getPacketType() == PacketType.TRIANGLE) {
            size = Config.PORT_VISUAL_SIZE_TRIANGLE;
            Polygon trianglePort = new Polygon();
            double x1, y1, x2, y2, x3, y3;
            double hf = 1.0, bf = 0.866;
            if (port.getType() == PortType.INPUT) { x1=cx+size*hf*0.6; y1=cy; x2=cx-size*hf*0.4; y2=cy-size*bf; x3=cx-size*hf*0.4; y3=cy+size*bf;}
            else { x1=cx-size*hf*0.6; y1=cy; x2=cx+size*hf*0.4; y2=cy-size*bf; x3=cx+size*hf*0.4; y3=cy+size*bf;}
            trianglePort.getPoints().addAll(x1, y1, x2, y2, x3, y3);
            trianglePort.setFill(Config.COLOR_PORT_TRIANGLE);
            return trianglePort;
        } else {
            return new Circle(cx, cy, Config.PORT_BASE_SIZE_UNIT * 0.5, Color.GRAY);
        }
    }


    public Port findPortAt(double screenX, double screenY) {
        for (Map.Entry<Port, Shape> entry : portShapes.entrySet()) {
            Shape shape = entry.getValue();
            if (shape.contains(screenX, screenY)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void drawIndicatorForSystem(SystemNode system) {
        Rectangle indicatorRect = new Rectangle();
        double indicatorWidth = Config.SYSTEM_DIVIDER_X_OFFSET - 2 * Config.INDICATOR_PADDING_X;
        indicatorRect.setX(system.getX() + Config.INDICATOR_PADDING_X);
        indicatorRect.setY(system.getY() + Config.INDICATOR_Y_POS);
        indicatorRect.setWidth(indicatorWidth);
        indicatorRect.setHeight(Config.INDICATOR_HEIGHT);

        if (system.isAllPortsActuallyConnected()) {
            indicatorRect.setFill(Config.COLOR_SYSTEM_INDICATOR_ON);
        } else {
            indicatorRect.setFill(Config.COLOR_SYSTEM_INDICATOR_OFF);
        }

        indicatorRect.setArcWidth(3);
        indicatorRect.setArcHeight(3);
        this.getChildren().add(indicatorRect);
    }

    private void drawSinglePacket(Packet packet) {
        Node packetNode = packetNodes.get(packet);
        Label sizeLabel = packet.getSizeLabel();

        if (packetNode == null) {
            packetNode = createPacketNode(packet);
            if (packetNode != null) {
                packet.setVisualNode(packetNode);
                this.getChildren().add(packetNode);
                packetNodes.put(packet, packetNode);

                if (sizeLabel != null && !this.getChildren().contains(sizeLabel)) {
                    this.getChildren().add(sizeLabel);
                    packet.updateLabelPosition();
                }
            }
        }
    }

    private Node createPacketNode(Packet packet) {
        double size;
        double x = packet.getX();
        double y = packet.getY();
        Node node = null;

        if (packet.getType() == PacketType.SQUARE) {
            size = Config.PORT_VISUAL_SIZE_SQUARE * 2;
            Rectangle rect = new Rectangle(x - size / 2.0, y - size / 2.0, size, size);
            rect.setFill(Config.COLOR_PACKET_SQUARE);
            node = rect;
        } else if (packet.getType() == PacketType.TRIANGLE) {
            size = Config.PORT_VISUAL_SIZE_TRIANGLE * 1.8;
            Polygon poly = new Polygon();
            double height = size * Math.sqrt(3) / 2.0;
            poly.getPoints().addAll( x, y - height * (2.0/3.0), x - size / 2.0, y + height * (1.0/3.0), x + size / 2.0, y + height * (1.0/3.0) );
            poly.setFill(Config.COLOR_PACKET_TRIANGLE);
            node = poly;
        }

        if(node instanceof Shape) { ((Shape)node).setStroke(Color.DARKSLATEGRAY); ((Shape)node).setStrokeWidth(0.5); }
        return node;
    }

    public Node getNodeForPacket(Packet packet) {
        return packetNodes.get(packet);
    }

    public void addNode(Node node) {
        if (node != null && !this.getChildren().contains(node)) {
            if (node instanceof Line) { this.getChildren().add(0, node); }
            else { this.getChildren().add(node); }
        }
    }

    public void removePacketNode(Packet packet) {
        if (packet != null) {
            Node nodeToRemove = packetNodes.remove(packet);
            if (nodeToRemove != null) {
                this.getChildren().remove(nodeToRemove);
            }
        }
    }

    public void removeNode(Node node) {
        if (node != null) { this.getChildren().remove(node); }
    }

    public void updatePacketPositions(GameState gameState) {
        for (Packet packet : gameState.getPackets()) {
            Node node = packetNodes.get(packet);
            Label label = packet.getSizeLabel();

            if (node != null) {
                node.relocate(packet.getX() - node.getBoundsInLocal().getWidth() / 2.0,
                        packet.getY() - node.getBoundsInLocal().getHeight() / 2.0);
                if (label != null) {
                    packet.updateLabelPosition();
                }
            } else {
                drawSinglePacket(packet);
            }
        }
        List<Packet> currentPackets = gameState.getPackets();
        packetNodes.entrySet().removeIf(entry -> {
            if (!currentPackets.contains(entry.getKey())) {
                this.getChildren().remove(entry.getValue());
                Label labelToRemove = entry.getKey().getSizeLabel();
                if (labelToRemove != null) {
                    this.getChildren().remove(labelToRemove);
                }
                return true;
            }
            return false;
        });
    }
}