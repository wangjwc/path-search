package com.learn.path.star2;

/**
 * @Author : wangjingwang
 * @Date : 2020/1/19 10:02
 * @Description :
 */
public class Node implements Comparable<Node> {
    public Node parent;
    public int x, y;
    public double g;
    public double h;

    public Node(Node parent, int xpos, int ypos, double g, double h) {
        this.parent = parent;
        this.x = xpos;
        this.y = ypos;
        this.g = g;
        this.h = h;
    }

    // Compare by f value (g + h)
    @Override
    public int compareTo(Node o) {
        return (int) ((this.g + this.h) - (o.g + o.h));
    }
}
