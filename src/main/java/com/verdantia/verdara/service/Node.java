package com.verdantia.verdara.service;

public class Node {
    private final int x;
    private final int y;
    private final int angle;
    private final int base;

    public Node(int x, int y, int angle, int base) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.base = base;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getAngle() {
        return angle;
    }

    public int getBase() {
        return base;
    }

    public Node rotate(int rotateAngle) {
        return new Node(this.x, this.y, this.angle + rotateAngle, this.base);
    }
}
