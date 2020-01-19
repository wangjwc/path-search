package com.learn.path.star2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author : wangjingwang
 * @Date : 2020/1/17 18:44
 * @Description :
 */
public class AStar {
    private final List<Node> open;
    private final List<Node> closed;
    private final List<Node> path;
    private final int[][] maze;
    private Node now;
    private final int xstart;
    private final int ystart;
    private int xend, yend;
    /**
     * 是否允许对角
     */
    private final boolean diag;

    AStar(int[][] maze, int xstart, int ystart, boolean diag) {
        this.open = new ArrayList<>();
        this.closed = new ArrayList<>();
        this.path = new ArrayList<>();
        this.maze = maze;
        this.now = new Node(null, xstart, ystart, 0, 0);
        this.xstart = xstart;
        this.ystart = ystart;
        this.diag = diag;
    }

    /*
     ** Finds path to xend/yend or returns null
     **
     ** @param (int) xend coordinates of the target position
     ** @param (int) yend
     ** @return (List<Node> | null) the path
     */
    public List<Node> findPathTo(int xend, int yend) {
        this.xend = xend;
        this.yend = yend;
        this.closed.add(this.now);
        addNeigborsToOpenList();
        while (this.now.x != this.xend || this.now.y != this.yend) {
            if (this.open.isEmpty()) { // Nothing to examine
                return null;
            }
            this.now = this.open.get(0); // get first node (lowest f score)
            this.open.remove(0); // remove it
            this.closed.add(this.now); // and add to the closed
            addNeigborsToOpenList();
        }
        this.path.add(0, this.now);
        while (this.now.x != this.xstart || this.now.y != this.ystart) {
            this.now = this.now.parent;
            this.path.add(0, this.now);
        }
        return this.path;
    }

    /*
     ** Looks in a given List<> for a node
     **
     ** @return (bool) NeightborInListFound
     */
    private static boolean findNeighborInList(List<Node> array, Node node) {
        return array.stream().anyMatch((n) -> (n.x == node.x && n.y == node.y));
    }

    /**
     * 计算this.now的相邻方格到目的地（xend/yend）间的距离
     *
     * @param dx
     * @param dy
     * @return
     */
    private double distance(int dx, int dy) {
        if (this.diag) { // if diagonal movement is alloweed
            // sqrt(h2 +w2)
            // 计算欧氏距离(直角三角形的斜边长度)
            return Math.hypot(this.now.x + dx - this.xend, this.now.y + dy - this.yend);
        } else {
            // h + w
            // 计算曼哈顿距离(直角三角形的直角边总长度)
            return Math.abs(this.now.x + dx - this.xend) + Math.abs(this.now.y + dy - this.yend); // else return "Manhattan distance"
        }
    }

    /**
     * 添加临近节点
     */
    private void addNeigborsToOpenList() {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0) {
                    continue;
                }

                if (this.now.x + x < 0 || this.now.x + x >= this.maze[0].length) {
                    continue;
                }

                if (this.now.y + y < 0 || this.now.y + y >= this.maze.length) {
                    continue;
                }

                if (!this.diag && x != 0 && y != 0) {
                    continue; // skip if diagonal movement is not allowed
                }

                Node node = new Node(this.now, this.now.x + x, this.now.y + y, this.now.g, this.distance(x, y));

                /**
                 * 过滤障碍物
                 */
                if (this.maze[this.now.y + y][this.now.x + x] != -1 // check if square is walkable
                        && !findNeighborInList(this.open, node) && !findNeighborInList(this.closed, node)) { // if not already done
                    // 加基础移动成本(1)
                    node.g = node.parent.g + 1.0;
                    // 加方格地形移动成本
                    node.g += maze[this.now.y + y][this.now.x + x];

                    // diagonal cost = sqrt(hor_cost² + vert_cost²)
                    // in this example the cost would be 12.2 instead of 11
                    if (diag && x != 0 && y != 0) {
                        node.g += 0.4;    // Diagonal movement cost = 1.4
                    }
                    this.open.add(node);
                }
            }
        }
        Collections.sort(this.open);
    }

    public static void main(String[] args) {
        // -1 = blocked
        // 0+ = additional movement cost
        int[][] maze = {
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 100, 100, 100, 0, 0},
                {0, 0, 0, 0, 0, 100, 0, 0},
                {0, 0, 100, 0, 0, 100, 0, 0},
                {0, 0, 100, 0, 0, 100, 0, 0},
                {0, 0, 100, 100, 100, 100, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
        };
        AStar as = new AStar(maze, 0, 0, true);
        List<Node> path = as.findPathTo(7, 7);


        if (path != null) {
            path.forEach((n) -> {
                System.out.print("[" + n.x + ", " + n.y + "] ");
                maze[n.y][n.x] = -2;
            });
            System.out.printf("\nTotal cost: %.02f\n", path.get(path.size() - 1).g);

            /**
             * 列序号
             */
            System.out.print("+");
            for (int i = 0; i < maze[0].length; i++) {
                System.out.print(i);
            }
            System.out.println();

            /**
             * 行
             */
            for (int i = 0; i < maze.length; i++) {
                System.out.print(i);
                for (int j = 0; j < maze[i].length; j++) {
                    switch (maze[i][j]) {
                        case 0:
                            System.out.print(".");
                            break;
                        case -2:
                            System.out.print("*");
                            break;
                        default:
                            System.out.print("#");
                    }
                }
                System.out.println();
            }
        }
    }
}
